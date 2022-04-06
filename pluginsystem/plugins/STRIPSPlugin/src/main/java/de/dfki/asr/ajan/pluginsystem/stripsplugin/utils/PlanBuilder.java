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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.utils;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.AJANGoal;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.Consumable;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.NoActionAvailableException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.TermEvaluationException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.VariableEvaluationException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.extensions.GraphPlanConfig;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.extensions.Problem;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.graphplan.Planner;
import graphplan.PlanResult;
import graphplan.PlanSolution;
import graphplan.domain.DomainDescription;
import graphplan.domain.Operator;
import graphplan.domain.Proposition;
import graphplan.flyweight.OperatorFactoryException;
import graphplan.graph.planning.PlanningGraphException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.slf4j.LoggerFactory;

public class PlanBuilder {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PlanBuilder.class);
	private final URIManager uriManager = new URIManager();

	private final List<URI> actions;
	private final List<BehaviorQuery> goals;
	private final List<BehaviorQuery> initials;
	private final List<GraphPlanConfig> configs;
	private final Boolean randomExecute;
	private final String url;
	private final AgentTaskInformation taskInfo;

	private final Map<String, AJANOperator> ajanOps = new HashMap();

	public PlanBuilder(Problem problem){
		actions = problem.getActions();
		goals = problem.getGoals();
		initials = problem.getInitials();
		configs = problem.getConfigs();
		randomExecute = problem.getRandomExecute();
		url = problem.getUrl();
		taskInfo = problem.getObject();
	}
	
	public Task<AgentTaskInformation> build ()
			throws ConditionEvaluationException, URISyntaxException, PlanningGraphException, OperatorFactoryException,
			TimeoutException, RDFBeanException, VariableEvaluationException, TermEvaluationException, NoActionAvailableException {
		DomainDescription domain = getDomainDescription();
		GraphPlanConfig config = configs.get(0);
		Planner planner = new Planner(config.getMaxLevels(), config.getMaxLength(), config.getTimeout(), config.getAllSolutions(), domain);     
		PlanSolution solution = planner.plan();
		return createSubTree(solution.getAllHighlevelPlans());
	}

	private DomainDescription getDomainDescription()
			throws ConditionEvaluationException, URISyntaxException, RDFBeanException,
			VariableEvaluationException, TermEvaluationException, NoActionAvailableException {
		List<Proposition> init = StateLoader.getState(initials, taskInfo, uriManager);
		List<Proposition> goal = StateLoader.getState(goals, taskInfo, uriManager);
		List<Operator> ops = getOperators();
		printProblemDomain(init, goal, ops);
		return new DomainDescription(ops, init, goal);
	}

	private List<Operator> getOperators()
			throws RDFBeanException, VariableEvaluationException, TermEvaluationException, NoActionAvailableException, URISyntaxException {
		PlannerUtil.checkActionsAvailability(actions);
		List operators = new ArrayList();
		for (URI actionURI: actions) {
			OperatorBuilder builder = new OperatorBuilder(uriManager, ajanOps, actionURI, taskInfo);
			operators.add(builder.build());
		}
		return operators;
	}

	private void printProblemDomain(List<Proposition> init, List<Proposition> goal, List<Operator> ops) {
		printPropositions(init, "State:");
		printPropositions(goal, "Goal:");
		for (Operator op: ops) {
			printOperator(op, "Operator:");
			printPropositions(op.getPreconds(), "Preconds:");
			printPropositions(op.getEffects(), "Effects:");
		}
	}

	private void printPropositions(List<Proposition> props, String type) {
		LOG.info(type);
		for (Proposition prop: props) {
			LOG.info(prop.toString());
		}
	}

	private void printOperator(Operator op, String type) {
		LOG.info(type);
		LOG.info(op.toString());
	}

	private Task<AgentTaskInformation> createSubTree(Set<PlanResult> planResults) throws URISyntaxException, TermEvaluationException {
		Sequence sequence = new Sequence();
		if(!planResults.isEmpty()) {
			PlanResult plan = PlannerUtil.getRandomNumber(randomExecute, planResults, LOG);
			PlannerUtil.printPlan(plan, LOG, uriManager);
			return createSequenceTree(plan, sequence);
		}
		else
			return sequence;
	}

	private Task<AgentTaskInformation> createSequenceTree(PlanResult plan, Sequence sequence) throws URISyntaxException {
		for(Operator operator: plan) {
			AJANOperator ajanOp = ajanOps.get(operator.getFunctor());
			Class clazz = ajanOp.getClazz();
			if (clazz == AbstractActionDefinition.class) {
				sequence.addChild(createActionNode(ajanOp, operator));
			} else if (clazz == AJANGoal.class) {
				sequence.addChild(createProduceGoalNode(ajanOp, operator));
			}
		}
		return sequence;
	}

	private Task<AgentTaskInformation> createActionNode(final AJANOperator ajanOp, final Operator operator) throws URISyntaxException {
		ActionBuilder builder = new ActionBuilder(uriManager);
		builder.setServiceUrl(url + "/actionNodes/" + UUID.randomUUID());
		builder.setServiceDescription(operator);
		builder.setActionInputs(ajanOp, operator);
		builder.setServiceLabel("PlannedAction");
		return builder.build();
	}

	private Task<AgentTaskInformation> createProduceGoalNode(final AJANOperator ajanOp, final Operator operator) throws URISyntaxException {
		GoalProducerBuilder builder = new GoalProducerBuilder(uriManager);
		builder.setProducerUrl(url + "/goalProducerNodes/" + UUID.randomUUID());
		builder.setProducerContent(ajanOp, operator);
		builder.setProducerLabel("PlannedGoalProducer");
		builder.setGoalUri(ajanOps.get(operator.getFunctor()).getUri());
		return builder.build();
	}
}
