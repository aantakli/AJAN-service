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

import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.RegisterRunningServiceAction;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractActionWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.EvaluateServiceInput;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.LoadInputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http.PerformHttpExecuteRequest;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.RegisterCompletionUri;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http.RegisterHttpConnection;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Running;

public final class AsyncServiceWorkflow extends AbstractActionWorkflow  {
	private TaskStep startOfChain;

	public AsyncServiceWorkflow () {
		setupWorkflow();
	}

	@Override
	public void setupWorkflow() {
		// built in reverse order (since following step is needed for constructor)
		TaskStep running				= new Running();
		TaskStep registerAction			= new RegisterRunningServiceAction(running);
		TaskStep httpRequest			= new PerformHttpExecuteRequest(registerAction);
		TaskStep checkInput				= new EvaluateServiceInput(httpRequest);
		TaskStep registerCompletionUri	= new RegisterCompletionUri(checkInput);
		TaskStep registerConnection		= new RegisterHttpConnection(registerCompletionUri);
		startOfChain					= new LoadInputModel(registerConnection);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		return startOfChain.execute(context);
	}
}
