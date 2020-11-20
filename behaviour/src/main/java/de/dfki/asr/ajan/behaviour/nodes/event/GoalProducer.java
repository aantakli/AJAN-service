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
import de.dfki.asr.ajan.behaviour.exception.EventEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:GoalProducer")
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

	@RDF("ajan:bindings")
	@Getter @Setter
	private Bindings bindings;

	private AJANGoal goal;
	private Status goalStatus = Status.FRESH;
	private static final Logger LOG = LoggerFactory.getLogger(GoalProducer.class);

	@Override
	public Resource getType() {
		return BTVocabulary.GOAL_PRODUCER;
	}

	@Override
	@SuppressWarnings("PMD.ConfusingTernary")
	public LeafStatus executeLeaf() {
		Status exStatus;
		if (goalStatus.equals(Status.FRESH)) {
			goalStatus = Status.RUNNING;
			exStatus = Status.RUNNING;
			try {
				produceGoal();
			} catch (EventEvaluationException | AJANBindingsException | URISyntaxException ex) {
				LOG.info(toString(), ex);
				return new LeafStatus(Status.FAILED, toString() + " FAILED");
			}
		} else if (!goalStatus.equals(Status.RUNNING)) {
			exStatus = goalStatus;
			goalStatus = Status.FRESH;
		} else {
			exStatus = Status.RUNNING;
		}
		printStatus(exStatus);
		return new LeafStatus(exStatus, toString() + " " + exStatus);
	}

	private void printStatus(final Status status) {
		switch (status) {
			case RUNNING:
				LOG.info(toString() + " RUNNING");
				break;
			case SUCCEEDED:
				LOG.info(toString() + " SUCCEEDED");
				break;
			default:
				LOG.info(toString() + " FAILED");
				break;
		}
	}

	private void produceGoal() throws EventEvaluationException, AJANBindingsException, URISyntaxException {
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

	private void createGoalEvent(final AJANGoal goal) throws AJANBindingsException, URISyntaxException {
		List<Bound> bdgs = bindings.getBindings(this.getObject());
		if (checkGoalValues(goal, bdgs)) {
			goal.setEventInformation(this, new GoalInformation(bdgs));
		} else {
			throw new AJANBindingsException("bound values do not fit to goal definition!");
		}
	}

	private boolean checkGoalValues(final AJANGoal goal, final List<Bound> bdgs) {
		return goal.getVariables().stream().map((var) -> {
			boolean found = false;
			for (Bound bnd: bdgs) {
				if (var.getVarName().equals(bnd.getVarName())
								&& var.getDataType().equals(bnd.getDataType())) {
					found = true;
				}
			}
			return found;
		}).noneMatch((found) -> (!found));
	}

	@Override
	public void reportGoalStatus(final Status result) {
		goalStatus = result;
		if (goalStatus.equals(Status.RUNNING)) {
			return;
		}
		try {
			if (checkCondition()) {
				goalStatus = Status.SUCCEEDED;
			} else {
				goalStatus = Status.FAILED;
			}
			ModelEvent event = createEvent();
			event.setEventInformation(new LinkedHashModel());
		} catch (AJANBindingsException | URISyntaxException ex) {
			LOG.error("Error while creating an Event: " + ex);
			goalStatus = Status.FAILED;
		}
	}

	public boolean checkCondition() throws AJANBindingsException, URISyntaxException {
		String condition = goal.getCondition();
		Repository repo = this.getObject().getAgentBeliefs().getInitializedRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			BooleanQuery query = conn.prepareBooleanQuery(condition);
			assignVariables(query);
			return query.evaluate();
		} catch (AJANBindingsException | URISyntaxException ex) {
			return false;
		}
	}

	private void assignVariables(final BooleanQuery query) throws AJANBindingsException, URISyntaxException {
		for (Bound bound: bindings.getBindings(this.getObject())) {
			query.setBinding(bound.getVarName(), getValue(bound));
		}
	}

	private Value getValue(final Bound bound) {
		String valueType = bound.getDataType().toString();
		if (valueType.equals(RDFS.RESOURCE.toString())) {
			return vf.createIRI(bound.getStringValue());
		}
		return vf.createLiteral(bound.getStringValue(), vf.createIRI(valueType));
	}

	@Override
	public boolean goalIsRunning() {
		return goalStatus.equals(Status.RUNNING);
	}

	private ModelEvent createEvent() throws URISyntaxException {
		ModelEvent event = new ModelEvent();
		String eventName = "e_" + label;
		event.setName(eventName);
		event.setUrl(url + "#" + eventName);
		event.register(this.getObject().getBt());
		this.getObject().getEvents().put(new URI(url), event);
		return event;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
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

