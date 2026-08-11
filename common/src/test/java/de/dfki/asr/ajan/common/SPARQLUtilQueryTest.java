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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.rio.RDFFormat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Charakterisierung der SPARQLUtil-Query-Pfade fuer den RDF4J-Umstieg
 * 3.6.3 -> 5.3.1. Fix-Runde 2: queryModel wertet die Algebra jetzt direkt
 * gegen die SailConnection aus statt sie zu Text zu rendern (siehe
 * spikes/etappe3-query-equivalence fuer den vollen Old-vs-New-Vergleich).
 * Neu: ASK-als-Quellquery (echter ACTN-Fall), eine feindliche IRI und ein
 * jetzt korrekt matchendes Blank-Node-Describe.
 */
public class SPARQLUtilQueryTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final IRI A = VF.createIRI("http://ajan.test/a");
	private static final IRI B = VF.createIRI("http://ajan.test/b");
	private static final IRI C = VF.createIRI("http://ajan.test/c");

	private static final String TURTLE_DATA =
			"@prefix t: <http://ajan.test/> .\n"
			+ "t:a t:p t:b .\n"
			+ "t:b t:q \"literal\" .\n"
			+ "t:c t:r t:a .\n";

	private Model data() throws IOException {
		return SPARQLUtil.createModel(TURTLE_DATA, RDFFormat.TURTLE);
	}

	@Test
	public void describeQueryForSingleResourceReturnsIncomingAndOutgoingStatements() throws IOException {
		Model model = data();
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(A).iterator());
		Model result = SPARQLUtil.queryModel(model, query);
		assertEquals(result.size(), 2, "describe(a) should yield the two statements touching a");
		assertTrue(result.contains(A, VF.createIRI("http://ajan.test/p"), B), "outgoing statement missing");
		assertTrue(result.contains(C, VF.createIRI("http://ajan.test/r"), A), "incoming statement missing");
	}

	@Test
	public void describeQueryForTwoResourcesUnionsBothDescriptions() throws IOException {
		Model model = data();
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(A, B).iterator());
		Model result = SPARQLUtil.queryModel(model, query);
		assertEquals(result.size(), 3, "describe(a,b) should yield all three statements");
	}

	@Test
	public void describeQueryEvaluatedAgainstRepositoryYieldsSameResult() throws IOException {
		Model model = data();
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(A).iterator());
		Model result = SPARQLUtil.queryRepository(SPARQLUtil.createRepository(model), query);
		assertEquals(result.size(), 2, "repository path must match the in-memory model path");
	}

	@Test
	public void selectQueryFromTupleExprReturnsOneBindingPerStatement() throws IOException {
		Model model = data();
		TupleExpr tupleExpr = SPARQLUtil.getTupleExpr("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
		List<String> vars = new ArrayList<>(Arrays.asList("s", "p", "o"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(tupleExpr, vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 3, "one binding set per statement expected");
		assertTrue(bindings.get(0).hasBinding("s"), "projection variable s must be bound");
	}

	@Test
	public void selectQueryFromQueryStringFiltersByPredicate() throws IOException {
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery("SELECT ?s WHERE { ?s <http://ajan.test/p> ?o }", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 1, "only t:a has predicate t:p");
		assertEquals(bindings.get(0).getValue("s"), A, "subject binding must be t:a");
	}

	@Test
	public void createRepositoryYieldsAnInitialisedUsableRepository() throws IOException {
		Model model = data();
		Model all = SPARQLUtil.queryRepository(SPARQLUtil.createRepository(model),
				"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
		assertEquals(all.size(), 3, "createRepository must return a repository ready for querying");
	}

	@Test
	public void selectQueryWithFilterNotExistsIsNotLostOnTheQueryModelPath() throws IOException {
		// Pins the loss described in fix round 1, finding 1: rendering a
		// ParsedTupleQuery back to SPARQL text via SPARQLQueryRenderer and
		// re-parsing it fails to reproduce FILTER NOT EXISTS (it used to throw
		// MalformedQueryException). getSelectQuery(String, List) now keeps the
		// original source text so queryModel(Model, ParsedTupleQuery) can reuse
		// it instead of rendering.
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(
				"SELECT ?s WHERE { ?s ?p ?o . FILTER NOT EXISTS { ?s <http://ajan.test/q> ?any } }", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 2, "t:a and t:c have no t:q predicate, t:b does and must be excluded");
	}

	@Test
	public void selectQueryWithBindKeepsTheBoundValueOnTheQueryModelPath() throws IOException {
		// Pins the loss described in fix round 1, finding 1: BIND(...AS ?x)
		// used to be silently dropped by the render+reparse round-trip.
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s", "x"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(
				"SELECT ?s ?x WHERE { ?s <http://ajan.test/q> ?o . BIND(STR(?o) AS ?x) }", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 1, "only t:b has predicate t:q");
		assertTrue(bindings.get(0).hasBinding("x"), "BIND-introduced variable x must be bound");
		assertEquals(bindings.get(0).getValue("x").stringValue(), "literal", "?x must carry the bound STR(?o) value");
	}

	// Fix round 2, replaces the round-1 test of the same intent: with the
	// algebra evaluated directly against the SailConnection (never rendered to
	// SPARQL text), a blank node is matched by object identity via SameTerm,
	// exactly as the pre-refactor SailQueryPreparer-based implementation did.
	// The round-1 test asserted the opposite (a thrown MalformedQueryException)
	// because at that point queryModel(Model, ParsedGraphQuery) still rendered
	// the algebra back to text, and SPARQL text has no syntax for addressing a
	// specific pre-existing blank node. That render step is gone now, so this
	// works again. (The round-1 test was also methodologically unsound:
	// TestNG's expectedExceptions is method-scoped, so it could not tell a
	// throw during query construction from one during evaluation, and it did
	// not discriminate between the two anyway.)
	@Test
	public void describeQueryForBlankNodeResourceMatchesTheBlankNodeItself() throws IOException {
		String bnodeData =
				"@prefix t: <http://ajan.test/> .\n"
				+ "t:d t:s _:bn1 .\n"
				+ "_:bn1 t:t t:e .\n";
		Model model = SPARQLUtil.createModel(bnodeData, RDFFormat.TURTLE);
		IRI d = VF.createIRI("http://ajan.test/d");
		IRI s = VF.createIRI("http://ajan.test/s");
		Statement dToBnode = model.filter(d, s, null).iterator().next();
		Resource bnode = (Resource) dToBnode.getObject();
		assertTrue(bnode.isBNode(), "test data must contain a blank node object");
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(bnode).iterator());
		Model result = SPARQLUtil.queryModel(model, query);
		assertEquals(result.size(), 2, "describe(bnode) should yield both statements touching the blank node");
	}

	@Test
	public void selectQueryFromAskQueryReturnsBindingSets() throws IOException {
		// This is the real ACTN production case: vocabularies/actn.ttl defines
		// preconditions as "a SPARQL ASK query" and ACTNUtil.createSelectQuery
		// hands that text straight to SPARQLUtil.getSelectQuery(String, List).
		// Round 1's render-based queryModel broke this outright
		// (IllegalArgumentException: query is not a tuple query) because
		// conn.prepareTupleQuery(text) requires the text to start with SELECT.
		// Evaluating the algebra directly has no such restriction: an ASK's
		// WHERE-clause algebra carries an implicit LIMIT 1 (from the parser),
		// which is asserted here by using a pattern with more than one match.
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s", "o"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery("ASK WHERE { ?s ?p ?o }", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 1, "ASK's implicit LIMIT 1 must still apply when evaluated as a tuple query");
	}

	@Test
	public void describeQueryForHostileIriResourceIsUnaffectedByEmbeddedSyntax() throws IOException {
		// Proves no text path remains reachable from getDescribeQuery + queryModel:
		// a resource whose IRI string contains characters that would corrupt
		// rendered SPARQL text (">" plus a bogus update clause) is matched purely
		// by algebra-level value identity (ValueConstant/SameTerm), never
		// embedded as text. Built directly (not via Rio/Turtle, which validates
		// IRI syntax more strictly than ValueFactory.createIRI does) to mirror
		// the reachable production path: AgentResourceManager.getResources only
		// checks "instanceof Resource", not IRI well-formedness.
		IRI evil = VF.createIRI("http://ajan.test/x> } ; DROP ALL ; # ");
		IRI p = VF.createIRI("http://ajan.test/p");
		IRI r = VF.createIRI("http://ajan.test/r");
		Model model = new org.eclipse.rdf4j.model.impl.LinkedHashModel();
		model.add(evil, p, B);
		model.add(C, r, evil);
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(evil).iterator());
		Model result = SPARQLUtil.queryModel(model, query);
		assertEquals(result.size(), 2, "describe(evil) must still yield exactly the two statements touching it");
		assertTrue(result.contains(evil, p, B), "outgoing statement missing");
		assertTrue(result.contains(C, r, evil), "incoming statement missing");
	}

	@Test
	public void selectQueryWithFilterExistsIsNotLostOnTheQueryModelPath() throws IOException {
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(
				"SELECT ?s WHERE { ?s ?p ?o . FILTER EXISTS { ?s <http://ajan.test/q> ?any } }", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 1, "only t:b has the t:q predicate that FILTER EXISTS requires");
		assertEquals(bindings.get(0).getValue("s"), B, "subject binding must be t:b");
	}

	@Test
	public void selectQueryWithCountGroupByKeepsTheAggregateBound() throws IOException {
		Model model = data();
		List<String> vars = new ArrayList<>(Arrays.asList("s", "n"));
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(
				"SELECT ?s (COUNT(?o) AS ?n) WHERE { ?s ?p ?o } GROUP BY ?s", vars);
		List<BindingSet> bindings = SPARQLUtil.queryModel(model, query);
		assertEquals(bindings.size(), 3, "one group per distinct subject expected");
		for (BindingSet bindingSet : bindings) {
			assertTrue(bindingSet.hasBinding("n"), "aggregate variable n must be bound for every group");
			assertEquals(bindingSet.getValue("n").stringValue(), "1", "each subject has exactly one statement");
		}
	}
}
