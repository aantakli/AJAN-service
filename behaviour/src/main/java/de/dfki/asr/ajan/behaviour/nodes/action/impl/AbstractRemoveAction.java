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

package de.dfki.asr.ajan.behaviour.nodes.action.impl;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRemoveAction extends AbstractChainStep {
	private TaskContext context;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRemoveAction.class);

	public AbstractRemoveAction(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute (final TaskContext taskContext) {
		context = taskContext;
		Action action = context.get(Action.class);
		return removeAction(action);
	}

	public Task.Status removeAction(final Action action) {
		try {
			action.setRunning(false);
			Map<URI, IConnection> connections = action.getObject().getConnections();
			URI key = getKeyUri(action);
			if (connections.containsKey(key)) {
				IConnection con = action.getObject().getConnections().get(key);
				String id = action.getId().toString();
				removeProcessIDs(action.getObject(), con, id);
				con.getEvent().delete(action.getObject().getBt());
			}
		} catch (URISyntaxException ex) {
			LOG.error("Running action to be removed could not be found.", ex);
			return Task.Status.FAILED;
		}
		return executeNext(getContext());
	}

	protected abstract URI getKeyUri(final Action action) throws URISyntaxException;

	public TaskContext getContext() {
		return context;
	}

	protected void removeProcessIDs(final AgentTaskInformation info, final IConnection con, final String id) {
		con.removeProcessId(id);
		info.getActionInformation().remove(id);
	}
}
