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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MultivaluedMap;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Internal helper; the public facade is {@link AgentUtil}. It adds HTTP message information (time stamp, headers) to incoming messages.
 */
final class MessageInformation {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();
  // log category used by AgentUtil
  private static final Logger LOG = LoggerFactory.getLogger(AgentUtil.class);

  private MessageInformation() {}

  public static Model setResponseMessageInformation(
      final Model input, final MultivaluedMap<String, String> mm) {
    BNode resSubj = VF.createBNode();
    input.add(resSubj, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#Response"));
    return setMessageInformation(resSubj, input, mm);
  }

  public static Model setRequestMessageInformation(
      final Model input, final MultivaluedMap<String, String> mm) {
    BNode reqSubj = VF.createBNode();
    input.add(reqSubj, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#Request"));
    return setMessageInformation(reqSubj, input, mm);
  }

  private static Model setMessageInformation(
      final Resource subject, final Model input, final MultivaluedMap<String, String> mm) {
    input.add(
        subject,
        VF.createIRI("https://www.w3.org/TR/owl-time/inXSDDateTimeStamp"),
        VF.createLiteral(OffsetDateTime.now(ZoneOffset.UTC).toString()));
    input.addAll(setBodyInformation(subject, input));
    if (!mm.isEmpty()) {
      addHeaders(subject, input, mm);
    }
    return input;
  }

  private static Model setBodyInformation(final Resource subject, final Model input) {
    Model body = new LinkedHashModel();
    for (Statement stmt : input) {
      body.add(subject, VF.createIRI("http://www.w3.org/2011/http#body"), stmt.getSubject());
    }
    return body;
  }

  private static void addHeaders(
      final Resource subject, final Model input, final MultivaluedMap<String, String> mm) {
    BNode headers = VF.createBNode();
    input.add(subject, VF.createIRI("http://www.w3.org/2011/http#headers"), headers);
    List<Resource> headersColl = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : mm.entrySet()) {
      BNode responseHeader = VF.createBNode();
      headersColl.add(responseHeader);
      input.add(
          responseHeader, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#MessageHeader"));
      input.add(
          responseHeader,
          VF.createIRI("http://www.w3.org/2011/http#fieldName"),
          VF.createLiteral(entry.getKey()));
      for (String value : entry.getValue()) {
        input.add(
            responseHeader,
            VF.createIRI("http://www.w3.org/2011/http#fieldValue"),
            VF.createLiteral(value));
      }
    }
    input.addAll(RDFCollections.asRDF(headersColl, headers, input));
  }

  public static JsonNode setMessageInformation(
      final JsonNode input, final MultivaluedMap<String, String> mm) {
    ObjectNode info = JsonNodeFactory.instance.objectNode();
    info.set("message", input);
    setMessageInformation2JSON(info, mm);
    return info;
  }

  private static void setMessageInformation2JSON(
      final ObjectNode node, final MultivaluedMap<String, String> mm) {
    node.put("utc", OffsetDateTime.now(ZoneOffset.UTC).toString());
    for (Map.Entry<String, List<String>> entry : mm.entrySet()) {
      node.put(entry.getKey(), OffsetDateTime.now(ZoneOffset.UTC).toString());
      for (String value : entry.getValue()) {
        node.put(entry.getKey(), value);
      }
    }
  }

  public static Document setMessageInformation(
      final Document input, final MultivaluedMap<String, String> mm) {
    DocumentImpl doc = (DocumentImpl) input;
    Element root = doc.getDocumentElement();
    root.setAttribute("utc", OffsetDateTime.now(ZoneOffset.UTC).toString());
    for (Map.Entry<String, List<String>> entry : mm.entrySet()) {
      root.setAttribute(entry.getKey(), OffsetDateTime.now(ZoneOffset.UTC).toString());
      for (String value : entry.getValue()) {
        root.setAttribute(entry.getKey(), value);
      }
    }
    LOG.info("Length: " + doc.getDocumentElement().getAttributes().getLength());
    return doc;
  }
}
