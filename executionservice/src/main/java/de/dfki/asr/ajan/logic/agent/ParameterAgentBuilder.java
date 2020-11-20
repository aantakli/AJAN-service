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

package de.dfki.asr.ajan.logic.agent;

import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import de.dfki.asr.ajan.data.AgentTDBManager;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.model.Agent;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class ParameterAgentBuilder extends RDFAgentBuilder {

    public ParameterAgentBuilder(final AgentTDBManager tdbManager, final Repository agentRepo, final AJANPluginLoader apl) {
        super(tdbManager, agentRepo, apl);
    }

    public ParameterAgentBuilder setBaseURI(final URI uri) {
        this.baseURI = uri;
        return this;
    }

    public ParameterAgentBuilder setAgentName(final String name) {
        this.name = name;
        return this;
    }

    public ParameterAgentBuilder setAgentTemplate(final String template) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        this.template = factory.createIRI(template);
        return this;
    }

    public ParameterAgentBuilder setAgentKnowledge(final String strKnowledge, final RDFFormat format) throws IOException {
        InputStream input = new ByteArrayInputStream(strKnowledge.getBytes());
        initialKnowledge = Rio.parse(input, "", format);
        return this;
    }

    @Override
    public Agent build() throws URISyntaxException {
            inferencing = Inferencing.NONE;
            connections = new ConcurrentHashMap<>();
            url = (baseURI.toString() + name);
            agentTemplateModel = modelManager.getTemplateFromTDB(agentRepo, template);
            extensions = pluginLoader.getNodeExtensions();

            Map<URI,Event> agentEvents;
            agentEvents = getAgentEvents(template);
            events = agentEvents;

            AgentEndpoints agentEndpoints = new AgentEndpoints(modelManager, resourceManager, agentRepo);
            endpoints = agentEndpoints.getAgentEndpoints(template, agentTemplateModel, events);

            setBehaviorTreesFromResource(template);
            AgentBeliefBase beliefs = createAgentKnowledge(template);

            return new Agent(url, name, template, initialBehavior, finalBehavior, behaviors, beliefs, events, endpoints, connections);
    }
}
