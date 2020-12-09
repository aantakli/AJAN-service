/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.mappingplugin.extensions;

import be.ugent.rml.store.RDF4JStore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.messages.QueryDomain;
import de.dfki.asr.ajan.behaviour.nodes.messages.SyncMessage;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpHeader;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:QueryJsonDomain")
public class QueryJsonDomain extends SyncMessage implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("bt:mapping")
	@Getter @Setter
	private URI mapping;

	@RDF("bt:mappings")
	@Getter @Setter
	private List<URI> mappings;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("bt:queryUri")
	@Getter @Setter
	private BehaviorSelectQuery queryURI;
	private Model domainResponse;

	@Override
	public Status execute() {
		HttpBinding bng = new HttpBinding();
		try {
			bng.setMethod(new URI(AJANVocabulary.HTTP_METHOD_GET.toString()));
			bng.setHeaders(getHeaders());
			super.setUrl(url);
			super.setQueryURI(queryURI);
			super.setBinding(bng);
		} catch (URISyntaxException ex) {
			Logger.getLogger(QueryDomain.class.getName()).log(Level.SEVERE, null, ex);
		}
		return super.execute();
	}

	private List<HttpHeader> getHeaders() throws URISyntaxException {
		List list = new ArrayList();
		HttpHeader accept = new HttpHeader();
		accept.setHeaderName(new URI(AJANVocabulary.HTTP_HEADER_ACCEPT.toString()));
		accept.setHeaderValue("application/json");
		list.add(accept);
		return list;
	}

	@Override
	protected boolean checkResponse(final Object response) {
		if (response instanceof ObjectNode) {
			try {
				domainResponse = getModel((ObjectNode)response);
			} catch (URISyntaxException | RMLMapperException ex) {
				LOG.error("Malformed response!");
				return false;
			}
		} else if (response instanceof Model) {
			domainResponse = (Model)response;
		} else {
			LOG.error("Mime Type is not supported!");
			return false;
		}
		return updateBeliefs(modifyResponse(domainResponse), targetBase);
	}

	protected Model getModel(final ObjectNode input) throws RMLMapperException, URISyntaxException {
		Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
		RDF4JStore rmlStore;
		if (mapping == null) {
			rmlStore = new RDF4JStore(MappingUtil.getTriplesMaps(repo, mappings));
		} else {
			rmlStore = new RDF4JStore(MappingUtil.getTriplesMaps(repo, mapping));
		}
		Model model = MappingUtil.loadJsonMapping(input, rmlStore);
		return model;
	}

	@Override
	protected Model modifyResponse(final Model model) {
		return AgentUtil.setNamedGraph(model.iterator(), super.requestURI);
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "QueryJsonDomain (" + getUrl() + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			Resource resource = getInstance(root.getInstance());
			queryURI.setResultModel(resource, BTVocabulary.QUERY_URI_RESULT, model);
			if (domainResponse != null && !domainResponse.isEmpty()) {
				Resource resultNode = vf.createBNode();
				model.add(resource, BTVocabulary.HAS_ACTION_RESULT, resultNode);
				model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.RESPONSE_RESULT);
				model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.GRAPH_RESULT);
				Resource resultGraph = BTUtil.setGraphResultModel(model, domainResponse);
				model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
			}
		}
		return super.getModel(model, root, mode);
	}
}
