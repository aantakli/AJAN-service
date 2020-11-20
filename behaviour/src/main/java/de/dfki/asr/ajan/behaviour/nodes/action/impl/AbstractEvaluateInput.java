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
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.common.SPARQLUtil;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;

public abstract class AbstractEvaluateInput extends AbstractChainStep {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEvaluateInput.class);

	public AbstractEvaluateInput(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		Model inputModel = context.get(InputModel.class);
		Action action = context.get(Action.class);
		AbstractActionDefinition service = getActionDescription(context);
		if (action.isEvaluate() && !SPARQLUtil.askModel(inputModel, service.getConsumable().getSparql())) {
			LOG.error("Input model failed to conform to Action specification.");
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}

	public abstract AbstractActionDefinition getActionDescription(final TaskContext context);
}
