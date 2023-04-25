/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.mappingplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.opencsv.CSVWriter;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.CsvResolver;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import de.dfki.asr.ajan.common.CSVInput;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.vocabularies.MappingVocabulary;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.dom.DeferredDocumentImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.Repository;

public final class MappingUtil {

    private MappingUtil() {

	}

    public static InputStream getResourceStream(final Object input) throws JsonProcessingException, IOException, TransformerException {
        if (input instanceof JsonNode) {
            return MappingUtil.getJSONResourceStream(input);
        }
		if (input instanceof DocumentImpl) {
			return MappingUtil.getXMLResourceStream(input);
        }
		if (input instanceof CSVInput) {
			return MappingUtil.getCSVResourceStream(input);
        }
        return null;
    }

	public static Model getMappedModel(final Model mapping, final InputStream resourceStream) {
		Set<TriplesMap> mappingInput;
		mappingInput = RmlMappingLoader.build().load(mapping);
		RmlMapper mapper = RmlMapper.newBuilder()
			.setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
			.setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
			.setLogicalSourceResolver(Rdf.Ql.Csv, new CsvResolver())
			.build();

		mapper.bindInputStream(resourceStream);
		return mapper.map(mappingInput);
	}

	public static Model getJSONStatementModel(final InputStream resourceStream) throws IOException {
		Model model = new LinkedHashModel();
		String text = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
		model.add(Values.bnode(), MappingVocabulary.HAS_JSON_STRING, Values.literal(text));
		return model;
	}

    public static Model getTriplesMaps(final Repository repo, final List<URI> mappings) throws URISyntaxException {
        if (mappings == null) {
                throw new URISyntaxException("bt:mapping", "Cannot be null");
        }
		StringBuilder context = new StringBuilder();
		context.append("DESCRIBE ");
		mappings.stream().forEach((mapping) -> {
			context.append("<").append(mapping.toString()).append("> ");
		});
        return SPARQLUtil.queryRepository(repo, context.toString());
    }

	public static Model getTriplesMaps(final Repository repo, final URI mapping) throws URISyntaxException {
        if (mapping == null) {
                throw new URISyntaxException("bt:mapping", "Cannot be null");
        }
		StringBuilder context = new StringBuilder();
		context.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ");
		context.append("<").append(mapping.toString()).append("> { ?s ?p ?o }}");
		Model model = SPARQLUtil.queryRepository(repo, context.toString());
        return model;
    }

    private static InputStream getJSONResourceStream(final Object eventInfo) throws JsonProcessingException, IOException {
        JsonNode input = (JsonNode) eventInfo;
		ObjectMapper om = new ObjectMapper();
		ObjectWriter writer = om.writer();
        return new ByteArrayInputStream(writer.writeValueAsBytes(input));
    }

    private static InputStream getXMLResourceStream(final Object eventInfo) throws IOException, TransformerException {
		DocumentImpl input = (DocumentImpl) eventInfo;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(input);
		StreamResult console = new StreamResult(baos);
		transformer.transform(source, console);
		return new ByteArrayInputStream(baos.toByteArray());
    }

    private static InputStream getCSVResourceStream(final Object eventInfo) throws JsonProcessingException, IOException {
		CSVInput input = (CSVInput) eventInfo;
		String out = "";
		try (StringWriter sw = new StringWriter(); CSVWriter csvWriter = new CSVWriter(sw)) {
			for (String[] rowData: input.getContent()) {
				csvWriter.writeNext(rowData);
				out = sw.toString();
			}
		}
        return new ByteArrayInputStream(out.getBytes());
    }
}
