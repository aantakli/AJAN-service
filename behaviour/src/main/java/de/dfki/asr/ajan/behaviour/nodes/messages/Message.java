/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes.messages;

import de.dfki.asr.ajan.behaviour.exception.MessageEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.service.impl.SelectQueryTemplate;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.service.impl.HttpHeader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:Message")
@SuppressWarnings("PMD.ExcessiveImports")
public class Message extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:queryUri")
	@Setter @Getter
	private BehaviorConstructQuery queryURI;

	@RDF("bt:binding")
	@Setter @Getter
	private HttpBinding binding;

	protected String requestURI;
	protected HttpConnection request;
	protected static final Logger LOG = LoggerFactory.getLogger(Message.class);

	@Override
	public Resource getType() {
		return BTVocabulary.MESSAGE;
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			setRequestUri();
			binding.setRequestURI(new URI(requestURI));
			request = new HttpConnection(binding);
			String payload = null;
			if (binding.getPayload() != null) {
				payload = getInput(binding);
			}
			request.setPayload(payload);
			LOG.info("Executing request {}", request.toString());
			if (!checkResponse(request.execute())) {
				LOG.info(toString() + " FAILED due to malformed response model");
				return new LeafStatus(Status.FAILED, toString() + " FAILED");
			}
			LOG.info(toString() + " SUCCEEDED");
			return new LeafStatus(Status.SUCCEEDED, toString() + " SUCCEEDED");
		} catch (IOException | URISyntaxException | MessageEvaluationException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	protected boolean checkResponse(final Object response) {
		return true;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	protected void setRequestUri() throws URISyntaxException, MessageEvaluationException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), queryURI.getOriginBase());
		Model result = queryURI.getResult(repo);
		if (result.contains(null, AJANVocabulary.HTTP_REQUEST_URI, null)) {
			Model uris = result.filter(null, AJANVocabulary.HTTP_REQUEST_URI, null);
			requestURI = readLiteralUri(Models.objectLiteral(uris));
			if (requestURI.isEmpty()) {
				requestURI = readResourceUri(Models.objectIRI(uris));
			}
		} else {
			throw new MessageEvaluationException("No RequestURI defined in Message description");
		}
	}

	protected String getInput(final HttpBinding binding) throws URISyntaxException, UnsupportedEncodingException {
		BehaviorConstructQuery query = binding.getPayload().getBtQuery();
		SelectQueryTemplate tmpl = binding.getPayload().getTemplate();
		if (tmpl != null) {
			Repository repo = BTUtil.getInitializedRepository(getObject(), tmpl.getOriginBase());
			return ACTNUtil.getTemplatePayload(query.getResult(repo), tmpl);
		}
		Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		List<HttpHeader> headers = binding.getHeaders();
		String mimeType = ACTNUtil.getMimeTypeFromHeaders(headers);
		return ACTNUtil.getModelPayload(query.getResult(repo), mimeType);
	}

	private String readLiteralUri(final Optional<Literal> lit) throws MessageEvaluationException {
		if (!lit.isPresent()) {
			return "";
		}
		if (!lit.get().getDatatype().equals(XMLSchema.ANYURI)) {
			throw new MessageEvaluationException("RequestURI not defined as xsd:anyURI");
		}
		return lit.get().stringValue();
	}

	private String readResourceUri(final Optional<IRI> res) throws MessageEvaluationException {
		if (!res.isPresent()) {
			throw new MessageEvaluationException("No RequestUri specified");
		}
		return res.get().toString();
	}

	@Override
	public String toString() {
		return "Message (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.UNCLEAR;
	}
}
