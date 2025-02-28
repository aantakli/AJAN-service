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

package de.dfki.asr.ajan.behaviour.nodes;

import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
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
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;

@RDFBean("bt:Simulate")
public class Simulate extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:initialKnowledge")
	@Getter @Setter
	private List<BehaviorConstructQuery> queries;

	@RDF("bt:context")
	@Getter @Setter
	private URI context;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@Override
	public Resource getType() {
		return BTVocabulary.SIMULATE;
	}

	@Override
	public void reset() {
		for (BehaviorConstructQuery query : queries) {
			query.setReset(true);
		}
		super.reset();
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			if (queries == null) {
				return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED");
			} else {
				startEvaluation();
				return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
			}
		} catch (URISyntaxException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due malformed URI", ex);
		}
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	private void startEvaluation() throws URISyntaxException {
		Model init = new LinkedHashModel();
		for (BehaviorConstructQuery query : queries) {
			Repository origin = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			init = AgentUtil.mergeModels(init, query.getResult(origin), null);
		}
		SimulationResult result = new SimulationResult(init, this.getObject());
		simulate(result);
		((TreeNode)control).simulate(result.setDirection(Direction.Up));
		Model model = AgentUtil.setNamedGraph(result.getEvaluationModel(), context);
		writeToTarget(model);
	}

	private void writeToTarget(final Model model) {
		if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			this.getObject().getAgentBeliefs().update(model);
		} else if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			this.getObject().getExecutionBeliefs().update(model);
		}
	}

	@Override
	public String toString() {
		return "Simulate (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
