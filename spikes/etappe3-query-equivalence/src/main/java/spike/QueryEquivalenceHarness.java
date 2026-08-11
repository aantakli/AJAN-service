package spike;

import de.dfki.asr.ajan.common.SPARQLUtil;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.sail.SailQueryPreparer;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * Fix round 2 (Task 3, Etappe 3) committed differential harness.
 *
 * <p>Compares three implementations of the query-construction/-evaluation
 * paths in {@code common/.../SPARQLUtil.java}, all run against real RDF4J
 * 3.6.3 (the version the whole task must stay dual-compilable with):
 *
 * <ul>
 * <li><b>OLD</b> - reconstructed verbatim from the pre-Task-3 SPARQLUtil.java
 * (git commit {@code 5e90754c}): {@code QueryBuilderFactory} for
 * construction, {@code SailQueryPreparer} for evaluation. Never renders to
 * SPARQL text.</li>
 * <li><b>ROUND1</b> - the fix-round-1 SPARQLUtil.java (git commit
 * {@code 79096bb7}): hand-built algebra for {@code getDescribeQuery}, but
 * evaluation via {@code SPARQLQueryRenderer} + {@code conn.prepare*Query
 * (text)} (renders). Reconstructed here only to make the regression it
 * caused concrete and re-runnable.</li>
 * <li><b>NEW</b> - the real, shipped {@link SPARQLUtil}, used directly (not
 * reimplemented) via the {@code common} Maven dependency declared in this
 * spike's {@code pom.xml}, so this harness tests the exact code that ships.
 * </li>
 * </ul>
 *
 * <p>Run: {@code mvn -f spikes/etappe3-query-equivalence/pom.xml -q compile
 * exec:java} (run {@code mvn install} on the main reactor first so
 * {@code common-0.1.jar} is in the local repository).
 */
public final class QueryEquivalenceHarness {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final IRI A = VF.createIRI("http://ajan.test/a");
	private static final IRI B = VF.createIRI("http://ajan.test/b");
	private static final IRI C = VF.createIRI("http://ajan.test/c");
	private static final IRI P = VF.createIRI("http://ajan.test/p");
	private static final IRI Q = VF.createIRI("http://ajan.test/q");
	private static final IRI R = VF.createIRI("http://ajan.test/r");

	private static final String TURTLE_DATA =
			"@prefix t: <http://ajan.test/> .\n"
			+ "t:a t:p t:b .\n"
			+ "t:b t:q \"literal\" .\n"
			+ "t:c t:r t:a .\n";

	private int passCount;
	private int failCount;

	public static void main(String[] args) throws Exception {
		new QueryEquivalenceHarness().run();
	}

	private void run() throws Exception {
		System.out.println("=== Fix round 2 differential harness: OLD (SailQueryPreparer) vs ROUND1 (render) vs NEW (direct Sail) ===");
		System.out.println();

		caseSelectPlainBgp();
		caseSelectBind();
		caseSelectCountGroupBy();
		caseSelectFilterNotExists();
		caseSelectFilterExists();
		caseSelectFromAsk();
		caseDescribeOneIri();
		caseDescribeTwoIris();
		caseDescribeBlankNode();
		caseDescribeHostileIri();

		System.out.println();
		System.out.println("=== SUMMARY: " + passCount + " passed, " + failCount + " failed ===");
		if (failCount > 0) {
			System.exit(1);
		}
	}

	// ---------------------------------------------------------------
	// Cases
	// ---------------------------------------------------------------

