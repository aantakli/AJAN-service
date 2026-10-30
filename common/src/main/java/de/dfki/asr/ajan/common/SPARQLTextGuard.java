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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Guards every place where an IRI becomes part of SPARQL <em>text</em>.
 *
 * <p>Neither RDF4J's {@code RenderUtils.toSPARQL} (used by
 * {@code SPARQLQueryRenderer}) nor plain string concatenation escape an IRI;
 * they write {@code "<" + iri + ">"}. An IRI is only safe in that position if
 * it matches the SPARQL 1.1 {@code IRIREF} production
 * ({@code '<' ([^<>"{}|^`\]-[#x00-#x20])* '>'}): then the closing {@code >}
 * the renderer appends is the first one the SPARQL lexer sees, so the value
 * cannot end the IRI token early and continue as query syntax. The backslash
 * is excluded too, so no {@code \}{@code u003E} escape can smuggle in a
 * {@code >} either (some engines decode those before lexing).
 *
 * <p>This is a positive check against the grammar, not a list of known
 * payloads, and nothing is escaped or rewritten: an IRI that cannot be
 * written as {@code IRIREF} is rejected with an
 * {@link IllegalArgumentException} instead of being turned into a query.
 */
public final class SPARQLTextGuard {

	/*
	 * Call sites that put an IRI into SPARQL text:
	 * - SPARQLUtil.queryRepository(Repository, ParsedQuery) and SPARQLUtil.queryNamedGraph:
	 *   guarded here (AgentModelManager describe / init-knowledge queries).
	 * - MappingUtil.getTriplesMaps (MappingPlugin): concatenates java.net.URI values, which cannot
	 *   contain the characters rejected here; not guarded.
	 * - RDFBeanManager inverse-property CONSTRUCT (rdfbeans): concatenates property and resource
	 *   IRIs of already parsed data; not guarded (open).
	 * Through the public REST API the IllegalArgumentException is defence in depth: every RDF parser
	 * RDFConsumer accepts (Turtle, N-Triples, RDF/XML, JSON-LD, binary RDF) already rejects these
	 * IRI characters, so a request cannot reach it and no 4xx mapping is needed.
	 * One entry point bypasses the RDF parsers: the MOSIMPlugin Thrift endpoint
	 * (MAJANServiceHandler.CreateAgent -> AgentManager.createAgent(id, template, ...) ->
	 * ParameterAgentBuilder.setAgentTemplate) turns an unparsed string into the template IRI. That
	 * IRI reaches SPARQL text only via AgentModelManager.getTemplateFromTDB ->
	 * SPARQLUtil.queryRepository, i.e. through this guard; the exception surfaces as a Thrift error.
	 */
	private static final String FORBIDDEN_IN_IRIREF = "<>\"{}|^`\\";
	private static final int LAST_CONTROL_OR_SPACE = 0x20;

	private SPARQLTextGuard() {
	}

	/**
	 * @param iri the IRI string about to be written between {@code <} and {@code >}
	 * @return {@code iri}, unchanged
	 * @throws IllegalArgumentException if {@code iri} does not match {@code IRIREF}
	 */
	public static String requireIriRef(final String iri) {
		if (iri == null) {
			throw new IllegalArgumentException("IRI must not be null");
		}
		for (int i = 0; i < iri.length(); i++) {
			char c = iri.charAt(i);
			if (c <= LAST_CONTROL_OR_SPACE || FORBIDDEN_IN_IRIREF.indexOf(c) >= 0) {
				throw new IllegalArgumentException("Not a valid SPARQL IRIREF (character U+"
						+ String.format("%04X", (int) c) + " at index " + i + "): " + iri);
			}
		}
		return iri;
	}

	/**
	 * Checks every IRI that {@code SPARQLQueryRenderer} writes into the query
	 * text: IRI-valued constants and variables, literal datatypes, function
	 * IRIs and the dataset's FROM / FROM NAMED graphs.
	 * VALUES bindings and RDF-star triple terms are not inspected; no caller builds such queries.
	 *
	 * @param query the query about to be rendered
	 * @throws IllegalArgumentException if any of these IRIs is not a valid {@code IRIREF}
	 */
	public static void requireRenderableIris(final ParsedQuery query) {
		query.getTupleExpr().visit(new IriCollector());
		Dataset dataset = query.getDataset();
		if (dataset != null) {
			dataset.getDefaultGraphs().forEach(graph -> requireIriRef(graph.stringValue()));
			dataset.getNamedGraphs().forEach(graph -> requireIriRef(graph.stringValue()));
		}
	}

	private static void requireRenderable(final Value value) {
		if (value instanceof IRI) {
			requireIriRef(value.stringValue());
		} else if (value instanceof Literal) {
			requireIriRef(((Literal) value).getDatatype().stringValue());
		}
	}

	private static final class IriCollector extends AbstractQueryModelVisitor<RuntimeException> {

		@Override
		public void meet(final ValueConstant node) {
			requireRenderable(node.getValue());
			super.meet(node);
		}

		@Override
		public void meet(final Var node) {
			if (node.hasValue()) {
				requireRenderable(node.getValue());
			}
			super.meet(node);
		}

		@Override
		public void meet(final FunctionCall node) {
			requireIriRef(node.getURI());
			super.meet(node);
		}
	}
}
