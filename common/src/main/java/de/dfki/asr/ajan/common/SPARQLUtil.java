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

import de.dfki.asr.ajan.common.exceptions.AdaptSPARQLQueryException;
import de.dfki.asr.ajan.common.exceptions.TripleStoreException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.queryrender.QueryRenderer;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports", "PMD.GodClass"})
public final class SPARQLUtil {

	private final static String INSERT = "(INSERT((?:.*?\\r?\\n?)*)})|(insert((?:.*?\\r?\\n?)*)})";
	private final static String DELETE = "(DELETE((?:.*?\\r?\\n?)*)})|(delete((?:.*?\\r?\\n?)*)})";
	//private final static String WHERE = "(WHERE((?:.*?\\r?\\n?)*)})";

	private SPARQLUtil() {

	}

	public static boolean askModel(final Model model, final String query) {
		return ModelQueries.askModel(model, query);
	}

	public static List<BindingSet> assignVariables(final Model model, final String askString, final List<String> variables) {
		return ModelQueries.assignVariables(model, askString, variables);
	}

	public static Model queryModel(final Model model, final String query) throws QueryEvaluationException {
		return ModelQueries.queryModel(model, query);
	}

	public static Model queryModel(final Model model, final ParsedGraphQuery query) throws QueryEvaluationException {
		return ModelQueries.queryModel(model, query);
	}

	public static List<BindingSet> queryModel(final Model model, final ParsedTupleQuery query) throws QueryEvaluationException {
		return ModelQueries.queryModel(model, query);
	}

	public static Model createModel(final String statements, final RDFFormat format) throws IOException {
		InputStream input = new ByteArrayInputStream(statements.getBytes(Charset.forName("UTF-8")));
		return Rio.parse(input, "", format);
	}

	public static SailRepository createRepository(final Model model) {
		return ModelQueries.createRepository(model);
	}

