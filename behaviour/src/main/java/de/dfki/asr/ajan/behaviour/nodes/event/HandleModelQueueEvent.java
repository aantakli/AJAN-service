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

import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
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
	protected Model getEventModel() {
		constructQuery = query;
		Model model = new LinkedHashModel();
		if (this.getObject().getEventInformation() instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
			if (modelQueue.peek() != null && modelQueue.peek() instanceof Model) {
				model = constructQuery.getResult((Model)modelQueue.poll());
			}
		}
		return model;
	}

	@Override
	public String toString() {
		return "HandleQueueEvent (" + getLabel() + ")";
	}
}
