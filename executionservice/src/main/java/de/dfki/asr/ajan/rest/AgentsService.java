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

package de.dfki.asr.ajan.rest;

import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.model.Agent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.dfki.asr.ajan.behaviour.nodes.action.UriGenerator;

@Path("/agents")
@Api("List Agents")
@Component
public class AgentsService {
	public static final String NAME = "agentName";
	public static final String AGENT_PATH = "/{" + NAME + "}";

	@Autowired
	private AgentManager agentManager;

	@GET
	@Path("/")
	@Produces({"text/plain","text/turtle","application/ld+json"})
	@ApiOperation("List all Agents")
	public Collection<Agent> getAllAgents() {
		return agentManager.getAllAgents();
	}

	@POST
	@Path("/")
	@Produces("text/turtle")
	@ApiOperation("Add new Agent")
	public Agent createAgent(final Model model) throws URISyntaxException {
            return agentManager.createAgent(model);
	}

	@Path(AGENT_PATH)
	@ApiParam(name = NAME, value = "The name of the Agent.")
	public AgentResource getAgent(@PathParam(NAME) final String agentName) {
            Agent agent = agentManager.getAgent(agentName);
            return new AgentResource(agent, agentManager);
	}

	@RequiredArgsConstructor
	public static class UriGeneratorImpl implements UriGenerator {
		@NonNull
		private final UriBuilder builder;

		@Override
		public String getAgentUri() {
			return builder.clone().build().toString();
		}

		@Override
		public String getActionUri(final UUID actionID) {
			return builder.clone()
					.path(AgentResource.ACTION_PATH)
					.resolveTemplate(AgentResource.ACTION_PARAMETER, actionID.toString())
					.build().toString();
		}

		@Override
		public String getConnectionUri(final UUID connectionID) {
			return builder.clone()
					.path(AgentResource.CONNECTION_PATH)
					.resolveTemplate(AgentResource.CONNECTION_PARAMETER, connectionID.toString())
					.build().toString();
		}

		@Override
		public String getCompletionUri(final UUID connectionID, final UUID actionID) {
			return builder.clone()
					.path(AgentResource.COMPLETION_PATH)
					.resolveTemplate(AgentResource.CONNECTION_PARAMETER, connectionID.toString())
					.resolveTemplate(AgentResource.ACTION_PARAMETER, actionID.toString())
					.build().toString();
		}
	}
}
