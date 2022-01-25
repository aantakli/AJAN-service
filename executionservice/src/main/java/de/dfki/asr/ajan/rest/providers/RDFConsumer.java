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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Component;

@Provider
@Consumes({
				"application/rdf+xml",
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
@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.AvoidPrefixingMethodParameters"})
@Component
public class RDFConsumer implements MessageBodyReader<Model> {

	private static final String TURTLE_BASE_URI = "http://www.ajan.de";
        private static final ValueFactory VF = SimpleValueFactory.getInstance();

	@Override
	public boolean isReadable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
		if (!type.isAssignableFrom(Model.class)) {
			return false;
		}
		RDFParserRegistry registry = RDFParserRegistry.getInstance();
		Optional<RDFFormat> format = registry.getFileFormatForMIMEType(mt.toString());
		return format.isPresent();
	}

	@Override
	public Model readFrom(final Class<Model> t, final Type type, final Annotation[] annts, final MediaType mt, final MultivaluedMap<String, String> mm, final InputStream in) throws IOException, WebApplicationException {
		RDFParserRegistry registry = RDFParserRegistry.getInstance();
		Optional<RDFFormat> format = registry.getFileFormatForMIMEType(mt.toString());
		if (!format.isPresent()) {
			String msg = "Can not consume RDF of mimetype + " + mt.toString();
			Response response = Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(msg).build();
			throw new WebApplicationException(response);
		}
		                Model input = Rio.parse(in, TURTLE_BASE_URI, format.get());
		return setMessageInformation(input, mm);
	}

        private Model setMessageInformation(final Model input, final MultivaluedMap<String, String> mm) {
            BNode subject = VF.createBNode();
            IRI type = VF.createIRI("http://www.ajan.de/ajan-ns#RequestInformation");
            input.add(subject, RDF.TYPE, type);
            input.add(subject, VF.createIRI("https://www.w3.org/TR/owl-time/inXSDDateTimeStamp"), VF.createLiteral(OffsetDateTime.now(ZoneOffset.UTC).toString()));
            for (Map.Entry<String, List<String>> entry: mm.entrySet()) {
                IRI entryPred = VF.createIRI("http://www.ajan.de/generated-ns#has_" + entry.getKey());
                for (String value: entry.getValue()) {
                    input.add(subject, entryPred, VF.createLiteral(value));
                }
            }
            return input;
        }
}
