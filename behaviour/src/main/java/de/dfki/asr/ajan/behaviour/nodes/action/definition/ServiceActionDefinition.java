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
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.AbortServiceWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.AsyncServiceWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.CompleteHttpWsServiceWorkflow;
import de.dfki.asr.ajan.behaviour.nodes.action.workflow.impl.SyncServiceWorkflow;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFBean("actn:ServiceAction")
public class ServiceActionDefinition extends AbstractActionDefinition {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("actn:communication")
	@Getter @Setter
	private URI communication;

	@RDF("actn:runBinding")
	@Getter @Setter
	private HttpBinding run;

	@RDF("actn:abortBinding")
	@Getter @Setter
	private HttpBinding abort;

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
	@SuppressWarnings("PMD.ConfusingTernary")
	protected TaskStep getAsyncWorkflow() throws ActionBindingException {
		if (getRun() == null) {
			throw new ActionBindingException("No Binding defined!");
		}
		if (!getAction().containesKey() && !action.isRunning()) {
			return getAsyncStartWorkflow();
		} else if (getAction().containesKey() && action.isRunning()) {
			return new CompleteHttpWsServiceWorkflow();
		} else {
			return new Running();
		}
	}

	private TaskStep getAsyncStartWorkflow() {
		return new AsyncServiceWorkflow();
	}

	@Override
	protected TaskStep getSyncWorkflow() throws ActionBindingException {
		return new SyncServiceWorkflow();
	}

	@Override
	public TaskStep getAbortWorkflow() {
		if (action.isRunning()) {
			return new AbortServiceWorkflow();
		}
		return new Cancelled();
	}
}
