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
import de.dfki.asr.ajan.behaviour.exception.ActionBindingException;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import java.io.IOException;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;
import java.net.URISyntaxException;

public abstract class AbstractPerformRequest extends AbstractChainStep {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPerformRequest.class);
	public ServiceActionDefinition service;
	public InputModel inputModel;

	public AbstractPerformRequest(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		service = context.get(ServiceActionDefinition.class);
		inputModel = context.get(InputModel.class);
		try {
			ResultModel output = issueRequest(getConnection(context));
			context.put(output);
		} catch (URISyntaxException | IOException | ActionBindingException ex) {
			LOG.error("Failed to perform Action request", ex);
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}

	private ResultModel issueRequest(final IConnection connection) throws IOException, ActionBindingException {
		connection.setPayload(getInput(inputModel));
		ResultModel output = new ResultModel();
		Object response = connection.execute();
		if (response instanceof Model) {
			output.addAll((Model)response); // need to repackage these for typed map to work
		} else {
			throw new ActionBindingException("RDF expected, received something else!");
		}
		return output;
	}

	public abstract String getInput(final Model inputModel);

	public abstract IConnection getConnection(final TaskContext context) throws URISyntaxException, ActionBindingException;
}
