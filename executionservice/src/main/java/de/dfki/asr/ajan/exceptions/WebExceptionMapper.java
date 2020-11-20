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

package de.dfki.asr.ajan.exceptions;

import de.dfki.asr.ajan.common.AJANVocabulary;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
@Component
public class WebExceptionMapper implements ExceptionMapper<WebApplicationException> {
	private static final Logger LOG = LoggerFactory.getLogger(WebExceptionMapper.class);
	private final ValueFactory factory = SimpleValueFactory.getInstance();

	@Override
	public Response toResponse(final WebApplicationException e) {
		return createRDFExceptionResponse(e);
	}

	private Response createRDFExceptionResponse(final WebApplicationException e) {
		Model message = createRDFExceptionDescription(e);
		return Response.status(getStatusCode(e)).type("text/turtle").entity(message).build();
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private Model createRDFExceptionDescription(final Throwable e) {
		Model model = new LinkedHashModel();
		try {
			addExceptionToModel(e, model);
		} catch (Exception inner) {
			LOG.error("Cannot add exception to reply", inner);
			LOG.error("While handling outer error", e);
		}
		return model;
	}

	private BNode addExceptionToModel(final Throwable thrown, final Model model) {
		IRI cls = factory.createIRI("urn:java-class:", thrown.getClass().getCanonicalName());
		BNode thrownDescription = factory.createBNode();
		model.add(factory.createStatement(thrownDescription, RDF.TYPE, cls));
		model.add(factory.createStatement(thrownDescription, RDF.TYPE, AJANVocabulary.EXC_EXCEPTION));
		model.add(factory.createStatement(thrownDescription, RDFS.LABEL, factory.createLiteral(thrown.getMessage())));
		Throwable cause = thrown.getCause();
		if (cause != null) {
			BNode causeDescription = addExceptionToModel(cause, model);
			model.add(factory.createStatement(thrownDescription, AJANVocabulary.EXC_HAS_CAUSE, causeDescription));
		}
		return thrownDescription;
	}

	private int getStatusCode(final WebApplicationException e) {
		return e.getResponse().getStatus();
	}
}
