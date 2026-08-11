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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.MultiProjection;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.queryrender.QueryRenderer;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports", "PMD.GodClass"})
public final class SPARQLUtil {

	private final static String INSERT = "(INSERT((?:.*?\\r?\\n?)*)})|(insert((?:.*?\\r?\\n?)*)})";
	private final static String DELETE = "(DELETE((?:.*?\\r?\\n?)*)})|(delete((?:.*?\\r?\\n?)*)})";
	private static final ValueFactory VF = SimpleValueFactory.getInstance();
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
		Model resultModel = queryRepository(repo, query);
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

	public static Model createModel(final String statements, final RDFFormat format) throws IOException {
		InputStream input = new ByteArrayInputStream(statements.getBytes(Charset.forName("UTF-8")));
		return Rio.parse(input, "", format);
	}

	public static SailRepository createRepository(final Model model) {
		SailRepository repo = new SailRepository(new MemoryStore());
		repo.init();
		Repositories.consume(repo, conn -> conn.add(model));
		return repo;
	}

	public static Model queryRepository(final Repository repo, final ParsedQuery query) {
		// `repo` is an arbitrary org.eclipse.rdf4j.repository.Repository, not
		// necessarily a SailRepository -- it may be a remote HTTP-backed
		// repository (e.g. an external TDB), for which the SPARQL protocol
		// over HTTP is the only way to query it, so rendering to text here is
		// unavoidable, not a choice. This already rendered before this etappe
		// (queryModel(Model, ParsedGraphQuery/ParsedTupleQuery) below do not
		// anymore, see there); it is therefore not a regression introduced by
		// this refactor. It remains exposed to renderQuery()'s use of
		// RenderUtils.toSPARQL(Value), which interpolates an IRI's string value
		// between "<" and ">" with no escaping (RDF4J 3.6.3 source,
		// org.eclipse.rdf4j.queryrender.RenderUtils) -- a resource value with
		// unusual characters can still corrupt the rendered query text on this
		// path. Not addressed here; the fix would require this method to stop
		// talking to arbitrary (including remote) repositories via SPARQL text,
		// which is out of this task's scope.
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
			StringBuilder select = new StringBuilder();
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
		ParsedTupleQuery query = SPARQLUtil.getSelectQuery(tupleExpr, list);
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
	// Algebra weiterhin zu Text (siehe dortiger Kommentar) und bleibt daher exponiert.
	// Byte-Identitaet zur alten QueryBuilderFactory-Ausgabe (ein/zwei Ressourcen, BNode)
	// ist verifiziert vom committed Differential-Harness unter
	// spikes/etappe3-query-equivalence -- nicht von SPARQLUtilQueryTest.
	public static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
		String subjVar = "descr_subj";
		String predVar = "descr_pred";
		String objVar = "descr_obj";
		StatementPattern pattern = new StatementPattern(new Var(subjVar), new Var(predVar), new Var(objVar));
		Filter filter = new Filter(pattern, getDescribeFilter(resourceIterator, subjVar, objVar));
		ProjectionElemList projectionElems = new ProjectionElemList();
		projectionElems.addElement(new ProjectionElem(subjVar, "subject"));
		projectionElems.addElement(new ProjectionElem(predVar, "predicate"));
		projectionElems.addElement(new ProjectionElem(objVar, "object"));
		MultiProjection projection = new MultiProjection();
		projection.addProjection(projectionElems);
		projection.setArg(filter);
		return new ParsedGraphQuery(projection);
	}

	private static ValueExpr getDescribeFilter(final Iterator<Resource> resourceIterator, final String subjVar, final String objVar) {
		Resource resource = resourceIterator.next();
		if (resourceIterator.hasNext()) {
			return new Or(getSameTerm(resource, subjVar),
					new Or(getSameTerm(resource, objVar), getDescribeFilter(resourceIterator, subjVar, objVar)));
		} else {
			return new Or(getSameTerm(resource, subjVar), getSameTerm(resource, objVar));
		}
	}

	private static ValueExpr getSameTerm(final Resource resource, final String var) {
		return new SameTerm(new ValueConstant(resource), new Var(var));
	}

	public static String queryNamedGraph(final String graphName) {
		return "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphName + "> { ?s ?p ?o }}";
	}

	public static Model getNamedGraph(final Model resultModel) {
		Model model = resultModel;
		if (model.contains(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null)) {
			Optional<IRI> contextIRI = Models.objectIRI(model.filter(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null));
			if (contextIRI.isPresent()) {
				IRI context = contextIRI.get();
				model = addUniqueNamedGraph(model, context);
			}
			model.remove(null, AJANVocabulary.HAS_RDF_UUID_CONTEXT, null);
		}
		if (model.contains(null, AJANVocabulary.HAS_RDF_CONTEXT, null)) {
			Optional<IRI> contextIRI = Models.objectIRI(model.filter(null, AJANVocabulary.HAS_RDF_CONTEXT, null));
			if (contextIRI.isPresent()) {
				IRI context = contextIRI.get();
				model = addNamedGraph(model, context);
			}
			model.remove(null, AJANVocabulary.HAS_RDF_CONTEXT, null);
		}
		return model;
	}

	private static Model addNamedGraph(final Model model, final IRI context) {
		if (context != null) {
			String ctx = context.toString();
			model.add(VF.createIRI(ctx), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, AJANVocabulary.CONTEXT);
			return AgentUtil.setNamedGraph(model.iterator(), ctx);
		}
		return model;
	}

	private static Model addUniqueNamedGraph(final Model model, final IRI context) {
		if (context != null) {
			String ctx = context.toString();
			String graphName = ctx + "_" + UUID.randomUUID().toString();
			model.add(VF.createIRI(ctx), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, AJANVocabulary.UUID_CONTEXT);
			model.add(VF.createIRI(graphName), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, VF.createIRI(ctx));
			return AgentUtil.setNamedGraph(model.iterator(), graphName);
		}
		return model;
	}
}
