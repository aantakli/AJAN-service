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
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFBean("actn:Action")
public abstract class AbstractActionDefinition {
	@Getter @Setter
	@RDFSubject
	private String url;

	@Getter @Setter
	protected Action action;

	@RDF("actn:communication")
	public abstract URI getCommunication();
	public abstract void setCommunication(final URI uri);

	@RDF("actn:variables")
	public abstract List<ActionVariable> getVariables();
	public abstract void setVariables(final List<ActionVariable> variables);

	@RDF("actn:consumes")
	public abstract Consumable getConsumable();
	public abstract void setConsumable(final Consumable consumable);

	@RDF("actn:produces")
	public abstract Producible getProducible();
	public abstract void setProducible(final Producible producible);

	public TaskStep getWorkflow() throws ActionBindingException {
		if (Action.ASYNCHRONOUS_FRAGMENT.equals(action.getActionDefinition().getCommunication().getFragment())) {
			action.setCommunication(Action.Communication.ASYNCHRONOUS);
			return getAsyncWorkflow();
		}
		getAction().setCommunication(Action.Communication.ASYNCHRONOUS);
		return getSyncWorkflow();
	}

	protected abstract TaskStep getSyncWorkflow() throws ActionBindingException;
	protected abstract TaskStep getAsyncWorkflow() throws ActionBindingException;
	public abstract TaskStep getAbortWorkflow();
}
