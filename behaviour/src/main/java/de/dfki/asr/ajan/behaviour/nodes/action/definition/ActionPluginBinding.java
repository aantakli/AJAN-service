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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.nodes.Action.Communication;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorAskQuery;
import java.util.List;
import java.util.UUID;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;

@RDFBean("actn:PluginBinding")
public interface ActionPluginBinding {
	IRI getType();
	IRI getActionType();
	Communication getCommunication();
	List<ActionVariable> getVariables();
	BehaviorAskQuery getConsumable();
	BehaviorAskQuery getProducible();
	String getLable();
	String getDescription();
	ResultModel run(final InputModel inputModel, final AgentTaskInformation info, final String url);
	@SuppressWarnings("PMD.ExcessiveParameterList")
	void run(final InputModel inputModel, final ModelEvent event, final UUID id, final AgentTaskInformation info, final String url);
	ResultModel abort(final InputModel inputModel);
}
