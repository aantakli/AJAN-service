/*
 * Copyright (C) 2021 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.dfki.asr.ajan.pluginsystem.mappingplugin.extensions.mapping;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.exception.MessageEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.messages.Message;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.common.AgentUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.JSONMappingException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.poser.Converter.RdfToJson;
import de.dfki.asr.poser.Namespace.JSON;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
@Extension
@RDFBean("bt:JsonMessage")
public class JsonMessage extends Message implements NodeExtension {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:queryUri")
	@Setter @Getter
	private BehaviorSelectQuery queryURI;

	@RDF("bt:binding")
	@Setter @Getter
	private HttpBinding binding;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("poser:dataMapping")
	@Getter @Setter
	private BehaviorConstructQuery dataMapping;

	@RDF("poser:apiMapping")
	@Getter @Setter
	private BehaviorConstructQuery apiMapping;
	
	@RDF("bt:mapping")
	@Getter @Setter
	private URI mapping;

	private Model domainResponse;
	
	@Override
	public LeafStatus executeLeaf() {
		super.setUrl(url);
		super.setQueryURI(queryURI);
		try {
			setRequestUri();
			if (binding.getBtHeaders() != null && !binding.getBtHeaders().getSparql().isEmpty()) {
				binding.setAddHeaders(BTUtil.getInitializedRepository(getObject(), binding.getBtHeaders().getOriginBase()));
			}
			super.setBinding(binding);
			getBinding().setRequestURI(new URI(requestURI));
			request = new HttpConnection(getBinding());
			String payload = createPayload();
			request.setPayload(payload);
			LOG.info("Executing request {}", request.toString());
			if (!checkResponse(request.execute())) {
				LOG.info(toString() + " FAILED due to malformed response model");
				return new LeafStatus(Task.Status.FAILED, toString() + " FAILED");
			}
			LOG.info(toString() + " SUCCEEDED");
			return new LeafStatus(Task.Status.SUCCEEDED, toString() + " SUCCEEDED");
		} catch (IOException | URISyntaxException | MessageEvaluationException | JSONMappingException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new LeafStatus(Task.Status.FAILED, toString() + " FAILED");
		}
	}

	private String createPayload() throws URISyntaxException, UnsupportedEncodingException, JSONMappingException {
		String payload = "";
		if (getBinding().getPayload() == null || dataMapping == null || apiMapping == null) {
			throw new JSONMappingException("No payload or Mapping defined!");
		}
		Model inputModel = getInputModel(binding);
		Repository dataRepo =  BTUtil.getInitializedRepository(getObject(), dataMapping.getOriginBase());
		Model dataModel = modifyResponse(dataMapping.getResult(dataRepo), JSON.INPUT_DATA_TYPE.toString());
		Repository apiRepo =  BTUtil.getInitializedRepository(getObject(), apiMapping.getOriginBase());
		Model apiModel = modifyResponse(apiMapping.getResult(apiRepo), JSON.API_DESCRIPTION.toString());
		dataModel.addAll(apiModel);
		RdfToJson mapper = new RdfToJson();
		payload = mapper.buildJsonString(inputModel, dataModel);
		return payload;
	}

	protected Model getInputModel(final HttpBinding binding) throws URISyntaxException {
		BehaviorConstructQuery query = (BehaviorConstructQuery) binding.getPayload().getBtQuery();
		Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		return query.getResult(repo);
	}

	@Override
	protected boolean checkResponse(final Object response) {
		if (response instanceof Model) {
			domainResponse = (Model)response;
		} else {
			try {
				domainResponse = getModel(response);
			} catch (URISyntaxException | RMLMapperException | IOException | TransformerException ex) {
				LOG.error("Malformed response!");
				LOG.error("Mime Type is not supported!");
				return false;
			}
		}
		return updateBeliefs(modifyResponse(domainResponse, super.requestURI), targetBase);
	}

	protected Model modifyResponse(final Model model, final String uri) {
		return AgentUtil.setNamedGraph(model.iterator(), uri);
	}

	protected Model getModel(final Object response) throws RMLMapperException, URISyntaxException, IOException, TransformerException {
		Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
		InputStream resourceStream = MappingUtil.getResourceStream(response);
        if (mapping != null) {
			return MappingUtil.getMappedModel(MappingUtil.getTriplesMaps(repo, mapping), resourceStream);
		}
		else {
			throw new RMLMapperException("no mapping file selected!");
		}
	}
}
