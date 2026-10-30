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

import de.dfki.asr.ajan.common.*;
import de.dfki.asr.ajan.behaviour.events.*;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import de.dfki.asr.ajan.data.*;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.model.*;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CyclomaticComplexity"})
public class RDFAgentBuilder extends AgentBuilder {

    protected final AJANPluginLoader pluginLoader;
    protected final AgentModelManager modelManager;
    protected final AgentResourceManager resourceManager;

    protected Model initAgentModel;
    protected Resource agentResource;

    private static final Logger LOG = LoggerFactory.getLogger(RDFAgentBuilder.class);

    public RDFAgentBuilder(final AgentTDBManager tdbManager, final Repository agentRepo, final AJANPluginLoader apl) {
        super(tdbManager);
        this.agentRepo = agentRepo;
        this.pluginLoader = apl;
        this.modelManager = new AgentModelManager(agentRepo);
        this.resourceManager = new AgentResourceManager();
    }

    public RDFAgentBuilder setInitModel(final Model initAgentModel) {
        this.initAgentModel = initAgentModel;
        return this;
    }

    public RDFAgentBuilder setBaseURI(final URI uri) {
        this.baseURI = uri;
        return this;
    }

    public RDFAgentBuilder setAgentResource(final Resource agentResource) {
        this.agentResource = agentResource;
        return this;
    }

    @Override
    public Agent build() throws UnauthorizedException, URISyntaxException {
        connections = new ConcurrentHashMap<>();
        id = getIdFromModel();
        LOG.info("Creating agent with ID: {}", id);
        url = getAgentURI();
        template = setTemplateFromResource();
        LOG.info("--> Agent Template: {}{}", template.stringValue(), getAgentResolved());
	    inferencing = Inferencing.NONE;
        readEndpointsAndEvents();
        extensions = pluginLoader.getNodeExtensions();
        setBehaviorTreesFromResource(template);
        LOG.info("--> Agent Behaviors {}", getAgentResolved());
        initialKnowledge = modelManager.getAgentInitKnowledge(vf.createIRI(url), agentResource, initAgentModel, false);
        overwrite = isManagedAgentTDB();
        Credentials auth = readCredentials();
        AgentBeliefBase beliefs = createAgentKnowledge(template, auth);
        if (beliefs == null) {
            return null;
        }
        LOG.info("--> Agent beliefs: {} {}", beliefs.getSparqlEndpoint(), getAgentSet());
        Agent agent = new Agent(url, id, template, initialBehavior, finalBehavior, behaviors, overwrite, beliefs, events, endpoints, connections, reportURI);
        LOG.info("Created agent with ID {}: {}", id, url);
        return agent;
    }

    protected String getAgentResolved() {
        return "for " + id + " agent resolved!";
    }

    protected String getAgentSet() {
        return "for " + id + " agent set!";
    }

    protected void readEndpointsAndEvents() throws URISyntaxException {
	    AgentEndpoints agentEndpoints = new AgentEndpoints(modelManager, resourceManager, agentRepo);
        LOG.info("--> Agent endpoints {}", getAgentResolved());
        Map<URI, Event> agentEvents;
        agentEvents = getAgentEvents(template);
        LOG.info("--> Agent events {}", getAgentResolved());
        events = agentEvents;
        endpoints = agentEndpoints.getAgentEndpoints(template, agentTemplateModel, events);
        LOG.info("--> Agent endpoints {}", getAgentSet());
    }

    private boolean isManagedAgentTDB() {
        managedTDB = modelManager.getManagedTDB(initAgentModel, agentResource);
        return managedTDB == null || managedTDB.isEmpty();
    }

