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
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.service.impl.SelectQueryTemplate;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpHeader;
import de.dfki.asr.ajan.common.AgentUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
	private BehaviorSelectQuery queryURI;

	@RDF("bt:binding")
	@Setter @Getter
	private HttpBinding binding;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	protected String requestURI;
	protected HttpConnection request;
	protected static final Logger LOG = LoggerFactory.getLogger(Message.class);

	@Override
	public Resource getType() {
		return BTVocabulary.MESSAGE;
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			setRequestUri();
			if (binding.getBtHeaders() != null) {
				binding.setAddHeaders(BTUtil.getInitializedRepository(getObject(), binding.getBtHeaders().getOriginBase()));
			}
			binding.setRequestURI(new URI(requestURI));
			request = new HttpConnection(binding);
			prepareRequest();
			LOG.info("Executing request {}", request.toString());
			if (!checkResponse(request.execute())) {
				LOG.info(toString() + " FAILED due to malformed response model");
				return new NodeStatus(Status.FAILED, toString() + " FAILED");
			}
			LOG.info(toString() + " SUCCEEDED");
			return new NodeStatus(Status.SUCCEEDED, toString() + " SUCCEEDED");
		} catch (IOException | URISyntaxException | MessageEvaluationException | SAXException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	protected void prepareRequest() throws URISyntaxException, IOException, MessageEvaluationException {
		String payload = null;
		if (binding.getPayload() != null) {
			payload = getInput(binding);
		}
		if (payload != null) {
			request.setPayload(payload);
		}
	}

	protected boolean checkResponse(final Object response) throws URISyntaxException {
		if (response instanceof Model) {
			Model model = (Model) response;
			if (model.isEmpty()) {
				model.add(vf.createIRI(requestURI), BTVocabulary.HAS_RESPONSE, BTVocabulary.EMPTY);
				return updateBeliefs(AgentUtil.setNamedGraph(model, new URI(requestURI)), targetBase);
			}
			return updateBeliefs(AgentUtil.setNamedGraph(model, new URI(requestURI)), targetBase);
		}
		return true;
	}

	protected boolean updateBeliefs(final Model model, final URI targetBase) {
		if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			this.getObject().getExecutionBeliefs().update(model);
			return true;
		} else if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			this.getObject().getAgentBeliefs().update(model);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	protected void setRequestUri() throws URISyntaxException, MessageEvaluationException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), queryURI.getOriginBase());
		List<BindingSet> result = queryURI.getResult(repo);
		if (result.isEmpty()) {
			throw new MessageEvaluationException("No ?requestURI defined in Message description");
		}
		BindingSet bindings = result.get(0);
		Value strValue = bindings.getValue("requestURI");
		if (strValue == null) {
			throw new MessageEvaluationException("No ?requestURI defined in Message description");
		} else {
			requestURI = strValue.stringValue();
		}
	}

	protected String getInput(final HttpBinding binding) throws URISyntaxException, UnsupportedEncodingException, IOException {
		SelectQueryTemplate tmpl = binding.getPayload().getTemplate();
		if (tmpl != null) {
			return getQueryTemplate(tmpl);
		}
		BehaviorQuery query = binding.getPayload().getBtQuery();
		if (query.getSparql().equals("")) {
			return null;
		}
		Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		List<HttpHeader> headers = binding.getHeaders();
		String mimeType = ACTNUtil.getMimeTypeFromHeaders(headers);
		if (mimeType.contains("application/sparql-results+xml")) {
			BehaviorSelectQuery select = new BehaviorSelectQuery();
			select.setSparql(query.getSparql());
			return select.getResult(repo, TupleQueryResultFormat.SPARQL);
		} else {
			BehaviorConstructQuery construct = new BehaviorConstructQuery();
			construct.setSparql(query.getSparql());
			return ACTNUtil.getModelPayload(construct.getResult(repo), mimeType);
		}
	}

	protected String getQueryTemplate(final SelectQueryTemplate tmpl) throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), tmpl.getBtQuery().getOriginBase());
		BehaviorConstructQuery queryAll = new BehaviorConstructQuery();
		queryAll.setSparql("CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}");
		return ACTNUtil.getTemplatePayload(queryAll.getResult(repo), tmpl);
	}

	@Override
	public String toString() {
		return "Message (" + getLabel() + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL) && queryURI != null) {
			queryURI.setResultModel(getInstance(root.getInstance()), BTVocabulary.QUERY_URI_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.UNCLEAR;
	}
}
