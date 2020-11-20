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
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Cancelled;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.EvaluateServiceInput;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.LoadInputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http.PerformHttpAbortRequest;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.RegisterCompletionUri;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http.RemoveHttpAction;

public final class AbortServiceWorkflow extends AbstractActionWorkflow {
	private TaskStep startOfChain;

	public AbortServiceWorkflow () {
		setupWorkflow();
	}

	@Override
	public void setupWorkflow() {
		TaskStep done				= new Cancelled();
		TaskStep httpRequest		= new PerformHttpAbortRequest(done);
		TaskStep actionRemoved		= new RemoveHttpAction(httpRequest);
		TaskStep checkInput			= new EvaluateServiceInput(actionRemoved);
		TaskStep registerCompletionUri	= new RegisterCompletionUri(checkInput);
		startOfChain				= new LoadInputModel(registerCompletionUri);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		return startOfChain.execute(context);
	}
}
