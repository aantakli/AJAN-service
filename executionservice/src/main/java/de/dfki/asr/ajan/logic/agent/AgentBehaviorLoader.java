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
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.data.AgentModelManager;
import de.dfki.asr.ajan.data.AgentResourceManager;
import de.dfki.asr.ajan.data.AgentTDBManager;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import de.dfki.asr.ajan.model.Behavior;
import de.dfki.asr.ajan.model.SingleRunBehavior;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal helper; the public facade is {@link RDFAgentBuilder}. It loads the behaviors (behavior trees) of an agent template.
 */
final class AgentBehaviorLoader {

	// log category of the builder this code was extracted from
	private static final Logger LOG = LoggerFactory.getLogger(RDFAgentBuilder.class);

	private final AgentModelManager modelManager;
	private final AgentResourceManager resourceManager;
	private final AgentTDBManager tdbManager;
	private final AJANPluginLoader pluginLoader;

	public AgentBehaviorLoader(final AgentModelManager modelManager, final AgentResourceManager resourceManager,
					final AgentTDBManager tdbManager, final AJANPluginLoader pluginLoader) {
		this.modelManager = modelManager;
		this.resourceManager = resourceManager;
		this.tdbManager = tdbManager;
		this.pluginLoader = pluginLoader;
	}

	@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.PrematureDeclaration"})
	public Map<Resource, Behavior> getBehaviorTrees(final Iterator<Resource> behaviorRes, final Repository agentRepo, final String agentId) {
		Map<Resource, Behavior> bhvs = new ConcurrentHashMap<>();
		while (behaviorRes.hasNext()) {
			List<Resource> eventResc = new ArrayList<>();
			Resource resource = behaviorRes.next();
			if (!(resource instanceof IRI)) {
				throw new InitializationRDFValidationException("No BNodes allowed as Behavior Subject " + resource.stringValue());
			}
			Model model = modelManager.getTemplateFromTDB(agentRepo, resource);
			SingleRunBehavior sglBehavior = getSingleRunBehavior(resource, model, agentId);
			LOG.info("--> Behavior Tree for {} agent added: {}", agentId, resource.stringValue());
			boolean clearEKB = getClearEKB(model, resource, AJANVocabulary.BEHAVIOR_HAS_CLEAREKB);
			resourceManager.getResources(resource, model, AJANVocabulary.BEHAVIOR_HAS_TRIGGER).forEachRemaining(eventResc::add);
			bhvs.put((IRI) resource, new Behavior(sglBehavior.getName(), sglBehavior.getResource(), sglBehavior.getBehaviorTree(), clearEKB, eventResc));
		}
		return bhvs;
	}

	public SingleRunBehavior getSingleRunBehavior(final Resource resource, final Model model, final String agentId) {
		String behaviorName = modelManager.getLabel(model.filter(resource, RDFS.LABEL, null), "No Behavior-literal for " + RDFS.LABEL + " present");
		Resource btResc = resourceManager.getResource(model, resource, AJANVocabulary.BEHAVIOR_HAS_BT);
		return new SingleRunBehavior(behaviorName, (IRI) resource, getBT(btResc, agentId));
	}

	private boolean getClearEKB(final Model model, final Resource resource, final IRI predicate) {
		if (!model.filter(resource, predicate, null).objects().isEmpty()) {
			Value clearEKB = model.filter(resource, predicate, null).objects().stream().findFirst().get();
			if (clearEKB.isLiteral()) {
				return Boolean.parseBoolean(clearEKB.stringValue());
			}
		}
		return false;
	}

	private BTRoot getBT(final Resource behaviorResource, final String agentId) {
		BTRoot tree;
		try (RepositoryConnection conn = tdbManager.getBehaviorTDB().getInitializedRepository().getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn, pluginLoader.getNodeExtensions());
			tree = manager.get(behaviorResource, BTRoot.class);
			LOG.info("--> Created Behavior Tree: {} for {} agent", behaviorResource.stringValue(), agentId);
		} catch (RDF4JException ex) {
			LOG.error("--> Could not load BehaviorTree {} for {} agent. A possible reason is a reference to a non-existent resource! The issue stems from an issue with RDF4J", behaviorResource.stringValue(), agentId);
			LOG.error(ex.getMessage(), ex);
			throw new InitializationRDFValidationException("Could not load BehaviorTree: ", ex);
		} catch (RDFBeanException ex) {
			LOG.error("--> Could not load BehaviorTree {} for {} agent. A possible reason is a reference to a non-existent resource! The issue stems from an issue with RDFBean", behaviorResource.stringValue(), agentId);
			LOG.error(ex.getMessage(), ex);
			throw new InitializationRDFValidationException("Could not load BehaviorTree: ", ex);
		}
		return tree;
	}
}
