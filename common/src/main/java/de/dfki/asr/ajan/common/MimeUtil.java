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
import static de.dfki.asr.ajan.common.AgentUtil.formatForMimeType;
import static de.dfki.asr.ajan.common.AgentUtil.rdfMimeType;
import static de.dfki.asr.ajan.common.AgentUtil.setMessageInformation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.MultivaluedMap;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.xml.sax.SAXException;
import de.dfki.asr.ajan.common.exceptions.HttpResponseException;
import java.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CyclomaticComplexity", "PMD.ExcessiveParameterList"})
public final class MimeUtil {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final Logger LOG = LoggerFactory.getLogger(AgentUtil.class);

	private MimeUtil() {

	}

	public static Object encodeContent(final String BASE_URI, final boolean forceRDF, final MultivaluedMap mm, final InputStream content, final String mimeType) throws HttpResponseException, IOException, SAXException {
		if (mimeType == null) {
			return createModelFromResponse(BASE_URI, forceRDF, mm, content, "text/turtle");
		} else if (forceRDF) {
			return createModelFromResponse(BASE_URI, forceRDF, mm, content, mimeType);
		} else if (mimeType.contains("application/json")) {
			return createJsonFromResponse(mm, content);
		} else if (mimeType.contains("application/xml") || mimeType.contains("text/xml")) {
			return createXMLFromResponse(mm, content);
		} else if (mimeType.contains("text/html")) {
			return createHTMLFromResponse(mm, content);
		} else if (mimeType.contains("text/csv")) {
			return createCSVFromResponse(mm, content);
		}
		return createModelFromResponse(BASE_URI, forceRDF, mm, content, mimeType);
	}

	public static JsonNode createJsonFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String text = new String(response.readAllBytes(), StandardCharsets.UTF_8);
		LOG.info(text);
		JsonNode input =  mapper.readTree(new ByteArrayInputStream(text.getBytes()));
		return setMessageInformation(input, mm);
	}

	public static Document createXMLFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException, SAXException {
		Document input = AgentUtil.getXMLFromStream(response);
		return setMessageInformation(input, mm);
	}

	public static Document createHTMLFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException, SAXException {
		Document input = AgentUtil.getHTMLFromStream(response);
		return setMessageInformation(input, mm);
	}

	public static CSVInput createCSVFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException {
		CSVInput input = AgentUtil.getCSVFromStream(response);
		return setMessageInformation(input, mm);
	}

	private static Model createModelFromResponse(final String BASE_URI, final boolean forceRDF, final MultivaluedMap mm, final InputStream response, final String entityFormat) throws HttpResponseException {
		String mime = entityFormat;
		if (entityFormat.contains("charset")) {
			mime = entityFormat.split(";")[0];
		}
		if (forceRDF && !rdfMimeType(mime)) {
			try {
				Model model = new LinkedHashModel();
				String content = new String(response.readAllBytes(), StandardCharsets.UTF_8);
				model.add(VF.createIRI("http://www.w3.org/2011/http#Response"), VF.createIRI("http://www.w3.org/2011/http#body"), VF.createLiteral(content));
				return setMessageInformation(model, mm);
			} catch (IOException ex) {
				throw new HttpResponseException("Error while reading content.", ex);
			}
		} else {
			try {
				Model model = Rio.parse(response, BASE_URI, formatForMimeType(mime));
				return setMessageInformation(model, mm);
			} catch (IllegalArgumentException | RDFParseException | IOException ex) {
				throw new HttpResponseException("Error while parsing " + mime + " based response into RDF graph.", ex);
			}
		}
	}
}
