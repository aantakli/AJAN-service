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

import java.util.Collections;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * SPARQL injection through IRIs that are interpolated into SPARQL text.
 *
 * <p>The IRIs come from client-supplied agent-template RDF
 * (AgentModelManager.getTemplateFromTDB / getAgentInitKnowledge /
 * getInitKnowledgeFromGraph). RDF4J's {@code RenderUtils.toSPARQL} writes an
 * IRI as {@code "<" + iri + ">"} without escaping, and
 * {@link SPARQLUtil#queryNamedGraph(String)} concatenates the same way, so an
 * IRI containing {@code >} can rewrite the query. The repository used here is
 * an in-memory {@link SailRepository}; {@code queryRepository(Repository, ...)}
 * nevertheless goes through the very same rendered SPARQL text that is sent to
 * a remote (HTTP) repository, so the leak shown here is the remote leak.
 */
public class SPARQLUtilInjectionTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final IRI P = VF.createIRI("http://ajan.test/p");
	private static final IRI A = VF.createIRI("http://ajan.test/a");
	private static final IRI SECRET = VF.createIRI("http://ajan.test/secret");
	private static final IRI GRAPH_A = VF.createIRI("http://ajan.test/graphA");
	private static final IRI GRAPH_SECRET = VF.createIRI("http://ajan.test/graphSecret");

	// Closes the sameTerm(<...>, ?var) the renderer opens, ORs in `true` and
	// reopens a sameTerm(<... so every occurrence of the IRI stays balanced:
	//   sameTerm(<http://ajan.test/a>, ?v) || true || sameTerm(<http://ajan.test/a>, ?v)
	// -> the describe filter becomes a tautology, every statement matches.
	private static final String DESCRIBE_PAYLOAD =
			"http://ajan.test/a>, ?descr_subj) || true || sameTerm(<http://ajan.test/a";

	// GRAPH <http://ajan.test/graphA> { GRAPH ?leak { ?s ?p ?o } } } #> { ?s ?p ?o }}
	// The inner GRAPH ?leak overrides the outer graph and ranges over every
	// named graph; '#' comments out the rest of the concatenated query.
	private static final String NAMED_GRAPH_PAYLOAD =
			"http://ajan.test/graphA> { GRAPH ?leak { ?s ?p ?o } } } #";

	private static final Literal PUBLIC = VF.createLiteral("public");
	private static final Literal PUBLIC_IN_GRAPH = VF.createLiteral("public-in-graph");
	private static final Literal SECRET_VALUE = VF.createLiteral("secret");
	private static final Literal SECRET_IN_GRAPH = VF.createLiteral("secret-in-graph");

	private static Repository repository(final Resource subject, final Resource graph) {
		SailRepository repo = new SailRepository(new MemoryStore());
		repo.init();
		Repositories.consume(repo, conn -> conn.add(model(subject, graph)));
		return repo;
	}

	private static Model model(final Resource subject, final Resource graph) {
		Model model = new LinkedHashModel();
		model.add(subject, P, PUBLIC);
		model.add(SECRET, P, SECRET_VALUE);
		model.add(subject, P, PUBLIC_IN_GRAPH, graph);
		model.add(SECRET, P, SECRET_IN_GRAPH, GRAPH_SECRET);
		return model;
	}

	@Test
	public void describeWithInjectedIriDoesNotLeakRepository() {
		Repository repo = repository(A, GRAPH_A);
		ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(
				Collections.<Resource>singletonList(VF.createIRI(DESCRIBE_PAYLOAD)).iterator());
		Model result;
		try {
			result = SPARQLUtil.queryRepository(repo, query);
		} catch (IllegalArgumentException expected) {
			return;
		} finally {
			repo.shutDown();
		}
		assertFalse(result.contains(SECRET, null, null),
				"injected describe IRI leaked an unrelated resource: " + result);
	}

	@Test
	public void namedGraphWithInjectedIriDoesNotLeakOtherGraphs() {
		Repository repo = repository(A, GRAPH_A);
		Model result;
		try {
			result = SPARQLUtil.queryRepository(repo, SPARQLUtil.queryNamedGraph(NAMED_GRAPH_PAYLOAD));
		} catch (IllegalArgumentException expected) {
			return;
		} finally {
			repo.shutDown();
		}
		assertFalse(result.contains(SECRET, null, null),
				"injected graph IRI leaked another named graph: " + result);
	}

	@Test
	public void namedGraphWithInjectedIriDoesNotLeakOtherGraphsOnTheModelPath() {
		// AgentModelManager.getInitKnowledgeFromGraph(repo=false) runs the same
		// concatenated text through queryModel(Model, String).
		Model result;
		try {
			result = SPARQLUtil.queryModel(model(A, GRAPH_A), SPARQLUtil.queryNamedGraph(NAMED_GRAPH_PAYLOAD));
		} catch (IllegalArgumentException expected) {
			return;
		}
		assertFalse(result.contains(SECRET, null, null),
				"injected graph IRI leaked another named graph: " + result);
	}

	@DataProvider
	public Object[][] legitimateIris() {
		return new Object[][] {
			{"http://ajan.test/r?x=1&y=a,b;c"},
			{"http://ajan.test/r#frag-ment"},
			{"http://ajan.test/r%20x/caf%C3%A9"},
			{"http://ajan.test/café/日本"},
			{"urn:uuid:6e8bc430-9c3a-11d9-9669-0800200c9a66"},
			{"http://user@ajan.test:8090/a/b/c?q=(1)*2+3!$'~"}
		};
	}

	@Test(dataProvider = "legitimateIris")
	public void legitimateIrisStillWork(final String iri) {
		IRI subject = VF.createIRI(iri);
		IRI graph = VF.createIRI(iri + (iri.contains("#") ? "-g" : "#g"));
		Repository repo = repository(subject, graph);
		try {
			Model described = SPARQLUtil.queryRepository(repo, SPARQLUtil.getDescribeQuery(
					Collections.<Resource>singletonList(subject).iterator()));
			assertEquals(described.size(), 2, iri + ": describe must return the subject's statements");
			assertTrue(described.contains(subject, P, PUBLIC), iri);
			assertTrue(described.contains(subject, P, PUBLIC_IN_GRAPH), iri);

			Model graphModel = SPARQLUtil.queryRepository(repo, SPARQLUtil.queryNamedGraph(graph.stringValue()));
			assertEquals(graphModel.size(), 1, iri + ": named graph must return exactly its statement");
			assertTrue(graphModel.contains(subject, P, PUBLIC_IN_GRAPH), iri);
		} finally {
			repo.shutDown();
		}
		Model viaModel = SPARQLUtil.queryModel(model(subject, graph), SPARQLUtil.queryNamedGraph(graph.stringValue()));
		assertEquals(viaModel.size(), 1, iri + ": model path must return exactly the graph's statement");
	}
}
