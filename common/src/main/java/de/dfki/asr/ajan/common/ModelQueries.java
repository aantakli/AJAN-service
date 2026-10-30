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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * Internal helper; the public facade is {@link SPARQLUtil}. It evaluates queries against in-memory models.
 */
final class ModelQueries {

	private ModelQueries() {

	}

	public static boolean askModel(final Model model, final String query) {
		SailRepository repo = createRepository(model);
		boolean result;
		try (RepositoryConnection conn = repo.getConnection()) {
			BooleanQuery parsedQuery = conn.prepareBooleanQuery(query);
			result = parsedQuery.evaluate();
		}
		repo.shutDown();
		return result;
	}

	public static List<BindingSet> assignVariables(final Model model, final String askString, final List<String> variables) {
		SailRepository repo = createRepository(model);
		List<BindingSet> bindings = new ArrayList();
		try (RepositoryConnection conn = repo.getConnection()) {
			String selectString = SPARQLUtil.getSelectQueryFromAsk(askString, variables);
			TupleQueryResult list = conn.prepareTupleQuery(selectString).evaluate();
			while (list.hasNext()) {
				bindings.add(list.next());
			}
		}
		repo.shutDown();
		return bindings;
	}

	public static Model queryModel(final Model model, final String query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		Model resultModel = SPARQLUtil.queryRepository(repo, query);
		repo.shutDown();
		return resultModel;
	}

	public static Model queryModel(final Model model, final ParsedGraphQuery query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		Model resultModel = new LinkedHashModel();
		try (SailRepositoryConnection conn = repo.getConnection()) {
			conn.begin();
			// See evaluateAlgebra() for why this never renders to SPARQL text.
			// Bindings -> Statement (subject/predicate/object/context, type-
			// checked before use) replicates SailGraphQuery.evaluate() exactly
			// (RDF4J 3.6.3 source, see fix report). Because this never renders,
			// it is safe from SPARQL injection regardless of resource content
			// and evaluates a BNode value as object identity, not as text.
			evaluateAlgebra(conn, query.getTupleExpr(), query.getDataset(),
					bindingSet -> addStatementIfWellTyped(resultModel, bindingSet));
			conn.commit();
		}
		repo.shutDown();
		return resultModel;
	}

	private static void addStatementIfWellTyped(final Model resultModel, final BindingSet bindingSet) {
		Value subject = bindingSet.getValue("subject");
		Value predicate = bindingSet.getValue("predicate");
		Value object = bindingSet.getValue("object");
		Value context = bindingSet.getValue("context");
		boolean wellTyped = subject instanceof Resource && predicate instanceof IRI && object != null
				&& (context == null || context instanceof Resource);
		if (wellTyped) {
			if (context == null) {
				resultModel.add((Resource) subject, (IRI) predicate, object);
			} else {
				resultModel.add((Resource) subject, (IRI) predicate, object, (Resource) context);
			}
		}
	}

	public static List<BindingSet> queryModel(final Model model, final ParsedTupleQuery query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		List<BindingSet> resultModel = new ArrayList<>();
		try (SailRepositoryConnection conn = repo.getConnection()) {
			conn.begin();
			// See evaluateAlgebra() for why this never renders to SPARQL text.
			// This is required, not just preferred: the only reachable ACTN
			// precondition query is an ASK query (vocabularies/actn.ttl), and
			// conn.prepareTupleQuery(text) rejects any text that does not start
			// with SELECT -- an ASK's algebra has no such restriction, so
			// evaluating it directly here works unchanged.
			evaluateAlgebra(conn, query.getTupleExpr(), query.getDataset(), resultModel::add);
			conn.commit();
		}
		repo.shutDown();
		return resultModel;
	}

	// RDF4J 5 hat org.eclipse.rdf4j.repository.sail.SailQueryPreparer entfernt, das
	// intern SailGraphQuery/SailTupleQuery.evaluate() genau so auswertete: TupleExpr
	// direkt gegen die SailConnection auswerten, nie als Text rendern. `var` ist hier
	// erforderlich, weil CloseableIteration in RDF4J 3.6.3 zwei Typparameter hat und in
	// 5.3.1 nur noch einen -- ein ausgeschriebener Typ waere nicht mehr in beiden
	// Versionen compilierbar.
	private static void evaluateAlgebra(final SailRepositoryConnection conn, final TupleExpr tupleExpr,
			final Dataset dataset, final Consumer<BindingSet> consumer) {
		var bindingsIter = conn.getSailConnection().evaluate(tupleExpr, dataset, EmptyBindingSet.getInstance(), false);
		try {
			while (bindingsIter.hasNext()) {
				consumer.accept(bindingsIter.next());
			}
		} finally {
			bindingsIter.close();
		}
	}

	public static SailRepository createRepository(final Model model) {
		SailRepository repo = new SailRepository(new MemoryStore());
		repo.init();
		Repositories.consume(repo, conn -> conn.add(model));
		return repo;
	}
}
