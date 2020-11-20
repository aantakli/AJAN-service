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

package de.dfki.asr.ajan.behaviour.nodes.action.definition;

import de.dfki.asr.ajan.behaviour.exception.ActionBindingException;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Cancelled;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Running;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.AbortPluginWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.AsyncPluginWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.CompletePluginWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.SyncPluginWorkflow;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFBean("actn:PluginAction")
public class PluginActionDefinition extends AbstractActionDefinition {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("actn:communication")
	@Getter @Setter
	private URI communication;

	@RDF("actn:runBinding")
	@Getter @Setter
	private ActionPluginBinding run;

	@RDF("actn:abortBinding")
	@Getter @Setter
	private ActionPluginBinding abort;

	@RDF("actn:variables")
	@Getter @Setter
	private List<ActionVariable> variables;

	@RDF("actn:consumes")
	@Getter @Setter
	private Consumable consumable;

	@RDF("actn:produces")
	@Getter @Setter
	private Producible producible;

	@Override
	protected TaskStep getSyncWorkflow() throws ActionBindingException {
		return new SyncPluginWorkflow();
	}

	@Override
	protected TaskStep getAsyncWorkflow() throws ActionBindingException {
		if (action.isRunning() && action.containesKey()) {
			return new CompletePluginWorkflow();
		} else if (!action.isRunning() && !action.containesKey()) {
			return new AsyncPluginWorkflow();
		}
		return new Running();
	}

	@Override
	public TaskStep getAbortWorkflow() {
		if (action.isRunning()) {
			return new AbortPluginWorkflow();
		}
		return new Cancelled();
	}
}
