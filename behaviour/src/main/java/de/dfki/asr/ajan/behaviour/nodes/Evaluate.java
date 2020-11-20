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
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:Evaluate")
public class Evaluate extends AbstractTDBLeafTask {
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

	private static final Logger LOG = LoggerFactory.getLogger(Evaluate.class);

	@Override
	public Resource getType() {
		return BTVocabulary.EVALUATE;
	}

	@Override
	public void reset() {
		for (BehaviorConstructQuery query : queries) {
			query.setReset(true);
		}
		super.reset();
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			if (queries == null) {
				String report = toString() + " FAILED";
				LOG.info(report);
				return new LeafStatus(Status.FAILED, report);
			} else {
				startEvaluation();
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED due to evaluation error";
			LOG.info(report, ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	private void startEvaluation() throws URISyntaxException {
		Model init = new LinkedHashModel();
		for (BehaviorConstructQuery query : queries) {
			Repository origin = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			init = AgentUtil.mergeModels(init, query.getResult(origin), null);
		}
		EvaluationResult result = new EvaluationResult(init, this.getObject());
		evaluate(result);
		((TreeNode)control).evaluate(result.setDirection(Direction.Up));
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
		return "Evalaute (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
