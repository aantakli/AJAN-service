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

package de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http;

import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.AbstractRemoveAction;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;
import java.net.URI;
import java.net.URISyntaxException;
public class RemoveHttpAction extends AbstractRemoveAction {
	public RemoveHttpAction(final TaskStep next) {
		super(next);
	}

	@Override
	protected URI getKeyUri(final Action action) throws URISyntaxException {
		ServiceActionDefinition description = (ServiceActionDefinition)action.getActionDefinition();
		return new URI(description.getRun().getUrl());
	}
}
