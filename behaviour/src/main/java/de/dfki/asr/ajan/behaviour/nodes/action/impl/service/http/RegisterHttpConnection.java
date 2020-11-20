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

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.LoggerFactory;

public class RegisterHttpConnection extends AbstractChainStep {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RegisterHttpConnection.class);
	public ServiceActionDefinition service;

	public RegisterHttpConnection(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		service = context.get(ServiceActionDefinition.class);
		IConnection connection;
		try {
			connection = getConnection(context);
			context.put(connection);
		} catch (URISyntaxException ex) {
			LOG.error("Failed to establish Connection", ex);
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}

	public IConnection getConnection(final TaskContext context) throws URISyntaxException {
		Action action = context.get(Action.class);
		URI uri = new URI(service.getRun().getUrl());
		if (action.getObject().getConnections().containsKey(uri)) {
			IConnection connection = action.getObject().getConnections().get(uri);
			if (!connection.getEvent().isRegistered(action.getObject().getBt())) {
				connection.getEvent().register(action.getObject().getBt());
			}
			return connection;
		}
		return createConnection(action, uri);
	};

	private IConnection createConnection(final Action action, final URI uri) throws URISyntaxException {
		HttpConnection connection = new HttpConnection(service.getRun());
		connection.setEvent(getEvent(action, uri));
		action.getObject().getConnections().put(uri, connection);
		return connection;
	}

	private ModelEvent getEvent(final Action action, final URI uri) throws URISyntaxException {
		ModelEvent event = createModelEvent(action.getObject().getBt(), uri);
		action.getObject().getEvents().put(new URI(event.getUrl()), event);
		return event;
	}

	private ModelEvent createModelEvent(final BTRoot bt, final URI uri) {
		ModelEvent event = new ModelEvent();
		event.setName("e_" + uri.toString());
		event.setUrl(uri.toString());
		event.register(bt);
		return event;
	}
}