	private void caseSelectPlainBgp() throws Exception {
		header("plain BGP SELECT", "identical bindings");
		Model model = data();
		List<String> vars = Arrays.asList("s", "p", "o");
		String q = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));

		printBindings("OLD", old);
		printBindings("NEW", shiny);
		verdict(canon(old).equals(canon(shiny)), "OLD == NEW bindings");
	}

	private void caseSelectBind() throws Exception {
		header("BIND(STR(?o) AS ?x)", "?x bound (the round-1 render path dropped it)");
		Model model = data();
		List<String> vars = Arrays.asList("s", "x");
		String q = "SELECT ?s ?x WHERE { ?s <http://ajan.test/q> ?o . BIND(STR(?o) AS ?x) }";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));
		Object round1 = tryRound1Select(q, vars, model);

		printBindings("OLD   ", old);
		printBindings("NEW   ", shiny);
		System.out.println("ROUND1: " + round1);
		boolean xBoundInNew = !shiny.isEmpty() && shiny.get(0).hasBinding("x");
		verdict(canon(old).equals(canon(shiny)) && xBoundInNew, "OLD == NEW, and ?x is bound in NEW");
	}

	private void caseSelectCountGroupBy() throws Exception {
		header("(COUNT(?o) AS ?n) ... GROUP BY ?s", "?n bound");
		Model model = data();
		List<String> vars = Arrays.asList("s", "n");
		String q = "SELECT ?s (COUNT(?o) AS ?n) WHERE { ?s ?p ?o } GROUP BY ?s";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));

		printBindings("OLD", old);
		printBindings("NEW", shiny);
		boolean nBoundInNew = shiny.size() == 3 && shiny.stream().allMatch(bs -> bs.hasBinding("n"));
		verdict(canon(old).equals(canon(shiny)) && nBoundInNew, "OLD == NEW, and ?n is bound in every NEW row");
	}

	private void caseSelectFilterNotExists() throws Exception {
		header("FILTER NOT EXISTS", "returns rows, no MalformedQueryException");
		Model model = data();
		List<String> vars = Arrays.asList("s");
		String q = "SELECT ?s WHERE { ?s ?p ?o . FILTER NOT EXISTS { ?s <http://ajan.test/q> ?any } }";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));

		printBindings("OLD", old);
		printBindings("NEW", shiny);
		verdict(canon(old).equals(canon(shiny)) && shiny.size() == 2, "OLD == NEW, 2 rows (t:a, t:c)");
	}

	private void caseSelectFilterExists() throws Exception {
		header("FILTER EXISTS", "returns rows, no MalformedQueryException");
		Model model = data();
		List<String> vars = Arrays.asList("s");
		String q = "SELECT ?s WHERE { ?s ?p ?o . FILTER EXISTS { ?s <http://ajan.test/q> ?any } }";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));

		printBindings("OLD", old);
		printBindings("NEW", shiny);
		verdict(canon(old).equals(canon(shiny)) && shiny.size() == 1, "OLD == NEW, 1 row (t:b)");
	}

	private void caseSelectFromAsk() throws Exception {
		header("ASK WHERE { ... } as the source query", "binding sets returned (the real ACTN input)");
		Model model = data();
		List<String> vars = Arrays.asList("s", "p", "o");
		String q = "ASK WHERE { ?s ?p ?o }";

		List<BindingSet> old = Old.queryModel(model, Old.getSelectQuery(q, vars));
		printBindings("OLD (SailQueryPreparer, never rendered)   ", old);

		String round1Outcome;
		try {
			List<BindingSet> r1 = Round1.queryModel(model, Round1.getSelectQuery(q));
			round1Outcome = "returned " + r1.size() + " rows (unexpected: should have failed)";
		} catch (Exception ex) {
			round1Outcome = "THREW " + ex.getClass().getName() + ": " + ex.getMessage();
		}
		System.out.println("ROUND1 (render path, this round's regression) = " + round1Outcome);

		List<BindingSet> shiny = SPARQLUtil.queryModel(model, SPARQLUtil.getSelectQuery(q, vars));
		printBindings("NEW (direct Sail evaluation)               ", shiny);

		boolean round1Failed = round1Outcome.startsWith("THREW");
		verdict(!old.isEmpty() && !shiny.isEmpty() && old.size() == shiny.size() && round1Failed,
				"OLD and NEW both return exactly 1 binding set (ASK's implicit LIMIT 1); ROUND1 throws");
	}

	private void caseDescribeOneIri() throws Exception {
		header("describe, one IRI", "identical to the pre-refactor QueryBuilderFactory output");
		Model model = data();
		ParsedGraphQuery oldQ = Old.getDescribeQuery(Arrays.<Resource>asList(A).iterator());
		ParsedGraphQuery newQ = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(A).iterator());
		String oldRendered = render(oldQ);
		String newRendered = render(newQ);
		System.out.println("OLD rendered: " + oldRendered);
		System.out.println("NEW rendered: " + newRendered);

		Model oldResult = Old.queryModel(model, oldQ);
		Model newResult = SPARQLUtil.queryModel(model, newQ);
		printModel("OLD evaluated", oldResult);
		printModel("NEW evaluated", newResult);
		verdict(oldRendered.equals(newRendered) && canon(oldResult).equals(canon(newResult)) && newResult.size() == 2,
				"rendered text byte-identical AND evaluated results identical (2 statements)");
	}

	private void caseDescribeTwoIris() throws Exception {
		header("describe, two IRIs", "identical to the pre-refactor QueryBuilderFactory output");
		Model model = data();
		ParsedGraphQuery oldQ = Old.getDescribeQuery(Arrays.<Resource>asList(A, B).iterator());
		ParsedGraphQuery newQ = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(A, B).iterator());
		String oldRendered = render(oldQ);
		String newRendered = render(newQ);
		System.out.println("OLD rendered: " + oldRendered);
		System.out.println("NEW rendered: " + newRendered);

		Model oldResult = Old.queryModel(model, oldQ);
		Model newResult = SPARQLUtil.queryModel(model, newQ);
		printModel("OLD evaluated", oldResult);
		printModel("NEW evaluated", newResult);
		verdict(oldRendered.equals(newRendered) && canon(oldResult).equals(canon(newResult)) && newResult.size() == 3,
				"rendered text byte-identical AND evaluated results identical (3 statements)");
	}

	private void caseDescribeBlankNode() throws Exception {
		header("describe, blank node", "matches the blank node (algebra evaluated directly)");
		String bnodeData = "@prefix t: <http://ajan.test/> .\n"
				+ "t:d t:s _:bn1 .\n"
				+ "_:bn1 t:t t:e .\n";
		Model model = parseTurtle(bnodeData);
		IRI d = VF.createIRI("http://ajan.test/d");
		IRI s = VF.createIRI("http://ajan.test/s");
		Statement dToBnode = model.filter(d, s, null).iterator().next();
		Resource bnode = (Resource) dToBnode.getObject();

		Model oldResult = Old.queryModel(model, Old.getDescribeQuery(Arrays.<Resource>asList(bnode).iterator()));
		printModel("OLD (SailQueryPreparer, never rendered)", oldResult);

		String round1Outcome;
		try {
			ParsedGraphQuery r1Query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(bnode).iterator());
			Model r1Result = Round1.queryModel(model, r1Query);
			round1Outcome = "returned " + r1Result.size() + " statements (unexpected: should have failed)";
		} catch (Exception ex) {
			round1Outcome = "THREW " + ex.getClass().getSimpleName() + ": " + firstLine(ex.getMessage());
		}
		System.out.println("ROUND1 (render path, this round's regression) = " + round1Outcome);

		Model newResult = SPARQLUtil.queryModel(model, SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(bnode).iterator()));
		printModel("NEW (direct Sail evaluation)           ", newResult);

		boolean round1Failed = round1Outcome.startsWith("THREW");
		verdict(canon(oldResult).equals(canon(newResult)) && newResult.size() == 2 && round1Failed,
				"OLD == NEW (2 statements, blank node matched); ROUND1 throws MalformedQueryException");
	}

	private void caseDescribeHostileIri() throws Exception {
		header("describe with a hostile IRI (contains '>' plus SPARQL syntax)", "result set unchanged - proves no text path remains");
		IRI evil = VF.createIRI("http://ajan.test/x> } ; DROP ALL ; # ");
		Model model = new LinkedHashModel();
		model.add(evil, P, B);
		model.add(C, R, evil);

		Model oldResult = Old.queryModel(model, Old.getDescribeQuery(Arrays.<Resource>asList(evil).iterator()));
		printModel("OLD (SailQueryPreparer, never rendered)", oldResult);

		String round1Outcome;
		try {
			ParsedGraphQuery r1Query = SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(evil).iterator());
			Model r1Result = Round1.queryModel(model, r1Query);
			round1Outcome = "returned " + r1Result.size() + " statements (query text corrupted, result meaningless if non-empty)";
		} catch (Exception ex) {
			round1Outcome = "THREW " + ex.getClass().getSimpleName() + ": " + firstLine(ex.getMessage());
		}
		System.out.println("ROUND1 (render path, this round's regression) = " + round1Outcome);

		Model newResult = SPARQLUtil.queryModel(model, SPARQLUtil.getDescribeQuery(Arrays.<Resource>asList(evil).iterator()));
		printModel("NEW (direct Sail evaluation)           ", newResult);

		boolean round1Broken = round1Outcome.startsWith("THREW") || !round1Outcome.contains("2 statements");
		verdict(canon(oldResult).equals(canon(newResult)) && newResult.size() == 2 && round1Broken,
				"OLD == NEW (2 statements, hostile value never became text); ROUND1 is corrupted or throws");
	}

	// ---------------------------------------------------------------
	// OLD: pre-Task-3 SPARQLUtil, reconstructed verbatim (git 5e90754c)
	// ---------------------------------------------------------------

	private static final class Old {

		static ParsedTupleQuery getSelectQuery(final String query, final List<String> varNames) {
			TupleExpr tupleExpr = getTupleExpr(query);
			return getSelectQuery(tupleExpr, varNames);
		}

		static ParsedTupleQuery getSelectQuery(final TupleExpr tupleExpr, final List<String> varNames) {
			QueryBuilder<ParsedTupleQuery> builder = QueryBuilderFactory.select();
			builder.addProjectionVar(varNames.toArray(new String[0]));
			builder.group();
			ParsedTupleQuery parsedQuery = builder.query();
			parsedQuery.setTupleExpr(tupleExpr);
			return parsedQuery;
		}

		static TupleExpr getTupleExpr(final String query) {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery parsedQuery = parser.parseQuery(query, "http://localhost:8090/ajan");
			return parsedQuery.getTupleExpr();
		}

		static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
			QueryBuilder<ParsedGraphQuery> builder = QueryBuilderFactory.construct();
			String subj = "descr_subj";
			String pred = "descr_pred";
			String obj = "descr_obj";
			builder.addProjectionStatement(subj, pred, obj);
			builder.group().atom(subj, pred, obj);
			builder.group().filter(describeFilter(resourceIterator, subj, obj));
			return builder.query();
		}

		static ValueExpr describeFilter(final Iterator<Resource> resourceIterator, final String subj, final String obj) {
			Resource resource = resourceIterator.next();
			if (resourceIterator.hasNext()) {
				return new Or(sameTerm(resource, subj), new Or(sameTerm(resource, obj), describeFilter(resourceIterator, subj, obj)));
			} else {
				return new Or(sameTerm(resource, subj), sameTerm(resource, obj));
			}
		}

		static ValueExpr sameTerm(final Resource resource, final String var) {
			return new SameTerm(new ValueConstant(resource), new Var(var));
		}

		static SailRepository createRepository(final Model model) {
			SailRepository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			Repositories.consume(repo, conn -> conn.add(model));
			return repo;
		}

		static Model queryModel(final Model model, final ParsedGraphQuery query) {
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

		static List<BindingSet> queryModel(final Model model, final ParsedTupleQuery query) {
			SailRepository repo = createRepository(model);
			List<BindingSet> resultModel = new ArrayList<>();
			try (SailRepositoryConnection conn = repo.getConnection()) {
				conn.begin();
				SailQueryPreparer preparer = new SailQueryPreparer(conn, false);
				TupleQueryResult results = preparer.prepare(query).evaluate();
				while (results.hasNext()) {
					resultModel.add(results.next());
				}
				conn.commit();
			}
			repo.shutDown();
			return resultModel;
		}
	}

	// ---------------------------------------------------------------
	// ROUND1: fix-round-1 SPARQLUtil, reconstructed verbatim (git 79096bb7)
	// getDescribeQuery construction did not change between round 1 and round
	// 2, so the real SPARQLUtil.getDescribeQuery is reused for those cases;
	// only the evaluation methods (the actual regression) are reconstructed.
	// ---------------------------------------------------------------

	private static final class Round1 {

		static ParsedTupleQuery getSelectQuery(final String query) {
			TupleExpr tupleExpr = Old.getTupleExpr(query);
			return new ParsedTupleQuery(query, tupleExpr);
		}

		@SuppressWarnings("PMD.AvoidCatchingGenericException")
		static String renderQuery(final ParsedQuery query) throws Exception {
			return new SPARQLQueryRenderer().render(query);
		}

		static Model queryModel(final Model model, final ParsedGraphQuery query) throws Exception {
			SailRepository repo = SPARQLUtil.createRepository(model);
			Model resultModel;
			try (SailRepositoryConnection conn = repo.getConnection()) {
				conn.begin();
				GraphQuery graphQuery = conn.prepareGraphQuery(renderQuery(query));
				GraphQueryResult results = graphQuery.evaluate();
				resultModel = QueryResults.asModel(results);
				conn.commit();
			}
			repo.shutDown();
			return resultModel;
		}

		static List<BindingSet> queryModel(final Model model, final ParsedTupleQuery query) throws Exception {
			SailRepository repo = SPARQLUtil.createRepository(model);
			List<BindingSet> resultModel = new ArrayList<>();
			try (SailRepositoryConnection conn = repo.getConnection()) {
				conn.begin();
				String queryString = query.getSourceString() == null ? renderQuery(query) : query.getSourceString();
				TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
				TupleQueryResult results = tupleQuery.evaluate();
				while (results.hasNext()) {
					resultModel.add(results.next());
				}
				conn.commit();
			}
			repo.shutDown();
			return resultModel;
		}
	}

	// ---------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------

	private static Model data() throws Exception {
		return parseTurtle(TURTLE_DATA);
	}

	private static Model parseTurtle(final String turtle) throws Exception {
		return Rio.parse(new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)), "", RDFFormat.TURTLE);
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private static String render(final ParsedQuery query) throws Exception {
		return new SPARQLQueryRenderer().render(query);
	}

	private static Object tryRound1Select(final String q, final List<String> vars, final Model model) {
		try {
			return Round1.queryModel(model, Round1.getSelectQuery(q)).size() + " rows (unexpected: BIND should have been dropped, or this now matches NEW)";
		} catch (Exception ex) {
			return "THREW " + ex.getClass().getSimpleName() + ": " + firstLine(ex.getMessage());
		}
	}

	private static String firstLine(final String message) {
		if (message == null) {
			return "";
		}
		int idx = message.indexOf('\n');
		return idx < 0 ? message : message.substring(0, idx);
	}

	private static TreeSet<String> canon(final List<BindingSet> bindings) {
		TreeSet<String> set = new TreeSet<>();
		for (BindingSet bs : bindings) {
			List<String> names = new ArrayList<>(bs.getBindingNames());
			names.sort(String::compareTo);
			StringBuilder row = new StringBuilder();
			for (String name : names) {
				Value v = bs.getValue(name);
				row.append(name).append('=').append(v == null ? "?" : v.stringValue()).append(';');
			}
			set.add(row.toString());
		}
		return set;
	}

	private static TreeSet<String> canon(final Model model) {
		TreeSet<String> set = new TreeSet<>();
		for (Statement s : model) {
			set.add(s.getSubject().stringValue() + " | " + s.getPredicate().stringValue() + " | " + s.getObject().stringValue());
		}
		return set;
	}

	private static void printBindings(final String label, final List<BindingSet> bindings) {
		System.out.println(label + " (" + bindings.size() + " rows): " + canon(bindings));
	}

	private static void printModel(final String label, final Model model) {
		System.out.println(label + " (" + model.size() + " statements): " + canon(model));
	}

	private static void header(final String caseName, final String expectation) {
		System.out.println();
		System.out.println("--- case: " + caseName + " ---");
		System.out.println("expectation: " + expectation);
	}

	private void verdict(final boolean pass, final String assertion) {
		if (pass) {
			passCount++;
			System.out.println("PASS: " + assertion);
		} else {
			failCount++;
			System.out.println("FAIL: " + assertion);
		}
	}
}
