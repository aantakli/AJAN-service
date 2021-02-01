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

import de.dfki.asr.ajan.behaviour.events.ModelEventInformation;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

@RDFBean("bt:HandleQueueEvent")
public class HandleModelQueueEvent extends HandleModelEvent {

	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("bt:validate")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@Override
	protected boolean checkEventGoalMatching() {
		Object info = this.getObject().getEventInformation();
		if (info instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
			if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				return checkQueueItem(((ModelEventInformation)modelQueue.poll()).getEvent());
			}
		}
		return false;
	}

	private boolean checkQueueItem(final String eventUrl) {
		boolean eventMatching = getEvent() != null && getEvent().toString().equals(eventUrl);
		boolean allEvents = getEvent() != null && getEvent().toString().equals(AJANVocabulary.ALL.toString());
		return getEvent() == null || eventMatching || allEvents;
	}

	@Override
	protected Model getEventModel() {
		constructQuery = query;
		Object info = this.getObject().getEventInformation();
		Model model = new LinkedHashModel();
		if (info instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
			if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				model = constructQuery.getResult(((ModelEventInformation)modelQueue.poll()).getModel());
			}
		}
		return model;
	}

	@Override
	public String toString() {
		return "HandleQueueEvent (" + getLabel() + ")";
	}
}
