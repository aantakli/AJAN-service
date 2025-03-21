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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions;

import de.dfki.asr.ajan.behaviour.exception.ConditionSimulationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.core.MBoolResponse;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:AbortInstruction")
public class AbortInstruction extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:coSim")
	@Getter @Setter
	private BehaviorSelectQuery query;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	private String host;
	private int port;
	private String avatarId;

	@RDF("bt-mosim:instructionID")
	@Setter
	private BehaviorSelectQuery instructionIDs;

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#AbortInstruction");
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			Map<String,String> cosim = MOSIMUtil.getCoSimInfos(query, this.getObject());
			if(!cosim.isEmpty()) {
				host = cosim.get("host");
				port = Integer.parseInt(cosim.get("port"));
				avatarId = cosim.get("avatarId");
				if (abortInstruction()) {
					String report = toString() + " SUCCEEDED";
					return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
				};
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
		}
		String report = toString() + " FAILED";
		return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
	}

	private boolean abortInstruction() throws URISyntaxException {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
				List<String> ids = getInstructionIDs();
				MBoolResponse response;
				if (ids.isEmpty()) {
					response = client.Abort(avatarId);
				} else {
					response = client.AbortInstructions(ids);
				}
				transport.close();
				addStatements(ids);
				return response.Successful;
			}
		} catch (TException | ConditionSimulationException ex) {
			this.getObject().getLogger().info(this.getClass(), "Could not load List<MSceneObject>", ex);
			return false;
		}
	}

	private List<String> getInstructionIDs() throws URISyntaxException {
		List<String> instructions = new ArrayList();
		if (instructionIDs != null) {
			Repository repo = BTUtil.getInitializedRepository(this.getObject(), instructionIDs.getOriginBase());
			List<BindingSet> result = instructionIDs.getResult(repo);
			if (!result.isEmpty()) {
				Iterator<BindingSet> itr = result.iterator();
				while (itr.hasNext()) {
					instructions.add(itr.next().getValue("id").stringValue());
				}
			}
		}
		return instructions;
	}

	private void addStatements(final List<String> ids) throws ConditionSimulationException {
		Model model = new LinkedHashModel();
		Resource instRoot = vf.createBNode();
		String timeStamp = MOSIMUtil.getTimeStamp();
		model.add(instRoot, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
		model.add(instRoot, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.ABORT_INSTRUCTION);
		model.add(instRoot, MOSIMVocabulary.HAS_TIMESTAMP, vf.createLiteral(timeStamp));
		model.add(instRoot, MOSIMVocabulary.HAS_INSTRUCTION_IDS, vf.createLiteral(StringUtils.join(ids, ",")));
		performWrite(model);
	}
	
	private void performWrite(final Model model) throws ConditionSimulationException {
		try {
			if (repository.toString().equals(AJANVocabulary.DOMAIN_KNOWLEDGE.toString())
							|| repository.toString().equals(AJANVocabulary.SERVICE_KNOWLEDGE.toString())
							|| repository.toString().equals(AJANVocabulary.BEHAVIOR_KNOWLEDGE.toString())) {
				return;
			}
			if (repository.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
				this.getObject().getExecutionBeliefs().update(model);
			} else if (repository.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
				this.getObject().getAgentBeliefs().update(model);
			} else {
				updateExternalRepo(new SPARQLRepository(repository.toString()), model);
			}
		} catch (QueryEvaluationException ex) {
			throw new ConditionSimulationException(ex);
		}
	}

	private void updateExternalRepo(final Repository repo, final Model model) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.begin(IsolationLevels.SERIALIZABLE);
			addStatementsWith(model,conn);
			conn.commit();
		}
	}

	private void addStatementsWith(final Model model, final RepositoryConnection connection) {
		model.forEach((stmt) -> {
			connection.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), (Resource) stmt.getContext());
		});
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "AbortInstruction (" + getLabel() + ")";
	}
	
	@Override
	public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return SimulationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
