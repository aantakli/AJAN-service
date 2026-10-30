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

package de.dfki.asr.ajan.common;

import java.util.Optional;
import java.util.UUID;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

/**
 * Internal helper; the public facade is {@link SPARQLUtil}. It moves statements into the named graphs requested via ajan:rdfContext.
 */
final class NamedGraphs {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();

	private NamedGraphs() {

	}

	public static Model getNamedGraph(final Model resultModel) {
		Model model = resultModel;
		if (model.contains(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null)) {
			Optional<IRI> contextIRI = Models.objectIRI(model.filter(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null));
			if (contextIRI.isPresent()) {
				IRI context = contextIRI.get();
				model = addUniqueNamedGraph(model, context);
			}
			model.remove(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null);
		}
		if (model.contains(null, AJANVocabulary.HAS_RDF_CONTEXT, null)) {
			Optional<IRI> contextIRI = Models.objectIRI(model.filter(null, AJANVocabulary.HAS_RDF_CONTEXT, null));
			if (contextIRI.isPresent()) {
				IRI context = contextIRI.get();
				model = addNamedGraph(model, context);
			}
			model.remove(null, AJANVocabulary.HAS_RDF_CONTEXT, null);
		}
		return model;
	}

	private static Model addNamedGraph(final Model model, final IRI context) {
		if (context != null) {
			String ctx = context.toString();
			model.add(VF.createIRI(ctx), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, AJANVocabulary.CONTEXT);
			return AgentUtil.setNamedGraph(model.iterator(), ctx);
		}
		return model;
	}

	private static Model addUniqueNamedGraph(final Model model, final IRI context) {
		if (context != null) {
			String ctx = context.toString();
			String graphName = ctx + "_" + UUID.randomUUID().toString();
			model.add(VF.createIRI(ctx), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, AJANVocabulary.UUID_CONTEXT);
			model.add(VF.createIRI(graphName), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, VF.createIRI(ctx));
			return AgentUtil.setNamedGraph(model.iterator(), graphName);
		}
		return model;
	}
}
