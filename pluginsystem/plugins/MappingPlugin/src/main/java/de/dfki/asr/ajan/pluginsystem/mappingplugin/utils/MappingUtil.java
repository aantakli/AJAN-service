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

import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.Term;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.CsvResolver;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.dom.DeferredDocumentImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;

public final class MappingUtil {

    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    private MappingUtil() {

}

    public static  InputStream getResourceStream(final Object input) throws JsonProcessingException, IOException, TransformerException {
        if (input instanceof JsonNode) {
            return MappingUtil.getJsonResourceStream(input);
        }
		if (input instanceof DeferredDocumentImpl) {
			return MappingUtil.getXMLResourceStream(input);
        }
        return null;
    }

    public static Model loadJsonMapping(final JsonNode input, final RDF4JStore rmlStore) throws RMLMapperException {
        ObjectNode node = JsonNodeFactory.instance.objectNode();;
		if (input.isArray()) {
			node.set("array", (ArrayNode)input);
		} else {
			node.setAll((ObjectNode)input);
		}
		try {
            boolean removeDuplicates = false;
            List<Term> triplesMaps = new ArrayList<>();
            RecordsFactory rf = new RecordsFactory(node, "http://www.ajan.de/ajan-ns#EventInformation");
            Executor executor = new Executor(rmlStore, rf);
            return readOutQuads(executor.execute(triplesMaps, removeDuplicates), getGraph(node));
        } catch ( Error | UnsupportedOperationException | IOException ex) {
            throw new RMLMapperException(ex);
        }
    }

    private static Model readOutQuads(final QuadStore store, final IRI graph) {
        Model model = new LinkedHashModel();
        store.getQuads(null,null,null,null).stream().forEach((quad) -> {
			Resource subject = getSubject(quad.getSubject());
			IRI predicate = VALUE_FACTORY.createIRI(quad.getPredicate().getValue());
                model.add(subject,predicate,getValue(quad.getObject()), graph);
        });
        return model ;
    }

	private static Resource getSubject(final Term term) {
        if (term instanceof BlankNode) return VALUE_FACTORY.createBNode(term.getValue());
        else return VALUE_FACTORY.createIRI(term.getValue());
    }

    private static Value getValue(final Term term) {
        if (term instanceof Literal) {
                Literal lit = (Literal)term;
                return VALUE_FACTORY.createLiteral(lit.getValue(), VALUE_FACTORY.createIRI(lit.getDatatype().getValue()));
        } 
		else if (term instanceof BlankNode) return VALUE_FACTORY.createBNode(term.getValue());
        else return VALUE_FACTORY.createIRI(term.getValue());
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

    public static Model getTriplesMaps(final Repository repo, final List<URI> mappings) throws URISyntaxException {
        if (mappings == null) {
                throw new URISyntaxException("bt:mappings", "Cannot be null");
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
		context.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH  ");
		context.append("<").append(mapping.toString()).append("> { ?s ?p ?o }}");
        return SPARQLUtil.queryRepository(repo, context.toString());
    }


    public static InputStream getJsonResourceStream(final Object eventInfo) throws JsonProcessingException, IOException {
        JsonNode input = (JsonNode) eventInfo;
		ObjectMapper om = new ObjectMapper();
		ObjectWriter writer = om.writer();
        return new ByteArrayInputStream(writer.writeValueAsBytes(input));
    }

    public static InputStream getXMLResourceStream(final Object eventInfo) throws IOException, TransformerException {
		DeferredDocumentImpl input = (DeferredDocumentImpl) eventInfo;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(input);
		StreamResult console = new StreamResult(baos);
		transformer.transform(source, console);
		return new ByteArrayInputStream(baos.toByteArray());
    }

    /*private InputStream getCsvResourceStream(final Object eventInfo) throws JsonProcessingException {
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(input));
    }*/

    private static IRI getGraph(final ObjectNode node) {
        JsonNode context = node.findValue("AJAN_EVENT");
        if (context == null) {
                return null;
        }
        return VALUE_FACTORY.createIRI(context.textValue());
    }
}
