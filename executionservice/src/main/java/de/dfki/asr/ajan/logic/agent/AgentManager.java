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

import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.AJANDataBase;
import static de.dfki.asr.ajan.AJANDataBase.Store.ACTION_SERVICE;
import static de.dfki.asr.ajan.AJANDataBase.Store.AGENT_TEMPLATE;
import static de.dfki.asr.ajan.AJANDataBase.Store.BEHAVIOR;
import static de.dfki.asr.ajan.AJANDataBase.Store.DOMAIN;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.common.TripleStoreManager;
import de.dfki.asr.ajan.data.AgentTDBManager;
import de.dfki.asr.ajan.exceptions.AgentNotFoundException;
import de.dfki.asr.ajan.exceptions.InitializationNotAllowedException;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import de.dfki.asr.ajan.model.Agent;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.rest.AgentsService;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveParameterList", "PMD.TooManyFields"})
public class AgentManager {
	private final TripleStoreManager tripleStoreManager;
	private final TripleDataBase agents;
	private final TripleDataBase behaviors;
	private final TripleDataBase domain;
	private final TripleDataBase services;
	private final Map<String, Agent> agentMap;
	private RDFAgentBuilder agentRDFBuilder;
        private ParameterAgentBuilder agentParamBuilder;
	private final AgentTDBManager tdbManager;

        private URI baseURI;
        private final static String PATH = "/ajan/agents/";

	@Autowired
	private AJANPluginLoader pluginLoader;

        @Autowired
        private Environment environment;

        private static final Logger LOG = LoggerFactory.getLogger(AgentManager.class);

	@Autowired
	@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.NcssConstructorCount"})
	public AgentManager(final TripleStoreManager manager,
					final @AJANDataBase(AGENT_TEMPLATE) TripleDataBase agentDatabase,
					final @AJANDataBase(BEHAVIOR) TripleDataBase behaviorDatabase,
					final @AJANDataBase(DOMAIN) TripleDataBase domainDatabase,
					final @AJANDataBase(ACTION_SERVICE) TripleDataBase serviceDatabase) {
		tripleStoreManager = manager;
		agents = agentDatabase;
		behaviors = behaviorDatabase;
		domain = domainDatabase;
		services = serviceDatabase;
		agentMap = new HashMap<>();
		tdbManager = new AgentTDBManager(manager);
	}

	@PostConstruct
	public void afterPropertiesSet() throws URISyntaxException, UnknownHostException {
		Repository agentRepo = agents.getInitializedRepository();
		agentRDFBuilder = new RDFAgentBuilder(tdbManager, agentRepo, pluginLoader);
                agentParamBuilder = new ParameterAgentBuilder(tdbManager, agentRepo, pluginLoader);
		tdbManager.setBehaviorTDB(behaviors);
		tdbManager.setDomainTDB(domain);
		tdbManager.setServiceTDB(services);
	}

        public void setBaseURI(final String publicHostName, final boolean usePort) throws URISyntaxException, UnknownHostException {
            String port = environment.getProperty("server.port");
            String host = InetAddress.getLoopbackAddress().getHostName();

            LOG.info("HostCanonicalName: " + InetAddress.getLocalHost().getCanonicalHostName());

            LOG.info("HostLocalAddress: " + InetAddress.getLocalHost().getHostAddress());
            LOG.info("HostLocalName: " + InetAddress.getLocalHost().getHostName());

            LOG.info("HostLoopbackAddress: " + InetAddress.getLoopbackAddress().getHostAddress());
            LOG.info("HostLoopbackName: " + host);
            URIBuilder builder = new URIBuilder();
            if (usePort) {
                baseURI = builder.setScheme("http").setHost(publicHostName).setPort(Integer.parseInt(port)).setPath(PATH).build();
            } else {
                baseURI = builder.setScheme("http").setHost(publicHostName).setPath(PATH).build();
            }
            LOG.info("AJAN Base URI: " + baseURI);
        }

	public Agent createAgent(final Model initAgentRDF) throws URISyntaxException {
		Set<Resource> subjects = initAgentRDF.filter(null, RDF.TYPE, AJANVocabulary.AGENT_INITIALISATION).subjects();
		if (subjects.size() != 1) {
			throw new InitializationRDFValidationException("For now, only setting up exactly one agent is supported.");
		}
		return createAgent(initAgentRDF, subjects.iterator().next());
	}

	public Agent createAgent(final Model initAgentsRDF, final Resource initAgentRsc) throws URISyntaxException {
		agentRDFBuilder.setBaseURI(baseURI);
		agentRDFBuilder.setInitModel(initAgentsRDF);
		agentRDFBuilder.setAgentResource(initAgentRsc);
		Agent agent = agentRDFBuilder.build();
                initiateAgent(agent);
		return agent;
	}

        public Agent createAgent(final String name, final String template, final String knowledge, final RDFFormat format) throws URISyntaxException, IOException {
                agentParamBuilder.setName(name);
		agentParamBuilder.setBaseURI(baseURI);
                agentParamBuilder.setAgentTemplate(template);
		agentParamBuilder.setAgentKnowledge(knowledge, format);
		Agent agent = agentParamBuilder.build();
                initiateAgent(agent);
		return agent;
	}

        private void initiateAgent(final Agent agent) throws URISyntaxException {
            registerAgent(agent);
            if (agent.getInitialBehavior() != null) {
                BTRoot initialBT = agent.getInitialBehavior().getBehaviorTree();
                LOG.info("Start Initial Behavior: " + agent.getInitialBehavior().getName());
                new Thread(() -> initialBT.run()).start();
            }
        }

        private void registerAgent(final Agent agent) throws URISyntaxException {
            addUriGenerator(agent);
            addAgent(agent);
        }

        private void addUriGenerator(final Agent agent) throws URISyntaxException {
            UriBuilder builder = UriBuilder.fromUri(baseURI)
						.path(AgentsService.AGENT_PATH)
						.resolveTemplate(AgentsService.NAME, agent.getName());
            agent.getBehaviors().entrySet().stream().forEach((behavior) -> {
                                behavior.getValue()
					.getBehaviorTree()
					.getObject()
					.setUriGenerator(new AgentsService.UriGeneratorImpl(builder));
            });
        }

	public void addAgent(final Agent agent) {
		if (agentMap.containsKey(agent.getName())) {
			throw new InitializationNotAllowedException("Agent " + agent.getName() + " already created");
		}
		agentMap.put(agent.getName(), agent);
	}

	public Agent getAgent(final String agentName) {
		if (agentMap.containsKey(agentName)) {
			return agentMap.get(agentName);
		} else {
			throw new AgentNotFoundException("Agent " + agentName + " does not exist");
		}
	}

	public Collection<Agent> getAllAgents() {
		return agentMap.values();
	}

	public void deleteAgent(final Agent agent) {
		if (!agentMap.containsKey(agent.getName())) {
			throw new IllegalArgumentException("Agent with name " + agent.getName() + " could not be found");
		}
		tripleStoreManager.removeTripleDataBase(agent.getBeliefs());
		agentMap.remove(agent.getName());
	}

	public String sayHello() {
		return "Hallo!";
	}

	@PreDestroy
	public void cleanAllAgents() {
		for (Iterator<Map.Entry<String, Agent>> iterator = agentMap.entrySet().iterator(); iterator.hasNext();) {
			TripleDataBase db = iterator.next().getValue().getBeliefs();
			tripleStoreManager.removeTripleDataBase(db);
			iterator.remove();
		}
	}
}
