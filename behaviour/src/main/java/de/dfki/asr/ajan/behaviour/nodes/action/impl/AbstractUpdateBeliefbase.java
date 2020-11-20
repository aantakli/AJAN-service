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
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.common.AJANVocabulary;
import org.eclipse.rdf4j.model.Model;

public abstract class AbstractUpdateBeliefbase extends AbstractChainStep {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractUpdateBeliefbase.class);

	public AbstractUpdateBeliefbase(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		Action action = context.get(Action.class);
		Model addModel = context.get(ResultModel.class);
		Model removeModel;
		try {
			AbstractActionDefinition service = getActionDescription(context);
			removeModel = ACTNUtil.getRemoveModel(service, context.get(InputModel.class));
			updateTarget(action, addModel, removeModel);
		} catch (RepositoryException ex) {
			LOG.error("Failed to update BeliefBase", ex);
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}

	private void updateTarget(final Action action, final Model addModel, final Model removeModel) {
		if (action.getTargetBase() != null && action.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			action.getObject().getExecutionBeliefs().update(addModel, removeModel, false);
		} else {
			action.getObject().getAgentBeliefs().update(addModel, removeModel, false);
		}
	}

	public abstract AbstractActionDefinition getActionDescription(final TaskContext context);
}
