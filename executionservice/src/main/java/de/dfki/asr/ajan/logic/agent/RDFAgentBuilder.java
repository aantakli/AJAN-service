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

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.common.*;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import de.dfki.asr.ajan.behaviour.events.*;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import de.dfki.asr.ajan.data.*;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.model.*;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
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
        LOG.info("Creating agent with ID: " + id);
        url = getAgentURI();
        manageTDB = isTDBManagement();
        template = setTemplateFromResource();
	LOG.info("--> Agent Template: " + template.stringValue() + getAgentResolved());
	inferencing = Inferencing.NONE;
        readEndpointsAndEvents();
        extensions = pluginLoader.getNodeExtensions();
        setBehaviorTreesFromResource(template);
	LOG.info("--> Agent Behaviors " + getAgentResolved());
        initialKnowledge = modelManager.getAgentInitKnowledge(vf.createIRI(url), agentResource, initAgentModel, false);
        Credentials auth = readCredentials();
        AgentBeliefBase beliefs = createAgentKnowledge(template, auth);
        if (beliefs == null) {
            return null;
        }
        LOG.info("--> Agent beliefs: " + beliefs.getSparqlEndpoint() + " " + getAgentSet());
        Agent agent = new Agent(url, id, template, initialBehavior, finalBehavior, behaviors, manageTDB, beliefs, events, endpoints, connections);
        LOG.info("Created agent with ID " + id + ": " + url);
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
	LOG.info("--> Agent endpoints " + getAgentResolved());
        Map<URI, Event> agentEvents;
        agentEvents = getAgentEvents(template);
	LOG.info("--> Agent events " + getAgentResolved());
        events = agentEvents;
        endpoints = agentEndpoints.getAgentEndpoints(template, agentTemplateModel, events);
	LOG.info("--> Agent endpoints " + getAgentSet());
    }

    protected AgentBeliefBase createAgentKnowledge(final Resource agentTemplateRsc, final Credentials auth) throws UnauthorizedException, URISyntaxException {
        AgentBeliefBase beliefs = new AgentBeliefBase(tdbManager.createAgentTDB(id,manageTDB,Inferencing.NONE));
        try {
            addAgentInformationToKnowledge(beliefs);
            reportURI = modelManager.getReportURI(initialKnowledge);
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
            removeAgentBeliefs(beliefs);
            return null;
        }
    }

    private void removeAgentBeliefs(final AgentBeliefBase beliefs) {
        LOG.info("Authentication with knowledge base not successful!");
        LOG.info("Please check the credentials!");
        tdbManager.deleteAgentTDB(beliefs);
        LOG.error("Undone agent creation of agent with ID: " + id);
    }

    @SuppressWarnings("PMD.NullAssignment")
    protected void setBehaviorTreesFromResource(final Resource agentTemplateRsc) {
        Resource initialBhv = resourceManager.getInitialBehaviorResource(agentTemplateRsc, agentTemplateModel);
        Resource finalBhv = resourceManager.getFinalBehaviorResource(agentTemplateRsc, agentTemplateModel);
        initialBehavior = null;
        finalBehavior = null;
        if (initialBhv != null) {
            initialBehavior = getSingleRunBehavior(initialBhv, modelManager.getTemplateFromTDB(agentRepo, initialBhv));
            LOG.info("--> Initial Behavior " + getAgentSet());
        }
        if (finalBhv != null) {
            finalBehavior = getSingleRunBehavior(finalBhv, modelManager.getTemplateFromTDB(agentRepo, finalBhv));
            LOG.info("--> Final Behavior " + getAgentSet());
        }
        Iterator<Resource> behaviorResources = resourceManager.getBehaviorResources(agentTemplateRsc, agentTemplateModel);
        Map<Resource, Behavior> behaviorTrees = getBehaviorTrees(behaviorResources);
        behaviors = behaviorTrees;
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

    private Credentials readCredentials() {
        Model controllerModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_TOKEN_CONTROLLER, null);
        String controller = modelManager.getString(controllerModel);
        Model userModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_USER, null);
        String user = modelManager.getString(userModel);
        Model roleModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_ROLE, null);
        String role = modelManager.getString(roleModel);
        Model pswdModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_PASSWORD, null);
        String pswd = modelManager.getString(pswdModel);
        if(controller != null && !controller.equals("")
                && user != null && !user.equals("")
                && role != null && !role.equals("")
                && pswd != null && !pswd.equals("")) {
            return new Credentials(controller, user, role, pswd);
        }
        return null;
    }

    private boolean isTDBManagement() {
        Model nameModel = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_MANAGE_TDB, null);
        return modelManager.getBoolean(nameModel);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected Map<URI, Event> getAgentEvents(final Resource agentTemplate) throws URISyntaxException {
        Map<URI, Event> agentEvents = new ConcurrentHashMap();
        Iterator<Resource> resources = resourceManager.getResources(agentTemplate, agentTemplateModel, AJANVocabulary.AGENT_HAS_EVENT);
        while (resources.hasNext()) {
            Resource resource = resources.next();
            if (!(resource instanceof IRI)) {
                throw new InitializationRDFValidationException("No named Event is used");
            }
            Model resModel = modelManager.getTemplateFromTDB(agentRepo, resource);
            String eventName = modelManager.getLabel(resModel.filter(resource, RDFS.LABEL, null), "No Event-literal for " + RDFS.LABEL + " present");
            Event event = getEvent((IRI) resource, resModel, eventName);
            agentEvents.put(new URI(((IRI) resource).toString()), event);
        }
        return agentEvents;
    }

    @SuppressWarnings("PMD.NcssMethodCount")
    private Event getEvent(final IRI resource, final Model model, final String name) {
        Event event;
        Optional<IRI> type = modelManager.getTypeIRI(model.filter(resource, RDF.TYPE, null), "Event");
        if (type.get().equals(AJANVocabulary.MODEL_EVENT_TYPE)) {
            event = new ModelEvent();
        } else if (type.get().equals(AJANVocabulary.MODEL_QUEUE_EVENT_TYPE)) {
            event = new ModelQueueEvent();
        } else if (type.get().equals(AJANVocabulary.GOAL_TYPE)) {
            event = createGoal(resource);
        } else if (type.get().equals(AJANVocabulary.STRING_EVENT_TYPE)) {
            event = new StringEvent();
        } else if (type.get().equals(AJANVocabulary.MAPPING_EVENT_TYPE)) {
            event = new MappingEvent();
        } else if (type.get().equals(AJANVocabulary.DEFAULT_EVENT_TYPE)) {
            event = new DefaultEvent();
        } else {
            throw new InitializationRDFValidationException("Event type unknown");
        }
        event.setName(name);
        event.setUrl(resource.toString());
        return event;
    }

    private Event createGoal(final IRI resource) {
        AJANGoal goal;
        try (RepositoryConnection conn = agentRepo.getConnection()) {
            RDFBeanManager manager = new BehaviorBeanManager(conn, pluginLoader.getNodeExtensions());
            goal = manager.get(resource, AJANGoal.class);
        }
        catch (RDF4JException | RDFBeanException ex) {
            LOG.error("--> Could not load AJANGoal " + resource.stringValue() + " for " + id + " agent.");
            LOG.error(ex.getMessage());
            throw new InitializationRDFValidationException("Could not load AJANGoal " + resource.stringValue(), ex);
        }
        return goal;
    }

    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.PrematureDeclaration"})
    private Map<Resource, Behavior> getBehaviorTrees(final Iterator<Resource> behaviorRes) {
        Map<Resource, Behavior> bhvs = new ConcurrentHashMap();
        while (behaviorRes.hasNext()) {
            List<Resource> eventResc = new ArrayList();
            Resource resource = behaviorRes.next();
            if (!(resource instanceof IRI)) {
                throw new InitializationRDFValidationException("No BNodes allowed as Behavior Subject " + resource.stringValue());
            }
            Model model = modelManager.getTemplateFromTDB(agentRepo, resource);
            SingleRunBehavior sglBehavior = getSingleRunBehavior(resource, model);
            LOG.info("--> Behavior Tree for " + id + " agent added: " + resource.stringValue());
            boolean clearEKB = getClearEKB(model, resource, AJANVocabulary.BEHAVIOR_HAS_CLEAREKB);
            resourceManager.getResources(resource, model, AJANVocabulary.BEHAVIOR_HAS_TRIGGER).forEachRemaining(eventResc::add);
            bhvs.put((IRI) resource, new Behavior(sglBehavior.getName(), sglBehavior.getResource(), sglBehavior.getBehaviorTree(), clearEKB, eventResc));
        }
        return bhvs;
    }

    private boolean getClearEKB(final Model model, final Resource resource, final IRI predicate) {
        if (!model.filter(resource, predicate, null).objects().isEmpty()) {
            Value clearEKB = model.filter(resource, predicate, null).objects().stream().findFirst().get();
            if(clearEKB.isLiteral()) {
                return Boolean.parseBoolean(clearEKB.stringValue());
            }
        }
        return false;
    }

    private SingleRunBehavior getSingleRunBehavior(final Resource resource, final Model model) {
        String behaviorName = modelManager.getLabel(model.filter(resource, RDFS.LABEL, null), "No Behavior-literal for " + RDFS.LABEL + " present");
        Resource btResc = resourceManager.getResource(model, resource, AJANVocabulary.BEHAVIOR_HAS_BT);
        return new SingleRunBehavior(behaviorName, (IRI) resource, getBT(btResc));
    }

    private BTRoot getBT(final Resource behaviorResource) {
        BTRoot tree;
        try (RepositoryConnection conn = tdbManager.getBehaviorTDB().getInitializedRepository().getConnection()) {
            RDFBeanManager manager = new BehaviorBeanManager(conn, pluginLoader.getNodeExtensions());
            tree = manager.get(behaviorResource, BTRoot.class);
            LOG.info("--> Created Behavior Tree: " + behaviorResource.stringValue() + " for " + id + " agent");
        }
        catch (RDF4JException | RDFBeanException ex) {
            LOG.error("--> Could not load BehaviorTree " + behaviorResource.stringValue() + " for " + id + " agent. A possible reason is a reference to a non-existent resource!");
            LOG.error(ex.getMessage());
            LOG.error("Undone agent creation of agent with ID: " + id);
            throw new InitializationRDFValidationException("Could not load BehaviorTree: ", ex);
        }
        return tree;
    }
}
