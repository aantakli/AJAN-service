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

package de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractActionWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.LoadInputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Running;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.CreatePluginActionEvent;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.EvaluatePluginInput;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.ExecuteAsyncPluginAction;

public final class AsyncPluginWorkflow extends AbstractActionWorkflow  {
	private TaskStep startOfChain;

	public AsyncPluginWorkflow () {
		setupWorkflow();
	}

	@Override
	public void setupWorkflow() {
		// built in reverse order (since following step is needed for constructor)
		TaskStep running				= new Running();
		TaskStep runAction				= new ExecuteAsyncPluginAction(running);
		TaskStep createEvent			= new CreatePluginActionEvent(runAction);
		TaskStep checkInput				= new EvaluatePluginInput(createEvent);
		startOfChain					= new LoadInputModel(checkInput);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		return startOfChain.execute(context);
	}
}
