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

package de.dfki.asr.ajan.rest.providers;

import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.model.Agent;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

public class ModelProducer {

	@Context
	private UriInfo uriInfo;

	public void writeModel(final Model t, final MediaType mt, final OutputStream output) {
		Optional<RDFFormat> format = getFormatForMediaType(mt);
		if (!format.isPresent()) {
			String msg = "Can not produce RDF as mimetype + " + mt.toString();
			Response response = Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(msg).build();
			throw new WebApplicationException(response);
		}
		RDFWriter writer = Rio.createWriter(format.get(), output);
                writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
		try {
			writer.startRDF();
			for (Statement stm : t) {
                                writer.handleNamespace("ajan", "http://www.ajan.de/ajan-ns#");
                                writer.handleNamespace("bt", "http://www.ajan.de/behavior/bt-ns#");
                                writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
                                writer.handleNamespace("actn", "http://www.ajan.de/actn#");
                                writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
                                writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				writer.handleStatement(stm);
			}
			writer.endRDF();
		} catch (RDFHandlerException ex) {
			Response response = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
			throw new WebApplicationException(ex, response);
		}
	}

	protected Optional<RDFFormat> getFormatForMediaType(final MediaType mt) {
		RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
		return registry.getFileFormatForMIMEType(mt.toString());
	}

	public void addAgentStatements(final Agent agent, final Model model) {
		ValueFactory factory = SimpleValueFactory.getInstance();
		Resource agentResource = getAgentResource(agent);
		IRI agentKnowledge = factory.createIRI(agent.getBeliefs().getSparqlUpdateEndpoint().toString());
		model.add(factory.createStatement(agentResource, RDF.TYPE, AJANVocabulary.AGENT_TYPE));
		model.add(factory.createStatement(agentResource, AJANVocabulary.AGENT_HAS_ID, factory.createLiteral(agent.getId())));
                model.add(factory.createStatement(agentResource, AJANVocabulary.AGENT_HAS_TEMPLATE, agent.getTemplate()));
		model.add(factory.createStatement(agentResource, AJANVocabulary.AGENT_HAS_KNOWLEDGE, agentKnowledge));
		agent.getBehaviors().forEach((k,v) -> {
                    URI uri;
                    try {
                        uri = new URI(k.stringValue());
                        Resource behavior = factory.createIRI(agentResource + "/behaviors/" + uri.getFragment());
                        model.add(factory.createStatement(agentResource,AJANVocabulary.AGENT_HAS_BEHAVIOR,behavior));
                        model.add(factory.createStatement(behavior,RDF.TYPE,BTVocabulary.BEHAVIOR_TREE));
                        model.add(factory.createStatement(behavior,RDFS.LABEL,factory.createLiteral(v.getName())));
                        model.add(factory.createStatement(behavior,RDFS.ISDEFINEDBY,k));
                    }
                    catch (URISyntaxException ex) {
                        Logger.getLogger(ModelProducer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                );
		agent.getEndpoints().forEach((k,v) -> model.add(factory.createStatement(
						agentResource,
						AJANVocabulary.AGENT_HAS_ACTION,
						factory.createIRI(agentResource + "?capability=" + k))));
	}

	private Resource getAgentResource(final Agent agent) {
		Resource agentResource;
		ValueFactory factory = SimpleValueFactory.getInstance();
		int segments = uriInfo.getPathSegments().size();
		String path = uriInfo.getPathSegments().get(segments - 1).getPath();
		if (agent.getId().equals(path)) {
			agentResource = factory.createIRI(uriInfo.getAbsolutePath().toString());
		} else {
			agentResource = factory.createIRI(uriInfo.getAbsolutePath().toString() + agent.getId());
		}
		return agentResource;
	}
}
