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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.extensions;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.NoActionAvailableException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.TermEvaluationException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.VariableEvaluationException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.utils.PlanBuilder;
import graphplan.flyweight.OperatorFactoryException;
import graphplan.graph.planning.PlanningGraphException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("strips:Problem")
public class Problem extends AbstractTDBBranchTask implements NodeExtension {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Problem.class);

	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("strips:actions")
	@Getter @Setter
	private List<URI> actions;

    @RDF("strips:goalStates")
	@Getter @Setter
	private List<BehaviorQuery> goals;

	@RDF("strips:initStates")
	@Getter @Setter
	private List<BehaviorQuery> initials;

	@RDF("strips:config")
	@Setter @Getter
	private List<GraphPlanConfig> configs;

	@RDF("strips:randomExecute")
	@Setter @Getter
	private Boolean randomExecute = false;

	@RDF("bt:resetBehavior")
	@Setter @Getter
	private Boolean resetPlan = false;

	@Override
	public String toString() {
		return "STRIPSProblem ( " + url + " )";
	}

	@Override
	public Resource getType() {
		ValueFactory vf = SimpleValueFactory.getInstance();
		return vf.createIRI("http://www.ajan.de/behavior/strips-ns#Problem");
	}

	@Override
	public void run () {
		if (runningChild != null) {
			runningChild.run();
			return;
		}
		if (resetPlan) {
			children.clear();
		}
		if (children.size == 0) {
			try {
				if(!generatePlan()) {
					LOG.info(toString() + " SUCCEEDED");
					LOG.info("Status (SUCCEEDED)");
					success();
					return;
				}
			} catch (OperatorFactoryException | TermEvaluationException | NoActionAvailableException
					| ConditionEvaluationException | URISyntaxException | RDFBeanException | VariableEvaluationException ex) {
				LOG.error(toString() + " FAILED due to evaluation error", ex);
				LOG.info("Status (FAILED)");
				fail();
				return;
			} catch (PlanningGraphException | TimeoutException ex) {
				LOG.info(toString() + " FAILED due to no plan result", ex);
				LOG.info("Status (FAILED)");
				fail();
				return;
			}
		}
		startChild();
	}

	private boolean generatePlan()
			throws ConditionEvaluationException, URISyntaxException, PlanningGraphException, OperatorFactoryException,
			TimeoutException, RDFBeanException, VariableEvaluationException, TermEvaluationException, NoActionAvailableException {
		Task<AgentTaskInformation> task;
		task = new PlanBuilder(this).build();
		if(task.getChildCount() > 0) {
			addChild(task);
			return true;
		} else
			return false;
	}

	private void startChild() {
		runningChild = getChild(0);
		runningChild.setControl(this);
		runningChild.start();
		runningChild.run();
	}

	@Override
	public void childFail (Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}

	@Override
	public void childSuccess (Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			Resource resource = getInstance(root.getInstance());
			goals.forEach((query) -> {
				query.setResultModel(resource, BTVocabulary.GOAL_RESULT,  model);
			});
			initials.forEach((query) -> {
				query.setResultModel(resource, BTVocabulary.INITIAL_RESULT,  model);
			});
		}
		return super.getModel(model, root, mode);
	}
}
