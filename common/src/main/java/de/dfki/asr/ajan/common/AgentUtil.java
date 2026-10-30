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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.validator.UrlValidator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings({
  "PMD.ExcessiveImports",
  "PMD.CyclomaticComplexity",
  "PMD.ExcessiveParameterList"
})
public final class AgentUtil {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private AgentUtil() {}

  public static String getString(final Model model, final RDFFormat mimeType) {
    OutputStream out = new ByteArrayOutputStream();
    Rio.write(model, out, mimeType);
    return out.toString();
  }

  public static Model setNamedGraph(final GraphQueryResult result, final String iri) {
    ModelBuilder builder = new ModelBuilder();
    while (result.hasNext()) {
      Statement stmt = result.next();
      addNamedStatement(builder, stmt, iri);
    }
    return builder.build();
  }

  public static Model mergeModels(final Model sink, final Model input, final Resource context) {
    ModelBuilder builder = new ModelBuilder(sink);
    addModel(builder, input.iterator(), context);
    return builder.build();
  }

  public static Model setNamedGraph(final Model model, final URI context) {
    return setNamedGraph(model.iterator(), context.toString());
  }

  public static Model setNamedGraph(final Model model, final Resource context) {
    return setNamedGraph(model.iterator(), context);
  }

  public static Model setNamedGraph(final Iterator<Statement> itr, final Object context) {
    ModelBuilder builder = new ModelBuilder();
    addModel(builder, itr, context);
    return builder.build();
  }

  private static void addModel(
      final ModelBuilder builder, final Iterator<Statement> itr, final Object context) {
    while (itr.hasNext()) {
      Statement stmt = itr.next();
      if (context instanceof String) {
        addNamedStatement(builder, stmt, (String) context);
      } else if (context instanceof Resource) {
        addNamedStatement(builder, stmt, (Resource) context);
      } else {
        addNamedStatement(builder, stmt);
      }
    }
  }

  public static void addModel(final Model model, final Iterator<Statement> itr) {
    while (itr.hasNext()) {
      model.add(itr.next());
    }
  }

  private static void addNamedStatement(
      final ModelBuilder builder, final Statement stmt, final String iri) {
    builder.namedGraph(iri).subject(stmt.getSubject()).add(stmt.getPredicate(), stmt.getObject());
  }

  private static void addNamedStatement(
      final ModelBuilder builder, final Statement stmt, final Resource resource) {
    builder
        .namedGraph(resource)
        .subject(stmt.getSubject())
        .add(stmt.getPredicate(), stmt.getObject());
  }

  private static void addNamedStatement(final ModelBuilder builder, final Statement stmt) {
    builder.defaultGraph().subject(stmt.getSubject()).add(stmt.getPredicate(), stmt.getObject());
  }

  public static boolean rdfMimeType(final String mimeType) {
    try {
      Optional<RDFFormat> fileFormatForMIMEType =
          RDFWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
      return fileFormatForMIMEType.isPresent();
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  public static RDFFormat formatForMimeType(final String mimeType) throws IllegalArgumentException {
    Optional<RDFFormat> fileFormatForMIMEType =
        RDFWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
    if (!fileFormatForMIMEType.isPresent()) {
      throw new IllegalArgumentException("No format known for mime type " + mimeType);
    }
    return fileFormatForMIMEType.get();
  }

  public static Resource getSubject(final String url) {
    UrlValidator uv = new UrlValidator();
    Resource subject;
    if (uv.isValid(url)) {
      subject = VF.createIRI(url);
    } else {
      subject = VF.createBNode();
    }
    return subject;
  }

  public static Model setResponseMessageInformation(
      final Model input, final MultivaluedMap<String, String> mm) {
    return MessageInformation.setResponseMessageInformation(input, mm);
  }

  public static Model setRequestMessageInformation(
      final Model input, final MultivaluedMap<String, String> mm) {
    return MessageInformation.setRequestMessageInformation(input, mm);
  }

  public static JsonNode setMessageInformation(
      final JsonNode input, final MultivaluedMap<String, String> mm) {
    return MessageInformation.setMessageInformation(input, mm);
  }

  public static Document setMessageInformation(
      final Document input, final MultivaluedMap<String, String> mm) {
    return MessageInformation.setMessageInformation(input, mm);
  }

  public static CSVInput setMessageInformation(
      final CSVInput input, final MultivaluedMap<String, String> mm) {
    // TODO: find a way to add header data
    return input;
  }

  public static JsonNode getJsonFromStream(final InputStream is) throws IOException {
    return MessageParser.getJsonFromStream(is);
  }

  public static Document getXMLFromStream(final InputStream is) throws SAXException, IOException {
    return MessageParser.getXMLFromStream(is);
  }

  public static Document getHTMLFromStream(final InputStream is) throws SAXException, IOException {
    return MessageParser.getHTMLFromStream(is);
  }

  public static CSVInput getCSVFromStream(final InputStream is) throws IOException {
    return MessageParser.getCSVFromStream(is);
  }
}
