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
 * Charakterisierung der SPARQLUtil-Query-Pfade, die beim RDF4J-Umstieg
 * 3.6.3 -> 5.3.1 umgebaut werden muessen (die alte Query-Builder-Factory und
 * der alte Sail-Query-Preparer existieren in RDF4J 5 nicht mehr). Die Tests
 * sind vor dem Umbau gegen die alte Implementierung geschrieben und muessen
 * danach unveraendert gruen bleiben.
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
}
