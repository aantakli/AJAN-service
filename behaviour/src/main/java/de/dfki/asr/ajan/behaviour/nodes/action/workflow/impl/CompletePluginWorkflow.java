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
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Failure;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Succeed;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.TryFirstThenOnFailure;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.EvaluateAsyncPluginOutput;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.PluginUpdateBeliefbase;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.plugin.RemovePluginAction;

public final class CompletePluginWorkflow extends AbstractActionWorkflow  {
	private TaskStep startOfChain;

	public CompletePluginWorkflow () {
		setupWorkflow();
	}

	@Override
	public void setupWorkflow() {
		// built in reverse order (since following step is needed for constructor)
		TaskStep done			= new Succeed();
		TaskStep updateBeliefs	= new PluginUpdateBeliefbase(done);
		TaskStep updateActions	= new RemovePluginAction(updateBeliefs);
		TaskStep successChain	= new EvaluateAsyncPluginOutput(updateActions);
		TaskStep failureChain	= new RemovePluginAction(new Failure());
		startOfChain			= new TryFirstThenOnFailure(successChain, failureChain);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		return startOfChain.execute(context);
	}
}
