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

package de.dfki.asr.ajan.behaviour.nodes.action.impl.service;

import de.dfki.asr.ajan.common.AJANVocabulary;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.action.UriGenerator;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;

public class RegisterCompletionUri extends AbstractChainStep {

	private static final Logger LOG = LoggerFactory.getLogger(RegisterCompletionUri.class);

	public RegisterCompletionUri(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute (final TaskContext context) {
		ValueFactory factory = SimpleValueFactory.getInstance();
		Action action = context.get(Action.class);
		IConnection connection = context.get(HttpConnection.class);
		try {
			UriGenerator generator = action.getObject().getUriGenerator();
			IRI completionIRI = factory.createIRI(generator.getCompletionUri(connection.getId(), action.getId()));
			LOG.info("Register Completion URI: " + completionIRI.toString());
			Model inputModel = context.get(InputModel.class);
			inputModel.add(factory.createBNode(), AJANVocabulary.ASYNC_REQUEST_URI, completionIRI);
		} catch (IllegalArgumentException ex) {
			LOG.error("Could not add request URI to model: " + ex.getMessage());
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}
}
