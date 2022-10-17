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

import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.model.*;
import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.Debug;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import de.dfki.asr.ajan.data.*;
import de.dfki.asr.ajan.knowledge.ExecutionBeliefBase;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TooManyFields","PMD.ExcessiveImports"})
public class AgentBuilder {

    protected final AgentTDBManager tdbManager;

    @Getter @Setter
    protected String id;
    @Getter @Setter
    protected Repository agentRepo;
    @Getter @Setter
    protected Resource template;
    @Getter @Setter
    protected Model agentTemplateModel;
    @Getter @Setter
    protected String url;
    @Getter @Setter
    protected SingleRunBehavior initialBehavior;
    @Getter @Setter
    protected SingleRunBehavior finalBehavior;
    @Getter @Setter
    protected Map<Resource, Behavior> behaviors;
    @Getter @Setter
    protected boolean manageTDB;
    @Getter @Setter
    protected Map<URI, Event> events;
    @Getter @Setter
    protected Map<String, Endpoint> endpoints;
    @Getter @Setter
    protected Model initialKnowledge;
    @Getter @Setter
    protected List<NodeExtension> extensions;
    @Getter @Setter
    protected Map<URI, IConnection> connections;
    @Getter @Setter
    protected Inferencing inferencing;
    @Getter @Setter
    protected String reportURI;

    protected URI baseURI;
    protected final ValueFactory vf = SimpleValueFactory.getInstance();
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AgentBuilder.class);

    public AgentBuilder(final AgentTDBManager manager) {
        tdbManager = manager;
    }

    public Agent build() throws UnauthorizedException, URISyntaxException {
        LOG.info("Creating agent with ID: " + id);
        url = getAgentURI();
        AgentBeliefBase beliefs = new AgentBeliefBase(tdbManager.createAgentTDB(id,manageTDB,inferencing,null));
        addAgentInformationToKnowledge(beliefs);
        beliefs.update(initialKnowledge);
        connections = new ConcurrentHashMap<>();
        configureBehaviorTree(beliefs, initialBehavior.getBehaviorTree(), initialBehavior.getResource(), true);
        configureBehaviorTree(beliefs, finalBehavior.getBehaviorTree(), finalBehavior.getResource(), true);
        configureBehaviorTrees(beliefs);
        Agent agent = new Agent(url, id, template, initialBehavior, finalBehavior, behaviors, manageTDB, beliefs, events, endpoints, connections);
        LOG.info("Agent with ID " + id + " is created: " + agent.getUrl());
        return agent;
    }
    protected String getAgentURI() {
        String ajanURI = baseURI.toString();
        StringBuilder builder = new StringBuilder();
        builder.append(ajanURI);
        if (!ajanURI.substring(ajanURI.length() - 1).equals("/")) {
            builder.append('/');
        }
        builder.append(id);
        return builder.toString();
    }

    protected void addAgentInformationToKnowledge(final AgentBeliefBase beliefs) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        Resource agent = factory.createIRI(url);
        initialKnowledge.add(factory.createStatement(agent, RDF.TYPE, AJANVocabulary.AGENT_TYPE));
        initialKnowledge.add(factory.createStatement(agent, RDF.TYPE, AJANVocabulary.AGENT_THIS));
        initialKnowledge.add(factory.createStatement(agent, AJANVocabulary.AGENT_HAS_ROOT, factory.createLiteral(baseURI.toString())));
        initialKnowledge.add(factory.createStatement(agent, AJANVocabulary.AGENT_HAS_ID, factory.createLiteral(id)));
        initialKnowledge.add(factory.createStatement(agent, AJANVocabulary.AGENT_HAS_KNOWLEDGE, factory.createLiteral(beliefs.getSparqlUpdateEndpoint().toString())));
        initialKnowledge.add(factory.createStatement(agent, AJANVocabulary.AGENT_HAS_TEMPLATE, template));
        addTemplateKnowledge();
    }

    protected void addTemplateKnowledge() {
        ValueFactory factory = SimpleValueFactory.getInstance();
        AgentModelManager modelMgr = new AgentModelManager(agentRepo);
        Model tmplModel = modelMgr.getAgentInitKnowledge(factory.createIRI(url), template, agentTemplateModel, true);
        Iterator<Statement> itr = tmplModel.iterator();
        while (itr.hasNext()) {
            initialKnowledge.add(itr.next());
        }
    }

    protected void configureBehaviorTrees(final AgentBeliefBase beliefs) {
        behaviors.entrySet().stream().forEach((Map.Entry<Resource, Behavior> behavior) -> {
            BTRoot bt = behavior.getValue().getBehaviorTree();
            try {
                configureBehaviorTree(beliefs, bt, behavior.getKey(), behavior.getValue().isClearEKB());
            }
            catch (URISyntaxException ex) {
                Logger.getLogger(AgentBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
            behavior.getValue().getEvents().forEach((res) -> {
                try {
                        events.get(new URI(res.toString())).register(bt);
                } catch (URISyntaxException ex) {
                        Logger.getLogger(AgentBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        });
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    protected void configureBehaviorTree(final AgentBeliefBase beliefs, final BTRoot bt, final Resource behaviorIRI, final boolean clearEKB) throws URISyntaxException {
        if (beliefs != null && bt != null) {
            bt.setInstance(vf.createIRI(getBTInstance(behaviorIRI)));
            bt.setObject(new AgentTaskInformation(
                new AJANLogger(id),
                bt,
                clearEKB,
                beliefs,
                new ExecutionBeliefBase(inferencing),
                tdbManager.getAgentTemplatesTDB(),
                tdbManager.getBehaviorTDB(),
                tdbManager.getDomainTDB(),
                tdbManager.getServiceTDB(),
                events,
                connections,
                extensions,
                new LinkedHashMap(),
                reportURI,
                createDebug(behaviorIRI)
            ));
        }
    }

    protected Debug createDebug(final Resource bt) throws URISyntaxException {
        Debug debug = new Debug();
        debug.setAgentURI(url);
        return debug;
    }

    protected String getBTInstance(final Resource bt) throws URISyntaxException {
        String behaviorId = new URI(bt.stringValue()).getFragment();
        return url + "/behaviors/" + behaviorId;
    }
}
