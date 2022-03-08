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

import de.dfki.asr.ajan.behaviour.nodes.event.GoalProducer;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import graphplan.domain.Operator;
import java.net.URI;
import java.net.URISyntaxException;

public final class GoalProducerBuilder {

	private String url;
	private URI goalUri;
	private String label;
	BehaviorConstructQuery content;

	private final URIManager uriManager;

	public GoalProducerBuilder(final URIManager uriManager) throws URISyntaxException {
		this.uriManager = uriManager;
	}

	public GoalProducerBuilder setProducerUrl(String url) {
		this.url = url;
		return this;
	}

	public GoalProducerBuilder setGoalUri(URI uri) throws URISyntaxException {
		this.goalUri = uri;
		return this;
	}

	public GoalProducerBuilder setProducerLabel(String label) {
		this.label = label;
		return this;
	}

	public GoalProducerBuilder setProducerContent(final AJANOperator ajanOp, final Operator operator) throws URISyntaxException {
		this.content = PlannerUtil.getNodeQuery(operator, ajanOp, uriManager);
		return this;
	}

	public GoalProducer build() {
		GoalProducer goal = new GoalProducer();
		goal.setLabel(label);
        goal.setUrl(url);
		goal.setGoalURI(goalUri);
		goal.setContent(content);
		return goal;
	}
}
