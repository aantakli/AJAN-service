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

package de.dfki.asr.ajan.data;

import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;

public class AgentResourceManager {

	public Resource getResource(final Model model, final Resource subject, final IRI predicate) {
		Optional<Resource> rsc = Models.objectResource(model.filter(subject, predicate, null));
		if (!rsc.isPresent()) {
			throw new InitializationRDFValidationException("No resource for " + predicate + " present");
		}
		return rsc.get();
	}

        public Resource getInitialBehaviorResource(final Resource agentTemplateRsc, final Model agentTemplateModel) {
		return getResource(agentTemplateRsc, agentTemplateModel, AJANVocabulary.AGENT_HAS_INITIALBEHAVIOR);
	}

        public Resource getFinalBehaviorResource(final Resource agentTemplateRsc, final Model agentTemplateModel) {
		return getResource(agentTemplateRsc, agentTemplateModel, AJANVocabulary.AGENT_HAS_FINALBEHAVIOR);
	}

	public Iterator<Resource> getBehaviorResources(final Resource agentTemplateRsc, final Model agentTemplateModel) {
		return getResources(agentTemplateRsc, agentTemplateModel, AJANVocabulary.AGENT_HAS_BEHAVIOR);
	}

        public Resource getResource(final Resource resource, final Model model, final IRI predicate) {
            Iterator<Resource> resourcesIter = getResources(resource, model, predicate);
            if (resourcesIter.hasNext()) {
                Resource btResource = resourcesIter.next();
                if (!(btResource instanceof IRI)) {
                    throw new InitializationRDFValidationException("No BNodes allowed as Behavior Subject " + btResource.stringValue());
                }
                return btResource;
            }
            return null;
	}

	public Iterator<Resource> getResources(final Resource resource, final Model model, final IRI predicate) {
		Iterator<Resource> resourcesIter;
		resourcesIter = model
				.filter(resource, predicate, null)
				.objects()
				.stream()
				.filter(value -> value instanceof Resource)
				.map(resourceValue -> (Resource) resourceValue)
				.iterator();
		return resourcesIter;
	}
}
