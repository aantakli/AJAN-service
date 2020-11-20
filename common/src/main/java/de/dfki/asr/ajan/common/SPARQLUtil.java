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
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.queryrender.QueryRenderer;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailQueryPreparer;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports"})
public final class SPARQLUtil {

	private final static String INSERT = "(INSERT((?:.*?\\r?\\n?)*)})|(insert((?:.*?\\r?\\n?)*)})";
	private final static String DELETE = "(DELETE((?:.*?\\r?\\n?)*)})|(delete((?:.*?\\r?\\n?)*)})";
	//private final static String WHERE = "(WHERE((?:.*?\\r?\\n?)*)})";

	private SPARQLUtil() {

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

	public static Model queryModel(final Model model, final String query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		Model resultModel = queryRepository(repo, query);
		repo.shutDown();
		return resultModel;
	}

	public static Model queryModel(final Model model, final ParsedGraphQuery query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		Model resultModel;
		try (SailRepositoryConnection conn = repo.getConnection()) {
			conn.begin();
			SailQueryPreparer preparer = new SailQueryPreparer(conn, false);
			GraphQueryResult results = preparer.prepare(query).evaluate();
			resultModel = QueryResults.asModel(results);
			conn.commit();
		}
		repo.shutDown();
		return resultModel;
	}

	public static List<BindingSet> queryModel(final Model model, final ParsedTupleQuery query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		List<BindingSet> resultModel;
		try (SailRepositoryConnection conn = repo.getConnection()) {
			conn.begin();
			SailQueryPreparer preparer = new SailQueryPreparer(conn, false);
			TupleQueryResult results = preparer.prepare(query).evaluate();
			resultModel = getBindingSetList(results);
			conn.commit();
		}
		repo.shutDown();
		return resultModel;
	}

	private static List<BindingSet> getBindingSetList(final TupleQueryResult result) {
		List<BindingSet> resultList = new ArrayList<>();
		while (result.hasNext()) {
			resultList.add(result.next());
		}
		return resultList;
	}

	public static Model createModel(final String statements, final RDFFormat format) throws IOException {
		InputStream input = new ByteArrayInputStream(statements.getBytes(Charset.forName("UTF-8")));
		return Rio.parse(input, "", format);
	}

	public static SailRepository createRepository(final Model model) {
		SailRepository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		Repositories.consume(repo, conn -> conn.add(model));
		return repo;
	}

	public static Model queryRepository(final Repository repo, final ParsedQuery query) {
		String renderedQuery = renderQuery(query);
		return queryRepository(repo, renderedQuery);
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static String renderQuery(final ParsedQuery query) throws QueryEvaluationException {
		QueryRenderer renderer = new SPARQLQueryRenderer();
		String renderedQuery;
		try {
			renderedQuery = renderer.render(query); // QueryRenderer throws EXCEPTION if there is an error while rendering, no other Exception supported
		} catch (Exception ex) {
			throw new QueryEvaluationException(ex);
		}
		return renderedQuery;
	}

	public static Model queryRepository(final Repository repo, final String query) {
		return Repositories.graphQuery(repo, query, r -> QueryResults.asModel(r));
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

	public static String getSelectQuery(final String query) {
		String upper = query.toUpperCase();
		if (upper.contains("CLEAR")) {
			return "SELECT DISTINCT * WHERE { ?s ?p ?o }";
		}
		String minInsert = query;
		String select = "SELECT DISTINCT * ";
		if (upper.contains("DELETE")) {
			minInsert = minInsert.replaceAll(DELETE,select);
			select = "";
		}
		if (upper.contains("INSERT")) {
			minInsert = minInsert.replaceAll(INSERT,select);
		}
		return minInsert;
	}

	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	public static String getSelectQuery(final TupleExpr tupleExpr) throws Exception {
		List<String> list = new ArrayList(tupleExpr.getBindingNames());
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(tupleExpr, list);
		return new SPARQLQueryRenderer().render(query);
	}

	public static ParsedTupleQuery getSelectQuery(final String query, final List<String> varNames) {
		TupleExpr tupleExpr = getTupleExpr(query);
		return getSelectQuery(tupleExpr, varNames);
	}

	public static ParsedTupleQuery getSelectQuery(final TupleExpr tupleExpr, final List<String> varNames) {
		QueryBuilder<ParsedTupleQuery> builder = QueryBuilderFactory.select();
		builder.addProjectionVar(varNames.toArray(new String[varNames.size()]));
		builder.group();
		ParsedTupleQuery parsedQuery = builder.query();
		parsedQuery.setTupleExpr(tupleExpr);
		return parsedQuery;
	}

	public static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
		QueryBuilder<ParsedGraphQuery> builder = QueryBuilderFactory.construct();
		setDescribeQueryParameters(builder, resourceIterator);
		return builder.query();
	}

	private static void setDescribeQueryParameters(final QueryBuilder<ParsedGraphQuery> builder, final Iterator<Resource> resourceIterator) {
		String subj = "descr_subj";
		String pred = "descr_pred";
		String obj = "descr_obj";
		builder.addProjectionStatement(subj, pred, obj);
		builder.group().atom(subj, pred, obj);
		builder.group().filter(setDescribeFilter(resourceIterator, subj, obj));
	}

	private static ValueExpr setDescribeFilter(final Iterator<Resource> resourceIterator, final String subj, final String obj) {
		Resource resource = resourceIterator.next();
		if (resourceIterator.hasNext()) {
			return new Or( setSameTerm(resource, subj), new Or( setSameTerm(resource, obj), setDescribeFilter(resourceIterator, subj, obj)));
		} else {
			return new Or( setSameTerm(resource, subj), setSameTerm(resource, obj));
		}
	}

	private static ValueExpr setSameTerm(final Resource resource, final String var) {
		return new SameTerm(new ValueConstant(resource), new Var(var));
	}

	public static String queryNamedGraph (final String graphName) {
		return "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphName + "> { ?s ?p ?o }}";
	}
}
