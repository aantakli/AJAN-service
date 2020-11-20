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

import de.dfki.asr.ajan.model.Agent;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.springframework.stereotype.Component;

@Provider
@Produces({
				"application/rdf+xml",
				"application/xml",
				"application/n-triples",
				"text/turtle",
				"application/x-turtle",
				"text/n3",
				"text/rdf+n3",
				"application/trix",
				"application/trig",
				"application/x-binary-rdf",
				"application/n-quads",
				"application/ld+json",
				"application/rdf+json",
				"application/xhtml+xml"
				})
@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.AvoidPrefixingMethodParameters", "PMD.ExcessiveImports"})
@Component
public class AgentProducer extends ModelProducer implements MessageBodyWriter<Agent> {

	@Override
	public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
		return Agent.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(final Agent t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
		// As of JAX-RS 2.0, the method has been deprecated
		// and the value returned by the method is ignored by a JAX-RS runtime.
		// All MessageBodyWriter implementations are advised to return -1.
		return -1;
	}

	@Override
	@SuppressWarnings("PMD.PreserveStackTrace")
	public void writeTo(final Agent t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
		Model agentModel = getAgentModel(t);
		writeModel(agentModel, mt, out);
	}

	public Model getAgentModel(final Agent agent) {
		Model model = new LinkedHashModel();
		addAgentStatements(agent, model);
		return model;
	}
}
