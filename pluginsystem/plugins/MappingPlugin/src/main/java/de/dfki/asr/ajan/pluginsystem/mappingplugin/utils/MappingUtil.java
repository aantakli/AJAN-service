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
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.Term;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;

public final class MappingUtil {

    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    private MappingUtil() {

}

    public static Model loadJsonMapping(final ObjectNode input, final RDF4JStore rmlStore) throws RMLMapperException {
        try {
            boolean removeDuplicates = false;
            List<Term> triplesMaps = new ArrayList<>();
            RecordsFactory rf = new RecordsFactory(input, "http://www.ajan.de/ajan-ns#EventInformation");
            Executor executor = new Executor(rmlStore, rf);
            return readOutQuads(executor.execute(triplesMaps, removeDuplicates), getGraph(input));
        } catch ( Error | UnsupportedOperationException | IOException ex) {
            throw new RMLMapperException(ex);
        }
    }

    private static Model readOutQuads(final QuadStore store, final IRI graph) {
        Model model = new LinkedHashModel();
        store.getQuads(null,null,null,null).stream().forEach((quad) -> {
                model.add(VALUE_FACTORY.createIRI(quad.getSubject().getValue()),
                                VALUE_FACTORY.createIRI(quad.getPredicate().getValue()),
                                getValue(quad.getObject()), graph);
        });
        return model ;
    }

    private static Value getValue(final Term term) {
        if (term instanceof Literal) {
                Literal lit = (Literal)term;
                return VALUE_FACTORY.createLiteral(lit.getValue(), VALUE_FACTORY.createIRI(lit.getDatatype().getValue()));
        } else {
                return VALUE_FACTORY.createIRI(term.getValue());
        }
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

    private static IRI getGraph(final ObjectNode node) {
        JsonNode context = node.findValue("AJAN_EVENT");
        if (context == null) {
                return null;
        }
        return VALUE_FACTORY.createIRI(context.textValue());
    }
}
