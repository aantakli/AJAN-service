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

package de.dfki.asr.ajan.pluginsystem.aspplugin.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.behaviour.exception.AJANRequestException;
import de.dfki.asr.ajan.behaviour.exception.MessageEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.LoadingRulesException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.ASPConfig;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.PatternUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.xml.sax.SAXException;
import org.pf4j.Extension;

@Extension
@RDFBean("asp:ILASPInput")
public class ILASPInput extends Problem implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

	@RDF("bt:queryUri")
	@Setter @Getter
	private BehaviorSelectQuery queryURI;

	@RDF("bt:binding")
	@Setter @Getter
	private HttpBinding binding;

    @RDF("asp:config")
    @Getter @Setter
    private ASPConfig config;

	@RDF("asp:posExamples")
	@Setter @Getter
	private BehaviorSelectQuery pos;

	@RDF("asp:negExamples")
	@Setter @Getter
	private BehaviorSelectQuery neg;

    @RDF("asp:domain")
    @Getter @Setter
    private BehaviorConstructQuery query;

    @RDF("asp:ruleSets")
    @Getter @Setter
    private List<RuleSetLocation> rules;

    @RDF("asp:write")
    @Getter @Setter
    private ASPWrite write;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	private String requestURI;

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/asp-ns#ILASPInput");
	}
    @Override
    public NodeStatus executeLeaf() {
		try {
			setRequestUri();
			generateRuleSet();
			if(!getConfig().runSolver(this)) {
				return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " UNSATISFIABLE");
			}
			if(getFacts() != null) {
				ObjectNode payload = getIlaspPayload();
				sendToIlasp(payload);
			}
			return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
		} catch (URISyntaxException | RDFBeanException | LoadingRulesException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to query evaluation error", ex);
		} catch (IOException | SAXException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due transmission problems", ex);
		} catch (MessageEvaluationException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to malformed request URI", ex);
		} catch (AJANRequestException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to wrong content-type in response. Expecting RDF-based content!");
		}
    }

	private ObjectNode getIlaspPayload() throws URISyntaxException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.putArray("pos").addAll(getExamplesArray("pos", pos));
		node.putArray("neg").addAll(getExamplesArray("neg", neg));
		node.putArray("facts").addAll(getFactsArray());
		return node;
	}

	private ArrayNode getExamplesArray(final String label, final BehaviorSelectQuery query) throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		List<BindingSet> bindings = query.getResult(repo);
		List<String> results = new ArrayList<>();
		for(BindingSet set: bindings) {
			if(set.hasBinding(label)) {
				results.add(set.getBinding(label).getValue().stringValue());
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(results.toArray());
	}

	private ArrayNode getFactsArray() {
		ObjectMapper mapper = new ObjectMapper();
		List<String> statements = PatternUtil.getFacts(getFacts().get(0));
		return mapper.valueToTree(statements.toArray());
	}

	private void sendToIlasp(final ObjectNode payload) throws URISyntaxException, MessageEvaluationException, IOException, SAXException, AJANRequestException {
		if (binding.getBtHeaders() != null) {
			binding.setAddHeaders(BTUtil.getInitializedRepository(getObject(), binding.getBtHeaders().getOriginBase()));
		}
		binding.setRequestURI(new URI(requestURI));
		HttpConnection request = new HttpConnection(binding);
		request.setPayload(payload.toString());
		request.execute();
	}

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

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

	@Override
	public String toString() {
		return "ILASPInput (" + getLabel() + ")";
	}
}
