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

import de.dfki.asr.ajan.behaviour.events.*;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.data.AgentModelManager;
import de.dfki.asr.ajan.data.AgentResourceManager;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal helper; the public facade is {@link RDFAgentBuilder}. It reads the events of an agent template.
 */
final class AgentEventLoader {

	// log category of the builder this code was extracted from
	private static final Logger LOG = LoggerFactory.getLogger(RDFAgentBuilder.class);

	private static final Map<IRI, Supplier<Event>> EVENT_FACTORIES = Map.of(
			AJANVocabulary.MODEL_EVENT_TYPE, ModelEvent::new,
			AJANVocabulary.MODEL_QUEUE_EVENT_TYPE, ModelQueueEvent::new,
			AJANVocabulary.STRING_EVENT_TYPE, StringEvent::new,
			AJANVocabulary.MAPPING_EVENT_TYPE, MappingEvent::new,
			AJANVocabulary.DEFAULT_EVENT_TYPE, DefaultEvent::new);

	private final AgentModelManager modelManager;
	private final AgentResourceManager resourceManager;
	private final Repository agentRepo;
	private final AJANPluginLoader pluginLoader;

	public AgentEventLoader(final AgentModelManager modelManager, final AgentResourceManager resourceManager,
					final Repository agentRepo, final AJANPluginLoader pluginLoader) {
		this.modelManager = modelManager;
		this.resourceManager = resourceManager;
		this.agentRepo = agentRepo;
		this.pluginLoader = pluginLoader;
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Map<URI, Event> getAgentEvents(final Resource agentTemplate, final Model agentTemplateModel, final String agentId) throws URISyntaxException {
		Map<URI, Event> agentEvents = new ConcurrentHashMap<>();
		Iterator<Resource> resources = resourceManager.getResources(agentTemplate, agentTemplateModel, AJANVocabulary.AGENT_HAS_EVENT);
		while (resources.hasNext()) {
			Resource resource = resources.next();
			if (!(resource instanceof IRI)) {
				throw new InitializationRDFValidationException("No named Event is used");
			}
			Model resModel = modelManager.getTemplateFromTDB(agentRepo, resource);
			String eventName = modelManager.getLabel(resModel.filter(resource, RDFS.LABEL, null), "No Event-literal for " + RDFS.LABEL + " present");
			Event event = getEvent((IRI) resource, resModel, eventName, agentId);
			agentEvents.put(new URI(((IRI) resource).toString()), event);
		}
		return agentEvents;
	}

	private Event getEvent(final IRI resource, final Model model, final String name, final String agentId) {
		Optional<IRI> type = modelManager.getTypeIRI(model.filter(resource, RDF.TYPE, null), "Event");
		Event event = createEvent(type.get(), resource, agentId);
		event.setName(name);
		event.setUrl(resource.toString());
		return event;
	}

	private Event createEvent(final IRI type, final IRI resource, final String agentId) {
		if (type.equals(AJANVocabulary.GOAL_TYPE)) {
			return createGoal(resource, agentId);
		}
		Supplier<Event> factory = EVENT_FACTORIES.get(type);
		if (factory == null) {
			throw new InitializationRDFValidationException("Event type unknown");
		}
		return factory.get();
	}

	private Event createGoal(final IRI resource, final String agentId) {
		AJANGoal goal;
		try (RepositoryConnection conn = agentRepo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn, pluginLoader.getNodeExtensions());
			goal = manager.get(resource, AJANGoal.class);
		} catch (RDF4JException ex) {
			LOG.error("--> Could not load AJANGoal {} for {} agent, due to an RDF4JException", resource.stringValue(), agentId);
			LOG.error(ex.getMessage(), ex);
			throw new InitializationRDFValidationException("Could not load AJANGoal " + resource.stringValue(), ex);
		} catch (RDFBeanException ex) {
			LOG.error("--> Could not load AJANGoal {} for {} agent, due to an RDFBeanException", resource.stringValue(), agentId);
			LOG.error(ex.getMessage(), ex);
			throw new InitializationRDFValidationException("Could not load AJANGoal " + resource.stringValue(), ex);
		}
		return goal;
	}
}
