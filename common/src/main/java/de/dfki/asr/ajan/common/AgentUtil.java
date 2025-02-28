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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.validator.UrlValidator;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CyclomaticComplexity", "PMD.ExcessiveParameterList"})
public final class AgentUtil {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final Logger LOG = LoggerFactory.getLogger(AgentUtil.class);

	private AgentUtil() {

	}

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

	private static void addModel(final ModelBuilder builder, final Iterator<Statement> itr, final Object context) {
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

	private static void addNamedStatement(final ModelBuilder builder, final Statement stmt, final String iri) {
		builder.namedGraph(iri)
			.subject(stmt.getSubject())
			.add(stmt.getPredicate(), stmt.getObject());
	}

	private static void addNamedStatement(final ModelBuilder builder, final Statement stmt, final Resource resource) {
		builder.namedGraph(resource)
			.subject(stmt.getSubject())
			.add(stmt.getPredicate(), stmt.getObject());
	}

	private static void addNamedStatement(final ModelBuilder builder, final Statement stmt) {
		builder.defaultGraph()
			.subject(stmt.getSubject())
			.add(stmt.getPredicate(), stmt.getObject());
	}

	public static boolean rdfMimeType(final String mimeType) {
		try {
			Optional<RDFFormat> fileFormatForMIMEType = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
			return fileFormatForMIMEType.isPresent();
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	public static RDFFormat formatForMimeType(final String mimeType) throws IllegalArgumentException {
		Optional<RDFFormat> fileFormatForMIMEType = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(mimeType);
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

	public static Model setResponseMessageInformation(final Model input, final MultivaluedMap<String, String> mm) {
		BNode resSubj = VF.createBNode();
		input.add(resSubj, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#Response"));
		return setMessageInformation(resSubj, input, mm);
	}

	public static Model setRequestMessageInformation(final Model input, final MultivaluedMap<String, String> mm) {
		BNode reqSubj = VF.createBNode();
		input.add(reqSubj, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#Request"));
		return setMessageInformation(reqSubj, input, mm);
	}

	private static Model setMessageInformation(final Resource subject, final Model input, final MultivaluedMap<String, String> mm) {
		input.add(subject, VF.createIRI("https://www.w3.org/TR/owl-time/inXSDDateTimeStamp"), VF.createLiteral(OffsetDateTime.now(ZoneOffset.UTC).toString()));
		input.addAll(setBodyInformation(subject, input));
		if (!mm.isEmpty()) {
			addHeaders(subject, input, mm);
		}
		return input;
	}

	private static Model setBodyInformation(final Resource subject, final Model input) {
		Model body = new LinkedHashModel();
		for (Statement stmt: input) {
			body.add(subject, VF.createIRI("http://www.w3.org/2011/http#body"), stmt.getSubject());
		}
		return body;
	}

	private static void addHeaders(final Resource subject, final Model input, final MultivaluedMap<String, String> mm) {
		BNode headers = VF.createBNode();
		input.add(subject, VF.createIRI("http://www.w3.org/2011/http#headers"), headers);
		List<Resource> headersColl = new ArrayList();
		for (Map.Entry<String, List<String>> entry: mm.entrySet()) {
			BNode responseHeader = VF.createBNode();
			headersColl.add(responseHeader);
			input.add(responseHeader, RDF.TYPE, VF.createIRI("http://www.w3.org/2011/http#MessageHeader"));
			input.add(responseHeader, VF.createIRI("http://www.w3.org/2011/http#fieldName"), VF.createLiteral(entry.getKey()));
			for (String value: entry.getValue()) {
				input.add(responseHeader, VF.createIRI("http://www.w3.org/2011/http#fieldValue"), VF.createLiteral(value));
			}
		}
		input.addAll(RDFCollections.asRDF(headersColl, headers, input));
	}

	public static JsonNode setMessageInformation(final JsonNode input, final MultivaluedMap<String, String> mm) {
		ObjectNode info = JsonNodeFactory.instance.objectNode();
		info.set("message", input);
		setMessageInformation2JSON(info, mm);
		return info;
	}

	private static void setMessageInformation2JSON(final ObjectNode node, final MultivaluedMap<String, String> mm) {
		node.put("utc", OffsetDateTime.now(ZoneOffset.UTC).toString());
		for (Map.Entry<String, List<String>> entry: mm.entrySet()) {
			node.put(entry.getKey(), OffsetDateTime.now(ZoneOffset.UTC).toString());
			for (String value: entry.getValue()) {
				node.put(entry.getKey(), value);
			}
		}
	}

	public static Document setMessageInformation(final Document input, final MultivaluedMap<String, String> mm) {
		DocumentImpl doc = (DocumentImpl) input;
		Element root = doc.getDocumentElement();
		root.setAttribute("utc", OffsetDateTime.now(ZoneOffset.UTC).toString());
		for (Map.Entry<String, List<String>> entry: mm.entrySet()) {
			root.setAttribute(entry.getKey(), OffsetDateTime.now(ZoneOffset.UTC).toString());
			for (String value: entry.getValue()) {
				root.setAttribute(entry.getKey(), value);
			}
		}
		LOG.info("Length: " + doc.getDocumentElement().getAttributes().getLength());
		return doc;
	}

	public static CSVInput setMessageInformation(final CSVInput input, final MultivaluedMap<String, String> mm) {
		// TODO: find a way to add header data
		return input;
	}

	public static JsonNode getJsonFromStream(final InputStream is) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(is);
	}

	public static Document getXMLFromStream(final InputStream is) throws SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(is));
		return parser.getDocument();
	}

	public static Document getHTMLFromStream(final InputStream is) throws SAXException, IOException {
		String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		W3CDom w3cDom = new W3CDom();
		return w3cDom.fromJsoup(Jsoup.parse(html, Parser.xmlParser()));
	}

	public static CSVInput getCSVFromStream(final InputStream is) throws IOException {
		CSVInput input;
		try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
			input = new CSVInput();
			input.setContent(reader.readAll());
		}
		return input;
	}
}
