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

import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.events.ModelQueueEvent;
import de.dfki.asr.ajan.behaviour.events.Producer;
import de.dfki.asr.ajan.behaviour.exception.ConditionSimulationException;
import de.dfki.asr.ajan.behaviour.exception.EventSimulationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;

@RDFBean("bt:EventProducer")
public class EventProducer extends AbstractTDBLeafTask implements Producer {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ajan:event")
	@Getter @Setter
	private URI eventURI;

	@RDF("bt:content")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@Override
	public Resource getType() {
		return BTVocabulary.EVENT_PRODUCER;
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			setModelEvent();
			return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
		} catch (ConditionSimulationException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to condition evaluation error", ex);
		} catch (EventSimulationException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED", ex);
		}
	}

	private void setModelEvent() throws ConditionSimulationException, EventSimulationException {
		Map<URI,Event> events = this.getObject().getEvents();
		if (events.containsKey(eventURI)) {
			if (events.get(eventURI) instanceof ModelEvent || events.get(eventURI) instanceof ModelQueueEvent) {
				Event event = events.get(eventURI);
				event.setEventInformation(getModel());
			} else {
				throw new EventSimulationException("Event is no ModelEvent");
			}
		} else {
			throw new EventSimulationException("No such Event defined");
		}
	}

	private Model getModel() throws ConditionSimulationException {
		try {
			Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			return query.getResult(repo);
		} catch (URISyntaxException | QueryEvaluationException ex) {
			throw new ConditionSimulationException(ex);
		}
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "EventProducer (" + label + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			query.setResultModel(getInstance(root.getInstance()), BTVocabulary.CONTENT_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}

	@Override
	public Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return Result.SUCCESS;
	}

	@Override
	public boolean goalIsRunning() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void reportGoalStatus(final Status status) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}