	public static Model queryRepository(final Repository repo, final ParsedQuery query) {
		// `repo` is an arbitrary org.eclipse.rdf4j.repository.Repository, not
		// necessarily a SailRepository -- it may be a remote HTTP-backed
		// repository (e.g. an external TDB), for which the SPARQL protocol
		// over HTTP is the only way to query it, so rendering to text here is
		// unavoidable, not a choice. RDF4J's RenderUtils.toSPARQL(Value) writes
		// an IRI as "<" + iri + ">" with no escaping, so renderQuery() first
		// validates every IRI it is about to render against the SPARQL IRIREF
		// grammar (SPARQLTextGuard) and rejects the query with an
		// IllegalArgumentException instead of rendering a rewritten one.
		String renderedQuery = renderQuery(query);
		return queryRepository(repo, renderedQuery);
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static String renderQuery(final ParsedQuery query) throws QueryEvaluationException {
		// Outside the try: an invalid IRI must surface as IllegalArgumentException,
		// not be wrapped like a renderer failure.
		SPARQLTextGuard.requireRenderableIris(query);
		QueryRenderer renderer = new SPARQLQueryRenderer();
		String renderedQuery;
		try {
			renderedQuery = renderer.render(query); // QueryRenderer throws EXCEPTION if there is an error while rendering, no other Exception supported
		} catch (Exception ex) {
			throw new QueryEvaluationException(ex);
		}
		return renderedQuery;
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static Model queryRepository(final Repository repo, final String query) throws TripleStoreException {
		try {
			return Repositories.graphQuery(repo, query, r -> QueryResults.asModel(r));
		} catch (Exception ex) {
			throw new TripleStoreException("Unable to query repository!", ex);
		}
	}

	public static TupleExpr getTupleExpr(final String query) {
		SPARQLParser parser = new SPARQLParser();
		ParsedQuery parsedQuery = parser.parseQuery(query, "http://localhost:8090/ajan");
		return parsedQuery.getTupleExpr();
	}

	public static List<UpdateExpr> getUpdateExpr(final String query) {
		SPARQLParser parser = new SPARQLParser();
		ParsedUpdate parsedUpdate = parser.parseUpdate(query, "http://localhost:8090/ajan");
		return parsedUpdate.getUpdateExprs() ;
	}

	public static TupleExpr getTupleExpr(final List<UpdateExpr> updateExpr) {
		TupleExprRenderer visitor = new TupleExprRenderer(updateExpr);
		return visitor.render();
	}

	public static String getSelectQueryFromUpdate(final String query) {
		String upper = query.toUpperCase();
		if (upper.contains("CLEAR")) {
			return "SELECT DISTINCT * WHERE { ?s ?p ?o }";
		}
		String minInsert = query;
		String select = "SELECT DISTINCT * ";
		if (upper.contains("DELETE {")) {
			minInsert = minInsert.replaceAll(DELETE,select);
			select = "";
		}
		if (upper.contains("INSERT {")) {
			minInsert = minInsert.replaceAll(INSERT,select);
		}
		return minInsert;
	}

	public static String getSelectQueryFromAsk(final String query, final List<String> variables) throws AdaptSPARQLQueryException {
		String upper = query.toUpperCase();
		if (upper.contains("ASK")) {
			StringBuilder select = new StringBuilder(64);
			select.append("SELECT DISTINCT ");
			for (String variableName: variables) {
				select.append('?').append(variableName).append(' ');
			}
			String minInsert = query;
			return minInsert.replaceAll("ASK", select.toString());
		} else {
			throw new AdaptSPARQLQueryException("NO valid ASK Query: " + query);
		}
	}

	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public static String getSelectQuery(final TupleExpr tupleExpr) throws Exception {
		List<String> list = new ArrayList(tupleExpr.getBindingNames());
		ParsedTupleQuery query = getSelectQuery(tupleExpr, list);
		SPARQLTextGuard.requireRenderableIris(query);
		return new SPARQLQueryRenderer().render(query);
	}

	public static ParsedTupleQuery getSelectQuery(final String query, final List<String> varNames) {
		// query may be a SELECT or (per the only reachable production caller,
		// ACTNUtil.createSelectQuery <- vocabularies/actn.ttl) an ASK query;
		// getTupleExpr parses either and returns the WHERE-clause algebra
		// (for ASK, including the implicit LIMIT 1). No SPARQL-text
		// preservation is needed here: queryModel(Model, ParsedTupleQuery)
		// evaluates this algebra directly against the Sail connection and
		// never renders it back to text, so the ASK/SELECT distinction -
		// which only exists at the text/grammar level - never comes up.
		TupleExpr tupleExpr = getTupleExpr(query);
		return getSelectQuery(tupleExpr, varNames);
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	public static ParsedTupleQuery getSelectQuery(final TupleExpr tupleExpr, final List<String> varNames) {
		// Frueher ueber QueryBuilderFactory.select(): dessen Projektion wurde
		// unmittelbar durch setTupleExpr(tupleExpr) ueberschrieben, varNames
		// blieb also ohne Wirkung. Der Konstruktor bildet das 1:1 ab und
		// existiert in RDF4J 3.6 wie 5.x.
		return new ParsedTupleQuery(tupleExpr);
	}

	// Frueher ueber QueryBuilderFactory.construct(); org.eclipse.rdf4j.queryrender.
	// builder existiert in RDF4J 5 nicht mehr, die Algebra-Klassen darunter (Filter,
	// MultiProjection, Or, SameTerm, ValueConstant, Var, ...) aber schon. Der Baum wird
	// deshalb direkt aus diesen Klassen gebaut, nicht als SPARQL-Text interpoliert.
	// SICHERHEIT HAENGT VOM AUSFUEHRENDEN PFAD AB, nicht von dieser Methode allein:
	// queryModel(Model, ParsedGraphQuery) wertet diese Algebra direkt gegen die
	// SailConnection aus und rendert nie -- dort schliesst das Injection aus und
	// erhaelt BNode-Semantik. queryRepository(Repository, ParsedQuery) rendert dieselbe
	// Algebra weiterhin zu Text (siehe dortiger Kommentar); dort verhindert die
	// IRIREF-Pruefung in renderQuery() (SPARQLTextGuard), dass eine IRI die Query umschreibt.
	// Byte-Identitaet zur alten QueryBuilderFactory-Ausgabe (ein/zwei Ressourcen, BNode)
	// ist verifiziert vom committed Differential-Harness unter
	// spikes/etappe3-query-equivalence -- nicht von SPARQLUtilQueryTest.
	public static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
		return DescribeQueries.getDescribeQuery(resourceIterator);
	}

	public static String queryNamedGraph(final String graphName) {
		// graphName comes from client-supplied agent-template RDF and is
		// concatenated into the query text: it must be a valid IRIREF, or it
		// could close GRAPH <...> early and continue as query syntax.
		SPARQLTextGuard.requireIriRef(graphName);
		return "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphName + "> { ?s ?p ?o }}";
	}

	public static Model getNamedGraph(final Model resultModel) {
		return NamedGraphs.getNamedGraph(resultModel);
	}
}
