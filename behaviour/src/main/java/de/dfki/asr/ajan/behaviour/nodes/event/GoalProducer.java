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

package de.dfki.asr.ajan.behaviour.nodes.event;

import de.dfki.asr.ajan.behaviour.events.*;
import de.dfki.asr.ajan.behaviour.exception.AJANBindingsException;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.exception.EventEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.SPARQLUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RDFBean("bt:GoalProducer")
@SuppressWarnings("PMD.ExcessiveImports")
public class GoalProducer extends AbstractTDBLeafTask implements Producer {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ajan:goal")
	@Getter @Setter
	private URI goalURI;

	@RDF("bt:content")
	@Getter @Setter
	private BehaviorConstructQuery content;

	private AJANGoal goal;
	private List<BindingSet> bindings;
	private Status goalStatus = Status.FRESH;

	@Override
	public Resource getType() {
		return BTVocabulary.GOAL_PRODUCER;
	}

	@Override
	@SuppressWarnings("PMD.ConfusingTernary")
	public NodeStatus executeLeaf() {
		Status exStatus;
		if (goalStatus.equals(Status.FRESH)) {
			goalStatus = Status.RUNNING;
			exStatus = Status.RUNNING;
			try {
				produceGoal();
			} catch (EventEvaluationException | AJANBindingsException | URISyntaxException | ConditionEvaluationException ex) {
				return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED", ex);
			}
		} else if (!goalStatus.equals(Status.RUNNING)) {
			exStatus = goalStatus;
			goalStatus = Status.FRESH;
		} else {
			exStatus = Status.RUNNING;
		}
		printStatus(exStatus);
		return new NodeStatus(exStatus, this.getObject().getLogger(), this.getClass(), toString() + " " + exStatus);
	}

	private void printStatus(final Status status) {
		switch (status) {
			case RUNNING:
				this.getObject().getLogger().info(this.getClass(), "Status (RUNNING)");
				break;
			case SUCCEEDED:
				this.getObject().getLogger().info(this.getClass(), "Status (SUCCEEDED)");
				break;
			default:
				this.getObject().getLogger().info(this.getClass(), "Status (FAILED)");
				break;
		}
	}

	private void produceGoal() throws EventEvaluationException, AJANBindingsException, URISyntaxException, ConditionEvaluationException {
		Map<URI,Event> events = this.getObject().getEvents();
		if (events.containsKey(goalURI)) {
			if (events.get(goalURI) instanceof AJANGoal) {
				goal = (AJANGoal)events.get(goalURI);
				createGoalEvent(goal);
			} else {
				throw new EventEvaluationException("Event is no AJANGoal");
			}
		} else {
			throw new EventEvaluationException("No such AJANGoal defined");
		}
	}

	private void createGoalEvent(final AJANGoal goal) throws AJANBindingsException, URISyntaxException, ConditionEvaluationException {
		Model inputModel = getModel();
		if (!inputModel.isEmpty() && checkConsumables(inputModel)) {
			goal.setEventInformation(this, new GoalInformation(inputModel));
		} else {
			throw new AJANBindingsException("No Input Model specified or Input Model dose not fit to the Goal Consumable!");
		}
	}

	private Model getModel() throws ConditionEvaluationException {
		try {
			Repository repo = BTUtil.getInitializedRepository(getObject(), content.getOriginBase());
			return content.getResult(repo);
		} catch (URISyntaxException | QueryEvaluationException ex) {
			throw new ConditionEvaluationException(ex);
		}
	}

	@Override
	public void reportGoalStatus(final Status result) {
		goalStatus = result;
		if (goalStatus.equals(Status.RUNNING)) {
			return;
		}
		try {
			if (checkProducibles()) {
				goalStatus = Status.SUCCEEDED;
			} else {
				goalStatus = Status.FAILED;
			}
			ModelEvent event = createEvent();
			event.setEventInformation(new LinkedHashModel());
		} catch (AJANBindingsException | URISyntaxException ex) {
			this.getObject().getLogger().info(this.getClass(), "Error while creating an Event: " + ex);
			goalStatus = Status.FAILED;
		}
	}

	public boolean checkConsumables(final Model inputModel) throws AJANBindingsException, URISyntaxException {
		String condition = goal.getConsumable().getSparql();
		boolean check = SPARQLUtil.askModel(inputModel, condition);
		assignVariables(inputModel, condition);
		return check;
	}

	public boolean checkProducibles() throws AJANBindingsException, URISyntaxException {
		String effect = goal.getProducible().getSparql();
		Repository repo = this.getObject().getAgentBeliefs().getInitializedRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			BooleanQuery askQuery = conn.prepareBooleanQuery(effect);
			for (Binding binding: bindings.get(0)) {
				askQuery.setBinding(binding.getName(), binding.getValue());
			}
			return askQuery.evaluate();
		}
	}

	private void assignVariables(final Model inputModel, final String askQuery) throws AJANBindingsException, URISyntaxException {
		List<String> variables = new ArrayList();
		for (ActionVariable variable: goal.getVariables()) {
			variables.add(variable.getVarName());
		}
		this.bindings = SPARQLUtil.assignVariables(inputModel, askQuery, variables);
	}

	@Override
	public boolean goalIsRunning() {
		return goalStatus.equals(Status.RUNNING);
	}

	private ModelEvent createEvent() throws URISyntaxException {
		ModelEvent event = new ModelEvent();
		String eventName = "g_" + label;
		event.setName(eventName);
		event.setUrl(url + "#" + eventName);
		event.register(this.getObject().getBt());
		this.getObject().getEvents().put(new URI(url), event);
		return event;
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GoalProducer (" + label + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.UNCLEAR;
	}
}