    private Credentials readCredentials() {
        CredentialsBuilder auth = new CredentialsBuilder();
        Model nameModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_MANAGED_TDB, null);
        Optional<Resource> managedResource = Models.objectResource(nameModel);
        if (managedResource.isPresent()) {
            Resource managedRes = managedResource.get();
            modelManager.setCredentials(auth, initAgentModel, managedRes);
            modelManager.setTokens(auth, initAgentModel, managedRes);
            return auth.build();
        }
        return null;
    }

    protected AgentBeliefBase createAgentKnowledge(final Resource agentTemplateRsc, final Credentials auth) throws UnauthorizedException, URISyntaxException {
        AgentBeliefBase beliefs = new AgentBeliefBase(tdbManager.createAgentTDB(id,managedTDB,Inferencing.NONE,auth));
        try {
            addAgentInformationToKnowledge(beliefs);
            reportURI = modelManager.getAnyURI(initialKnowledge, AJANVocabulary.AGENT_HAS_REPORT_URI);
            beliefs.update(initialKnowledge);
            if (initialBehavior != null) {
                configureBehaviorTree(beliefs, initialBehavior.getBehaviorTree(), initialBehavior.getResource(), true);
            }
            if (finalBehavior != null) {
                configureBehaviorTree(beliefs, finalBehavior.getBehaviorTree(), initialBehavior.getResource(), true);
            }
            configureBehaviorTrees(beliefs);
            return beliefs;
        } catch (UnauthorizedException ex) {
            LOG.error("Authentication with knowledge base not successful!", ex);
            removeAgentBeliefs(beliefs);
            return null;
        }
    }

    private void removeAgentBeliefs(final AgentBeliefBase beliefs) {
        LOG.info("Authentication with knowledge base not successful!");
        LOG.info("Please check the credentials!");
        tdbManager.deleteAgentTDB(beliefs);
        LOG.error("Undone agent creation of agent with ID: {}", id);
    }

    @SuppressWarnings("PMD.NullAssignment")
    protected void setBehaviorTreesFromResource(final Resource agentTemplateRsc) {
        Resource initialBhv = resourceManager.getInitialBehaviorResource(agentTemplateRsc, agentTemplateModel);
        Resource finalBhv = resourceManager.getFinalBehaviorResource(agentTemplateRsc, agentTemplateModel);
        initialBehavior = null;
        finalBehavior = null;
        if (initialBhv != null) {
            initialBehavior = behaviorLoader().getSingleRunBehavior(initialBhv, modelManager.getTemplateFromTDB(agentRepo, initialBhv), id);
            LOG.info("--> Initial Behavior {}", getAgentSet());
        }
        if (finalBhv != null) {
            finalBehavior = behaviorLoader().getSingleRunBehavior(finalBhv, modelManager.getTemplateFromTDB(agentRepo, finalBhv), id);
            LOG.info("--> Final Behavior {}", getAgentSet());
        }
        Iterator<Resource> behaviorResources = resourceManager.getBehaviorResources(agentTemplateRsc, agentTemplateModel);
        behaviors = behaviorLoader().getBehaviorTrees(behaviorResources, agentRepo, id);
    }

    private Resource setTemplateFromResource() {
        Resource agentTemplateRsc = resourceManager.getResource(initAgentModel, agentResource, AJANVocabulary.AGENT_HAS_TEMPLATE);
        agentTemplateModel = modelManager.getTemplateFromTDB(agentRepo, agentTemplateRsc);
        setTemplate(agentTemplateRsc);
        return agentTemplateRsc;
    }

    private String getIdFromModel() {
        Model idModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_ID, null);
        String agentID = modelManager.getLabel(idModel, "No AgentTemplate-literal for " + AJANVocabulary.AGENT_HAS_ID + " defined");
        id = agentID;
        return agentID;
    }

    protected Map<URI, Event> getAgentEvents(final Resource agentTemplate) throws URISyntaxException {
        return new AgentEventLoader(modelManager, resourceManager, agentRepo, pluginLoader)
                .getAgentEvents(agentTemplate, agentTemplateModel, id);
    }

    private AgentBehaviorLoader behaviorLoader() {
        return new AgentBehaviorLoader(modelManager, resourceManager, tdbManager, pluginLoader);
    }
}
