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
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.data.AgentModelManager;
import de.dfki.asr.ajan.data.AgentResourceManager;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import de.dfki.asr.ajan.model.Endpoint;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;

public class AgentEndpoints {

	private final AgentModelManager modelManager;
	private final AgentResourceManager resourceManager;
	private final Repository agentRepo;

	public AgentEndpoints(final AgentModelManager modelManager, final AgentResourceManager resourceManager, final Repository agentRepo) {
		this.modelManager = modelManager;
		this.resourceManager = resourceManager;
		this.agentRepo = agentRepo;
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Map<String,Endpoint> getAgentEndpoints(final Resource agentTemplate, final Model agentTemplateModel, final Map<URI,Event> events) throws URISyntaxException {
		Map<String,Endpoint> endpoints = new ConcurrentHashMap();
		Iterator<Resource> resources = resourceManager.getResources(agentTemplate, agentTemplateModel, AJANVocabulary.AGENT_HAS_ENDPOINT);
		while (resources.hasNext()) {
			Resource resource = resources.next();
			if (!(resource instanceof IRI)) {
				throw new InitializationRDFValidationException("No BNodes allowed as Endpoint Subject " + resource.stringValue());
			}
			Model model = modelManager.getTemplateFromTDB(agentRepo, resource);
			String name = modelManager.getLabel(model.filter(resource, AJANVocabulary.HAS_CAPABILITY, null), "No Endpoint-literal for " + RDFS.LABEL + " present");
			Event event = getEndpointEvent(model, resource, events);
			Endpoint endpoint = getEndpoint(model.filter(resource, RDF.TYPE, null), name, event);
			endpoints.put(name, endpoint);
		}
		return endpoints;
	}

	private Event getEndpointEvent(final Model model, final Resource resource, final Map<URI,Event> events) throws URISyntaxException {
		Optional<IRI> optEvent = Models.objectIRI(model.filter(resource, AJANVocabulary.AGENT_HAS_EVENT, null));
		if (!optEvent.isPresent()) {
			throw new InitializationRDFValidationException("No Endpoint event defined");
		}
		return events.get(new URI(optEvent.get().toString()));
	}

	private Endpoint getEndpoint(final Model model, final String name, final Event event) {
		Endpoint endpoint;
		Optional<IRI> type = modelManager.getTypeIRI(model, "Endpoint");
		if (type.get().equals(AJANVocabulary.ENDPOINT_TYPE)) {
			endpoint = new Endpoint(name,event);
		} else {
			throw new InitializationRDFValidationException("Endpoint type unknowen");
		}
		return endpoint;
	}

}
