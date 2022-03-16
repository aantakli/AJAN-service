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
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpResponseException;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.xml.sax.SAXException;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("asp:ILASPInput")
public class ILASPInput extends Problem implements NodeExtension {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ILASPInput.class);

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

    @RDF("asp:domain")
    @Getter @Setter
    private BehaviorConstructQuery query;

    @RDF("asp:ruleSets")
    @Getter @Setter
    private List<URI> rules;

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
				LOG.info(toString() + " UNSATISFIABLE");
				return new NodeStatus(Status.FAILED, toString() + " UNSATISFIABLE");
			}
			if(getFacts() != null) {
				ObjectNode payload = getIlaspPayload();
				sendToIlasp(payload);
			}
			String report = toString() + " SUCCEEDED";
			LOG.info(report);
			return new NodeStatus(Status.SUCCEEDED, report);
		} catch (URISyntaxException | RDFBeanException | LoadingRulesException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		} catch (IOException | SAXException ex) {
			LOG.info(toString() + " FAILED due transmission problems", ex);
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		} catch (MessageEvaluationException ex) {
			LOG.info(toString() + " FAILED due to malformed request URI", ex);
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		}
    }

	private ObjectNode getIlaspPayload() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		List<String> statements = PatternUtil.getFacts(getFacts().get(0));
		ArrayNode array = mapper.valueToTree(statements.toArray());
		node.putArray("facts").addAll(array);
		return node;
	}

	private void sendToIlasp(final ObjectNode payload) throws URISyntaxException, MessageEvaluationException, IOException, SAXException {
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
            LOG.info("ILASPInput (" + getStatus() + ")");
    }

	@Override
	public String toString() {
		return "ILASPInput (" + getLabel() + ")";
	}
}
