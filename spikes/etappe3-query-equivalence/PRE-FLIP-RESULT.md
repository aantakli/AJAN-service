# Pre-flip result: query-equivalence harness against RDF4J 3.6.3 `common-0.1.jar`

**Why this file exists and cannot be reproduced:** this harness's "NEW" column binds
against the installed `de.dfki.asr.ajan:common:0.1` jar on the local Maven repository.
It only proves anything about the RDF4J-3.6.3-vs-direct-Sail-evaluation question while
`common` is actually compiled against RDF4J 3.6.3. Task 5 flips the reactor-wide
`org.eclipse.rdf4j.version` property to 5.3.1, and from that point on `common` is
permanently built against 5.3.1 — the "OLD" (`SailQueryPreparer`, pre-refactor RDF4J-3.6.3
API) column this harness exercises cannot be produced again from this repository's history
without checking out a commit before the flip and reinstalling. This run was captured
immediately before that flip, specifically to preserve the evidence that the Task 3
query-model rewrite (direct `SailConnection` algebra evaluation replacing
`SailQueryPreparer`/text rendering) did not change query-evaluation behaviour.

**What it proves:** for all 10 harness cases (plain BGP SELECT, `BIND`, `COUNT`/`GROUP BY`,
`FILTER NOT EXISTS`, `FILTER EXISTS`, `ASK` as source query, `DESCRIBE` with one IRI, two
IRIs, a blank node, and a hostile IRI containing SPARQL syntax), the OLD path
(`SailQueryPreparer`, RDF4J 3.6.3) and the NEW path (direct `SailConnection` evaluation,
Task 3's rewrite) produce identical bindings/statements. The bundled `ROUND1` column
(an earlier, abandoned render-based approach) is intentionally broken on 3 of the 10 cases
by design — it demonstrates why the render approach was rejected in favour of direct Sail
evaluation, not a regression.

## Repository state at capture time

- Branch: `modernization/java21`
- Commit (pre-flip HEAD, the last commit before the RDF4J-5.3.1 property flip):
  `ad6ea035` — "docs: defer the rdfbeans exception-type refinement to etappe 4"
- The commit that performs the flip on top of this one: `build: bump rdf4j to 5.3.1
  reactor-wide and drop removed spin sail`

## Toolchain

- JDK: Eclipse Temurin 11.0.31 (`C:\Users\yanni\.jdks\temurin-11`)
- Maven: Apache Maven 3.9.16

## Commands run, in order

```
mvn install
mvn -f spikes/etappe3-query-equivalence/pom.xml -q compile exec:java
```

The first command is a full reactor `install` (JDK 11, foreground) so that
`common-0.1.jar` in the local repository is freshly built from the pre-flip source tree
before the harness binds against it. The second runs the harness itself.

## Verbatim output

```
=== Fix round 2 differential harness: OLD (SailQueryPreparer) vs ROUND1 (render) vs NEW (direct Sail) ===


--- case: plain BGP SELECT ---
expectation: identical bindings
OLD (3 rows): [o=http://ajan.test/a;p=http://ajan.test/r;s=http://ajan.test/c;, o=http://ajan.test/b;p=http://ajan.test/p;s=http://ajan.test/a;, o=literal;p=http://ajan.test/q;s=http://ajan.test/b;]
NEW (3 rows): [o=http://ajan.test/a;p=http://ajan.test/r;s=http://ajan.test/c;, o=http://ajan.test/b;p=http://ajan.test/p;s=http://ajan.test/a;, o=literal;p=http://ajan.test/q;s=http://ajan.test/b;]
PASS: OLD == NEW bindings

--- case: BIND(STR(?o) AS ?x) ---
expectation: ?x bound (the round-1 render path dropped it)
OLD    (1 rows): [s=http://ajan.test/b;x=literal;]
NEW    (1 rows): [s=http://ajan.test/b;x=literal;]
ROUND1: 1 rows (unexpected: BIND should have been dropped, or this now matches NEW)
PASS: OLD == NEW, and ?x is bound in NEW

--- case: (COUNT(?o) AS ?n) ... GROUP BY ?s ---
expectation: ?n bound
OLD (3 rows): [n=1;s=http://ajan.test/a;, n=1;s=http://ajan.test/b;, n=1;s=http://ajan.test/c;]
NEW (3 rows): [n=1;s=http://ajan.test/a;, n=1;s=http://ajan.test/b;, n=1;s=http://ajan.test/c;]
PASS: OLD == NEW, and ?n is bound in every NEW row

--- case: FILTER NOT EXISTS ---
expectation: returns rows, no MalformedQueryException
OLD (2 rows): [s=http://ajan.test/a;, s=http://ajan.test/c;]
NEW (2 rows): [s=http://ajan.test/a;, s=http://ajan.test/c;]
PASS: OLD == NEW, 2 rows (t:a, t:c)

--- case: FILTER EXISTS ---
expectation: returns rows, no MalformedQueryException
OLD (1 rows): [s=http://ajan.test/b;]
NEW (1 rows): [s=http://ajan.test/b;]
PASS: OLD == NEW, 1 row (t:b)

--- case: ASK WHERE { ... } as the source query ---
expectation: binding sets returned (the real ACTN input)
OLD (SailQueryPreparer, never rendered)    (1 rows): [o=http://ajan.test/b;p=http://ajan.test/p;s=http://ajan.test/a;]
ROUND1 (render path, this round's regression) = THREW java.lang.IllegalArgumentException: query is not a tuple query: ASK WHERE { ?s ?p ?o }
NEW (direct Sail evaluation)                (1 rows): [o=http://ajan.test/b;p=http://ajan.test/p;s=http://ajan.test/a;]
PASS: OLD and NEW both return exactly 1 binding set (ASK's implicit LIMIT 1); ROUND1 throws

--- case: describe, one IRI ---
expectation: identical to the pre-refactor QueryBuilderFactory output
OLD rendered: construct  {
  ?descr_subj ?descr_pred ?descr_obj.
}
where {
  ?descr_subj ?descr_pred ?descr_obj.
  filter  ( sameTerm(<http://ajan.test/a>, ?descr_subj) ||  sameTerm(<http://ajan.test/a>, ?descr_obj)).
}
NEW rendered: construct  {
  ?descr_subj ?descr_pred ?descr_obj.
}
where {
  ?descr_subj ?descr_pred ?descr_obj.
  filter  ( sameTerm(<http://ajan.test/a>, ?descr_subj) ||  sameTerm(<http://ajan.test/a>, ?descr_obj)).
}
OLD evaluated (2 statements): [http://ajan.test/a | http://ajan.test/p | http://ajan.test/b, http://ajan.test/c | http://ajan.test/r | http://ajan.test/a]
NEW evaluated (2 statements): [http://ajan.test/a | http://ajan.test/p | http://ajan.test/b, http://ajan.test/c | http://ajan.test/r | http://ajan.test/a]
PASS: rendered text byte-identical AND evaluated results identical (2 statements)

--- case: describe, two IRIs ---
expectation: identical to the pre-refactor QueryBuilderFactory output
OLD rendered: construct  {
  ?descr_subj ?descr_pred ?descr_obj.
}
where {
  ?descr_subj ?descr_pred ?descr_obj.
  filter  ( sameTerm(<http://ajan.test/a>, ?descr_subj) ||  ( sameTerm(<http://ajan.test/a>, ?descr_obj) ||  ( sameTerm(<http://ajan.test/b>, ?descr_subj) ||  sameTerm(<http://ajan.test/b>, ?descr_obj)))).
}
NEW rendered: construct  {
  ?descr_subj ?descr_pred ?descr_obj.
}
where {
  ?descr_subj ?descr_pred ?descr_obj.
  filter  ( sameTerm(<http://ajan.test/a>, ?descr_subj) ||  ( sameTerm(<http://ajan.test/a>, ?descr_obj) ||  ( sameTerm(<http://ajan.test/b>, ?descr_subj) ||  sameTerm(<http://ajan.test/b>, ?descr_obj)))).
}
OLD evaluated (3 statements): [http://ajan.test/a | http://ajan.test/p | http://ajan.test/b, http://ajan.test/b | http://ajan.test/q | literal, http://ajan.test/c | http://ajan.test/r | http://ajan.test/a]
NEW evaluated (3 statements): [http://ajan.test/a | http://ajan.test/p | http://ajan.test/b, http://ajan.test/b | http://ajan.test/q | literal, http://ajan.test/c | http://ajan.test/r | http://ajan.test/a]
PASS: rendered text byte-identical AND evaluated results identical (3 statements)

--- case: describe, blank node ---
expectation: matches the blank node (algebra evaluated directly)
OLD (SailQueryPreparer, never rendered) (2 statements): [genid-b4d2948cbc25487998b3fc076ea46b3d-bn1 | http://ajan.test/t | http://ajan.test/e, http://ajan.test/d | http://ajan.test/s | genid-b4d2948cbc25487998b3fc076ea46b3d-bn1]
ROUND1 (render path, this round's regression) = THREW MalformedQueryException: Encountered " <BLANK_NODE_LABEL> "_:genid-b4d2948cbc25487998b3fc076ea46b3d-bn1 "" at line 6, column 22.

NEW (direct Sail evaluation)            (2 statements): [genid-b4d2948cbc25487998b3fc076ea46b3d-bn1 | http://ajan.test/t | http://ajan.test/e, http://ajan.test/d | http://ajan.test/s | genid-b4d2948cbc25487998b3fc076ea46b3d-bn1]
PASS: OLD == NEW (2 statements, blank node matched); ROUND1 throws MalformedQueryException

--- case: describe with a hostile IRI (contains '>' plus SPARQL syntax) ---
expectation: result set unchanged - proves no text path remains
OLD (SailQueryPreparer, never rendered) (2 statements): [http://ajan.test/c | http://ajan.test/r | http://ajan.test/x> } ; DROP ALL ; # , http://ajan.test/x> } ; DROP ALL ; #  | http://ajan.test/p | http://ajan.test/b]
ROUND1 (render path, this round's regression) = THREW MalformedQueryException: Encountered " "}" "} "" at line 6, column 43.

NEW (direct Sail evaluation)            (2 statements): [http://ajan.test/c | http://ajan.test/r | http://ajan.test/x> } ; DROP ALL ; # , http://ajan.test/x> } ; DROP ALL ; #  | http://ajan.test/p | http://ajan.test/b]
PASS: OLD == NEW (2 statements, hostile value never became text); ROUND1 is corrupted or throws

=== SUMMARY: 10 passed, 0 failed ===
```
