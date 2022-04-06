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

import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import graphplan.domain.Operator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public final class ActionBuilder {

	private String url;
	private URI actionUri;
	private String label;
	List<BehaviorQuery> inputs;

	private final URIManager uriManager;

	public ActionBuilder(final URIManager uriManager) throws URISyntaxException {
		this.uriManager = uriManager;
		this.inputs = new ArrayList();
	}

	public ActionBuilder setServiceUrl(String url) {
		this.url = url;
		return this;
	}

	public ActionBuilder setServiceLabel(String label) {
		this.label = label;
		return this;
	}

	public ActionBuilder setServiceDescription(Operator operator) throws URISyntaxException {
		String uri = uriManager.getURIFromHash(operator.getFunctor());
		actionUri = new URI(uri);
		return this;
	}

	public ActionBuilder setActionInputs(final AJANOperator ajanOp, final Operator operator) throws URISyntaxException {
		inputs.add(PlannerUtil.getNodeQuery(operator, ajanOp, uriManager));
		return this;
	}

	public Action build() {
		Action action = new Action();
		action.setUrl(url);
		action.setDefinition(actionUri);
		action.setInputs(inputs);
		action.setLabel(label);
		return action;
	}
}
