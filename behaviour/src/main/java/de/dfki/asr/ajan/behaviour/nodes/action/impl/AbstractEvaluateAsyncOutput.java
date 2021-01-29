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
import de.dfki.asr.ajan.behaviour.events.ModelEventInformation;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.common.SPARQLUtil;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEvaluateAsyncOutput extends AbstractChainStep {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractEvaluateAsyncOutput.class);
	protected String resultGraphName = "";

	@Getter @Setter
	private boolean usesEvents;

	public AbstractEvaluateAsyncOutput(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute (final TaskContext context) {
		Action action = context.get(Action.class);
		AgentTaskInformation info = action.getObject();
		Model result;
		try {
			result = getResultModel(info, action.getId().toString());
			action.setActionResult(result);
		} catch (URISyntaxException ex) {
			return Task.Status.FAILED;
		}
		if (result.isEmpty()) {
			return Task.Status.RUNNING;
		}
		AbstractActionDefinition description = getActionDescription(context);
		if (action.isEvaluate() && !SPARQLUtil.askModel(result, description.getProducible().getSparql())) {
			LOG.error("Asynchronous action outcome model failed to conform to Action specification.");
			return Task.Status.FAILED;
		}
		buildResult(result, context);
		return executeNext(context);
	}

	private void buildResult(final Model outcomeModel, final TaskContext context) {
		ResultModel result = new ResultModel();
		result.addAll(outcomeModel);
		context.put(result);
	}

	private Model getResultModel(final AgentTaskInformation info, final String id) throws URISyntaxException {
		Model resultModel;
		resultModel = readEventOutcome(info, id);
		return resultModel;
	}

	private Model readEventOutcome(final AgentTaskInformation info, final String id) throws URISyntaxException {
		Model model = new LinkedHashModel();
		if (info.getActionInformation().get(id) instanceof ModelEventInformation) {
			ModelEventInformation output = (ModelEventInformation)info.getActionInformation().get(id);
			model = output.getModel();
		}
		return model;
	}

	public abstract AbstractActionDefinition getActionDescription(final TaskContext context);
}
