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

import com.fasterxml.jackson.databind.JsonNode;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.DebugMode;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.model.Agent;
import de.dfki.asr.ajan.model.Behavior;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AgentResource {
	private static final String TEXT = "text/plain";
	private static final String JSON = "application/json";
	private static final String XML = "application/xml";
	private static final String XML_OLD = "text/xml";
	private static final String CSV = "text/csv";
        private static final String TRIG = "application/trig";
	private static final String TURTLE = "text/turtle";
	private static final String JSONLD = "application/ld+json";
	public static final String CONNECTION_PARAMETER = "connectionID";
	public static final String ACTION_PARAMETER = "actionID";
	public static final String BEHAVIOR_ID = "behaviorID";
	public static final String ACTION_PATH = "/actions/{" + ACTION_PARAMETER + "}";
	public static final String BEHAVIOR_PATH = "/behaviors/{" + BEHAVIOR_ID + "}";
	public static final String CONNECTION_PATH = "{" + CONNECTION_PARAMETER + "}";
	public static final String COMPLETION_PATH = "{" + CONNECTION_PARAMETER + "}/completeAction/{" + ACTION_PARAMETER + "}";

	private final Agent agent;
	private final AgentManager agentManager;
	private static final Logger LOG = LoggerFactory.getLogger(AgentResource.class);

	public AgentResource(final Agent agent, final AgentManager manager) {
            this.agent = agent;
            this.agentManager = manager;
	}

	@GET
	@Produces({TURTLE,JSONLD})
	@ApiOperation("List a specific agent")
	public Agent getAgent() {
            return agent;
	}

	@POST
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive Object")
	public Agent postAgent(@QueryParam("capability") final String capability) {
            return setEvent(capability, new Object());
	}

	@POST
	@Consumes({TURTLE,TRIG,JSONLD})
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive Turtle, TRIG or LD+JSON")
	public Agent modelPostAgent(@QueryParam("capability") final String capability, final Model model) {
            return setEvent(capability, model);
	}

	@POST
	@Consumes(TEXT)
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive Text")
	public Agent stringPostAgent(@QueryParam("capability") final String capability, final String string) {
            return setEvent(capability, string);
	}

	@POST
	@Consumes(JSON)
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive Json")
	public Agent jsonPostAgent(@QueryParam("capability") final String capability, final JsonNode json) {
            return setEvent(capability, json);
	}

	@POST
	@Consumes({XML, XML_OLD})
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive XML")
	public Agent xmlPostAgent(@QueryParam("capability") final String capability, final Document xml) {
            return setEvent(capability, xml);
	}

	@POST
	@Consumes(CSV)
	@Produces({TURTLE,JSONLD})
	@ApiOperation("Receive CSV")
	public Agent csvPostAgent(@QueryParam("capability") final String capability, final JsonNode json) {
            return setEvent(capability, json);
	}

	private Agent setEvent(final String capability, final Object input) {
            if (capability == null) {
                return null;
            }
            agent.setEndpointEvent(capability, input);
            return agent;
	}

	@DELETE
	public Response deleteAgent() {
            try  {
                agent.stop();
                agentManager.deleteAgent(agent);
            } catch (IllegalArgumentException ex) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.OK).build();
	}

        @Path(BEHAVIOR_PATH)
	@GET
	@Produces({TURTLE,JSONLD})
	@ApiOperation(value = "Get Behavior Information.")
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Model getBehavior(@PathParam(BEHAVIOR_ID) final String behaviorId, @QueryParam("method") final String method, @QueryParam("mode") final String mode) throws URISyntaxException {
            String id = "";
            String behaviorURI = "";
            LOG.info("Received Behavior call: " + behaviorId + ", " + method + ", " + mode);
            for (Entry<Resource,Behavior> entry: agent.getBehaviors().entrySet()) {
                id = new URI(entry.getKey().stringValue()).getFragment();
                if (behaviorId.equals(id) && method != null && mode != null) {
                    behaviorURI = agent.getUrl() + "/behaviors/" + behaviorId;
                    if ("info".equals(method)) {
                        return getBehaviorInfo(entry.getValue(), behaviorURI, mode);
                    } else if ("debug".equals(method)) {
                        return setBehaviorDebug(entry.getValue(), behaviorURI, mode);
                    }
                }
            }
            return new LinkedHashModel();
	}


	private Model getBehaviorInfo(final Behavior behavior, final String uri, final String mode) throws URISyntaxException {
            switch (mode) {
                case "detail":
                    return behavior.getStatus(uri, ModelMode.DETAIL);
                case "normal":
                    return behavior.getStatus(uri, ModelMode.NORMAL);
                default:
                    return new LinkedHashModel();
            }
	}
   
	private Model setBehaviorDebug(final Behavior behavior, final String uri, final String mode) throws URISyntaxException {
            switch (mode) {
                case "resume":
                    behavior.setDebug(uri, DebugMode.RESUME);
                    break;
                case "step":
                    behavior.setDebug(uri, DebugMode.STEP);
                    break;
                case "pause":
                    behavior.setDebug(uri, DebugMode.PAUSE);
                    break;
                default:
                    break;
            }
            return getBehaviorInfo(behavior, mode, uri);
	}

	@Path(COMPLETION_PATH)
	@POST
	@Consumes(TURTLE)
	@ApiOperation(value = "Complete an asynchronous action.")
	public void completeAction(@PathParam(CONNECTION_PARAMETER) final String conId, @PathParam(ACTION_PARAMETER) final String actId, final Model model) {
            LOG.info("Action with ID: " + actId + " finnished");
            agent.getConnections().entrySet().stream().map((entry) -> entry.getValue()).forEach((connection) -> {
                if (conId.equals(connection.getId().toString())) {
                    connection.response(model, actId);
                }
            });
	}
}
