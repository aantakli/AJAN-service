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

package de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import java.net.URI;
import java.net.URISyntaxException;

public class CreatePluginActionEvent extends AbstractChainStep {

	public CreatePluginActionEvent(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		Action action = context.get(Action.class);
		String uri = action.getObject().getUriGenerator().getActionUri(action.getId());
		try {
			ModelEvent event = getEvent(action, uri);
			context.put(event);
		} catch (URISyntaxException ex) {
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}

	private ModelEvent getEvent(final Action action, final String uri) throws URISyntaxException {
		ModelEvent event = new ModelEvent();
		event.setName("e_" + uri);
		event.setUrl(uri);
		event.register(action.getObject().getBt());
		action.getObject().getEvents().put(new URI(event.getUrl()), event);
		return event;
	}
}
