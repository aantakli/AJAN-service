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

package de.dfki.asr.ajan.behaviour;

import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.knowledge.ExecutionBeliefBase;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Data;
import de.dfki.asr.ajan.behaviour.nodes.action.UriGenerator;

@SuppressWarnings("PMD.TooManyFields")
@Data
public class AgentTaskInformation {
	private final BTRoot bt;
	private final AgentBeliefBase agentBeliefs;
	private final ExecutionBeliefBase executionBeliefs;
	private final TripleDataBase behaviorTDB;
	private final TripleDataBase domainTDB;
	private final TripleDataBase serviceTDB;
	private final Map<URI,Event> events;
	private final Map<URI, IConnection> connections;
	private final List<NodeExtension> extensions;
	private final Map<String, Object> actionInformation;
	private UriGenerator uriGenerator;
	private Object eventInformation;
	private final String reportURI;
}
