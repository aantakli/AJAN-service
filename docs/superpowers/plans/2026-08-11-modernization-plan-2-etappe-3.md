# Modernisierung Plan 2: Triplestore-Neubau + RDF4J-5-Client-Umstieg (Etappe 3)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Den retired `tomcat8-maven-plugin`-Triplestore durch ein eigenes Embedded-Tomcat-Launcher-Modul mit RDF4J-5-Server-/Workbench-WARs ersetzen (Ports/Pfade/CLI identisch) und im selben Zug den RDF4J-Client im gesamten Produktcode von 3.6.3 auf 5.3.1 heben — inklusive rdfbeans und aller 10 Plugins, noch unter Spring Boot 1.3.5 und Java 11.

**Architecture:** Der Triplestore wird ein gewöhnliches Maven-Modul mit einer `main`-Klasse, die einen Embedded Tomcat 9 startet und die beiden offiziellen RDF4J-WARs unter `/rdf4j` und `/workbench` deployed; das Fat-JAR entsteht per `maven-shade-plugin` statt per `exec-war-only`. Der Client-Umstieg wird zweistufig gefahren: erst werden die von RDF4J 5 entfernten APIs im Produktcode durch Konstrukte ersetzt, die **unter 3.6.3 und 5.3.1 gleichermaßen kompilieren** und durch neue Charakterisierungstests abgesichert sind, danach wird die zentrale Versions-Property umgelegt. Dazwischen liegt ein bewusst offenes, exakt charakterisiertes Rot-Fenster in der E2E-Suite (s. Rot-Fenster-Regel).

**Tech Stack:** Java 11, Maven, Spring Boot 1.3.5 (unverändert), RDF4J 5.3.1, Embedded Tomcat 9.0.109 (javax.servlet), TestNG 7.10.2 + Hamcrest 2.2 (Unit-Tests), JUnit 5 + failsafe (nur `e2e/`), Docker (nur Task 1).

**Spec:** `docs/superpowers/specs/2026-07-11-java-modernization-design.md`
**Spike-Grundlage:** `docs/superpowers/specs/2026-07-11-spike-results.md`
**Ledger:** `.superpowers/sdd/progress.md`

---

## Vorbefunde (beim Planschreiben am 2026-08-11 verifiziert)

Diese Befunde sind **nicht** aus Dokumenten übernommen, sondern gegen Maven Central und das Repo geprüft. Jeder Task, der darauf aufbaut, nennt das Kommando zum Nachvollziehen.

| # | Befund | Beleg |
|---|---|---|
| V1 | `org.eclipse.rdf4j:rdf4j-http-server:5.3.1:war` und `org.eclipse.rdf4j:rdf4j-http-workbench:5.3.1:war` existieren auf Maven Central und lösen auf (58 MB / 54 MB). | `mvn dependency:get -Dartifact=org.eclipse.rdf4j:rdf4j-http-server:5.3.1:war` → Exit 0 |
| V2 | RDF4J 5.3.1 ist **Java-11-Bytecode** (class major version 55). Etappe 3 kann auf Java 11 bleiben. | `javap -verbose org.eclipse.rdf4j.model.impl.SimpleValueFactory` aus `rdf4j-model-5.3.1.jar` |
| V3 | Die RDF4J-5.3.1-WARs sind **javax.servlet**, nicht Jakarta: `WEB-INF/web.xml` deklariert `web-app 2.4`, `WEB-INF/lib/` enthält `spring-webmvc-5.3.39.jar` und `jstl-1.2.jar`, **kein** `jakarta.servlet`. ⇒ Embedded **Tomcat 9.0.x**; Tomcat 10/11 (Jakarta) ist ausgeschlossen. Damit ist offene Frage 5 der Spike-Ergebnisse beantwortet und die Spec-Formulierung „Embedded-Tomcat-Launcher-Modul (Jakarta-Linie)" **falsch** (Korrektur in Task 7). | `unzip -p rdf4j-http-server-5.3.1.war WEB-INF/web.xml`, `unzip -l ... \| grep -i servlet` |
| V4 | Das Server-WAR enthält **23 JSP-Dateien**, das Workbench-WAR keine ⇒ der Launcher braucht `tomcat-embed-jasper`. | `unzip -l rdf4j-http-server-5.3.1.war \| grep -c "\.jsp"` → 23 |
| V5 | `spring-boot-dependencies:1.3.5.RELEASE` verwaltet `jackson.version=2.6.6`, `httpclient.version=4.5.2`, `slf4j.version=1.7.21`. Die RDF4J-5.3.1-Distribution führt jackson **2.21.0**, httpclient 4.5.14, slf4j 1.7.36. `executionservice` erbt von `spring-boot-starter-parent` ⇒ die Boot-Pins gewinnen über die transitiven RDF4J-Versionen. **Das ist die Risikofrage aus offener Frage 4** und Gegenstand von Task 1. | `spring-boot-dependencies-1.3.5.RELEASE.pom`; `unzip -l rdf4j-http-server-5.3.1.war \| grep jackson` |
| V6 | `Repository.initialize()` existiert in 5.3.1 **nicht mehr**; es gibt nur `init()`. | `javap org.eclipse.rdf4j.repository.Repository` (5.3.1) |
| V7 | `org.eclipse.rdf4j.repository.sail.SailQueryPreparer` ist in `rdf4j-repository-sail:5.3.1` **entfernt** (in 3.6.3 vorhanden). | Klassenlisten-Diff beider JARs |
| V8 | Das komplette Paket `org.eclipse.rdf4j.queryrender.builder` (`QueryBuilder`, `QueryBuilderFactory`, `GroupBuilder`, `ValueExprFactory`, …) ist in `rdf4j-queryrender:5.3.1` **entfernt**. `org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer` bleibt. | Klassenlisten-Diff beider JARs |
| V9 | `CloseableIteration` hat in 5.3.1 **einen** Typparameter und erweitert `java.util.Iterator`: `CloseableIteration<E> extends Iterator<E>, AutoCloseable`. In 3.6.3 waren es zwei (`<E, X extends Exception>`). Betrifft ausschließlich `rdfbeans` (7 Stellen im Hauptcode, 4 in Tests). | `javap org.eclipse.rdf4j.common.iteration.CloseableIteration` (5.3.1) |
| V10 | `org.eclipse.rdf4j:rdf4j-sail-spin` existiert nur bis 3.7.x; für 4.0.0, 4.3.15, 5.0.0 und 5.3.1 schlägt die Auflösung fehl. `rdf4j-spin` (Parser/Funktionen) bleibt, enthält aber **kein** `SpinSail`. | `mvn dependency:get -Dartifact=org.eclipse.rdf4j:rdf4j-sail-spin:5.3.1:jar` → Exit 1; `mvn ... :3.7.7:jar` → Exit 0 |
| V11 | SPIN ist zur Laufzeit **toter Code**: `RDFAgentBuilder.java:96` und `ParameterAgentBuilder.java:81` setzen `inferencing = Inferencing.NONE` hart; die `SPIN`/`RDFS_SPIN`-Zweige in `ExecutionBeliefBase` und `RDF4JTripleStoreManager` sind unerreichbar. | `grep -rn "Inferencing\."` über den Produktcode |
| V12 | Die RDF4J-Version wird **zentral** über `pom.xml:161` (`org.eclipse.rdf4j.version=3.6.3`) gesteuert; genau drei POMs importieren `rdf4j-bom`: `common`, `behaviour`, `rdfbeans`. `rdfbeans` hat **keinen Parent** und eine **eigene** Property `rdf4j.version=2.0M1` (rdfbeans kompiliert heute gegen die 2.0M1-API und läuft gegen 3.6.3). | `grep -rln --include=pom.xml rdf4j-bom .`; `rdfbeans/pom.xml:75` |
| V13 | `QueryBuilderFactory.describe(r)` und `SPARQLUtil.getDescribeQuery([r])` erzeugen **dieselbe** gerenderte Query. Damit ist `AgentModelManager.getTemplateFromTDB` durch einen Aufruf der bereits vorhandenen `SPARQLUtil.getDescribeQuery` ersetzbar. Gerenderte Form (eine Ressource):<br>`construct { ?descr_subj ?descr_pred ?descr_obj. } where { ?descr_subj ?descr_pred ?descr_obj. filter ( sameTerm(<IRI>, ?descr_subj) \|\| sameTerm(<IRI>, ?descr_obj)). }` | Probe-Programm gegen den 3.6.3-Klassenpfad von `common` (Ausgabe im Task-1-Report zu wiederholen) |
| V14 | In `SPARQLUtil.getSelectQuery(TupleExpr, List<String>)` ist die Builder-Ausgabe **wirkungslos**: `builder.query()` wird sofort durch `parsedQuery.setTupleExpr(tupleExpr)` überschrieben. Ersatz ist der öffentliche Konstruktor `new ParsedTupleQuery(TupleExpr)`, der in **beiden** Versionen existiert. | `javap org.eclipse.rdf4j.query.parser.ParsedTupleQuery` (3.6.3 **und** 5.3.1) |
| V15 | Alle `ParsedQuery`-Objekte, die in den betroffenen `SPARQLUtil`-Methoden landen, stammen entweder aus `SPARQLParser` oder aus dem Query-Builder — beide Sorten werden **heute schon** in Produktion über `SPARQLQueryRenderer` zu Strings gerendert (`queryRepository(Repository, ParsedQuery)`, `BehaviorAskQuery.java:66`). Render-und-Prepare ist deshalb kein neues Risiko. | Caller-Inventar (Task 3, Step 1) |
| V16 | `triplestore/lib/` ist ein eingechecktes File-Repository mit `rdf4j-server-4.0.0.war` (33 MB) unter der Fantasie-groupId `rdf4j-server`; das Workbench-WAR kommt dagegen aus Central mit `${org.eclipse.rdf4j.version}` (heute 3.6.3). Das ist der in der Spec genannte „Versions-Mix". | `triplestore/pom.xml:145-164`, `ls triplestore/lib/...` |

**Bestätigte Auftraggeber-Entscheidungen (2026-08-11), nicht neu aufzurollen:**

1. **SPIN:** Enum `Inferencing` bleibt vollständig; die Zweige `SPIN`/`RDFS_SPIN` werfen künftig `UnsupportedOperationException` mit Begründungskommentar; `rdf4j-sail-spin` fliegt aus `common/pom.xml`. Laufzeitverhalten bleibt identisch (V11).
2. **Artefaktname:** Das Launcher-JAR heißt `triplestore-0.1.jar`. Start-Skripte, `Dockerfile`, `README.md` und der e2e-Glob ziehen mit. `.github/workflows/docker-image.yml` bleibt gemäß Epic-Regel **unangetastet** und wird in Etappe 5 nachgezogen — als bewusste, hier dokumentierte Lücke (der Workflow triggert ausschließlich auf `master`).
3. **E2E-RDF4J-Pin:** `e2e/pom.xml` bleibt auf RDF4J **3.6.3**; nur der irreführende Kommentar wird korrigiert. Begründung: Die Suite testet von außen und soll alle Etappen unverändert überleben; ein vom Produktcode entkoppelter Parser ist dafür das robustere Werkzeug.

---

## Global Constraints

- Alle Arbeit auf Branch `modernization/java21`; `master` wird nicht angefasst, nichts wird nach `master` gemerged oder dorthin gepusht.
- `.github/workflows/docker-image.yml` wird in diesem Plan **NICHT** verändert (Entscheidung 2 oben; Nachzug in Etappe 5).
- Kein `docker push`, keine Tags `aantakli/ajan-service*`; lokale Docker-Nutzung nur mit dem offiziellen `eclipse/rdf4j-workbench`-Image (Task 1) und lokalen Tags.
- Die öffentliche HTTP-API des executionservice bleibt unverändert; die charakterisierten IST-Werte aus Etappe 0 (Ledger, Task 4b) müssen erhalten bleiben: `GET /ajan/agents/{id}` mit `Accept: text/turtle` → 200 + `text/turtle; charset=UTF-8`, mit `Accept: application/ld+json` → 200 + `application/ld+json; charset=UTF-8`; unbekannter Agent → 404 mit Turtle-Error-Body; malformed Turtle → 400 mit `text/html; charset=UTF-8`.
- Ports sind fix: Triplestore **8090**, executionservice **8080**. Kontextpfade normativ: **`/rdf4j`** (Server) und **`/workbench`** (Workbench).
- Java-Target bleibt **11** (`maven.compiler.release=11`); Spring Boot bleibt **1.3.5.RELEASE**; pf4j bleibt **3.6.0**; kein javax→jakarta. All das ist Etappe 4.
- RDF4J-Zielversion ist **5.3.1** (V1, V2). Keine andere 5.x-Version ohne erneute Prüfung von V2 (Java-11-Bytecode).
- Embedded-Tomcat-Zielversion ist **9.0.109** (javax.servlet, V3). Tomcat 10/11 sind ausgeschlossen.
- Build-Umgebung (nichts im PATH):
  - JDK 11: `C:\Users\yanni\.jdks\temurin-11` (Default für alle Kommandos dieses Plans)
  - JDK 21: `C:\Users\yanni\.jdks\ms-21.0.11` (in diesem Plan **nicht** benötigt)
  - Maven: `C:\Users\yanni\AppData\Local\Programs\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd`
  - PowerShell-Muster: `$env:JAVA_HOME="C:\Users\yanni\.jdks\temurin-11"; & "C:\Users\yanni\AppData\Local\Programs\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" install`
- Build-Kommandos laufen im Repo-Root `AJAN-service/`, außer wo explizit anders angegeben.
- **Verifikation pro Task:** `mvn install` grün (bzw. der im Task genannte Teil-Reaktor). Am Etappen-Ende (Task 6): voller `mvn install` **und** `mvn -f e2e/pom.xml verify` grün.

### Rot-Fenster-Regel (wichtig)

Die Spec-Invariante „jede Etappe endet grün" gilt für die **Etappe**, nicht für jeden Zwischen-Task. Ab **Task 2** (neuer Triplestore serviert RDF4J 5, Client noch 3.6.3) bis einschließlich **Task 5** ist `mvn -f e2e/pom.xml verify` **erwartet rot**, und zwar mit exakt der Spike-A-Signatur:

- 16 Tests, **10 Failures**, 0 Errors, 0 Skipped
- `AgentLifecycleIT` 6/6 rot, `RdfResponseIT` 4/4 rot
- `ErrorPathIT` 3/3, `PluginLoadIT` 1/1, `SystemSmokeIT` 2/2 **grün**
- Erster Fehler: `agent creation should succeed, was 500: ... TripleStoreException: Unable to query repository! ... expected: <200> but was: <500>`

**Jede abweichende Fehlersignatur ist ein echter Befund und muss den Task stoppen** (nicht „wegarbeiten"): weniger als 10 Failures, Errors statt Failures, ein rotes `SystemSmokeIT`/`PluginLoadIT` oder ein anderer Erstfehler bedeuten, dass der neue Triplestore anders kaputt ist als erwartet. In Task 6 muss das Fenster wieder geschlossen sein (16/16 grün).

### Abbruchbedingung (Rückfallebene)

**Task 1 ist ein Gate.** Trägt RDF4J 5.3.1 unter Spring Boot 1.3.5 / Java 11 auch mit Versions-Property-Overrides nicht, dann greift die vom Auftraggeber mitbeschlossene Rückfallebene: **Etappe 3 wird mit Etappe 4 zusammengelegt**, dieser Plan endet nach Task 1, und das Ergebnis geht zurück an den Auftraggeber. In diesem Fall dürfen die Tasks 2–7 **nicht** ausgeführt werden.

---

### Task 1: Risiko-Gate — trägt RDF4J 5.3.1 unter Spring Boot 1.3.5 / Java 11?

**Kontext:** Offene Frage 4 der Spike-Ergebnisse. Spike A hat nur den **alten** Client gegen einen **neuen** Server getestet — nie den neuen Client im Boot-1.3-Dependency-Management. V5 zeigt den konkreten Verdacht: Boot 1.3.5 zwingt Jackson auf 2.6.6, RDF4J 5.3.1 rechnet mit 2.21. Dieser Task beantwortet das empirisch, **bevor** Produktcode angefasst wird, und liefert als Nebenprodukt die exakte Liste der nötigen Versions-Overrides für Task 5.

**Files:**
- Create: `spikes/rdf4j5-boot13/pom.xml`
- Create: `spikes/rdf4j5-boot13/src/main/java/spike/Rdf4j5Boot13Check.java`
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (neuer Abschnitt „Gate E")

**Interfaces:**
- Consumes: Docker (offizielles Image `eclipse/rdf4j-workbench:5.1.2`, aus Spike A als existierend bekannt; Kontextpfad dort `/rdf4j-server`).
- Produces:
  - Entscheidung „Etappe 3 eigenständig durchführbar: ja/nein" (Abbruchbedingung oben).
  - Die minimale Menge an Versions-Property-Overrides für `executionservice/pom.xml` in Task 5, in der Form `<jackson.version>…</jackson.version>` / `<httpclient.version>…</httpclient.version>` / `<slf4j.version>…</slf4j.version>`.

- [ ] **Step 1: RDF4J-5-Server als Container starten**

Vorher sicherstellen, dass lokal **kein** Triplestore auf 8090 läuft.

```bash
docker run -d --name rdf4j5-gate -p 8090:8080 eclipse/rdf4j-workbench:5.1.2
curl -fsS http://localhost:8090/rdf4j-server/protocol
```

Expected: Container läuft, Protokollversion `12`. (Der Kontextpfad des offiziellen Images ist `/rdf4j-server`, nicht `/rdf4j` — bekannt aus Spike A und für diesen Gate folgenlos.)

- [ ] **Step 2: Spike-Projekt anlegen — Variante A (ohne Overrides)**

`spikes/rdf4j5-boot13/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.dfki.asr.ajan.spikes</groupId>
  <artifactId>rdf4j5-boot13</artifactId>
  <version>0.1</version>
  <packaging>jar</packaging>
  <!-- Wegwerf-Gate (s. spikes/README.md): beantwortet offene Frage 4 der
       Spike-Ergebnisse. Bewusst mit demselben Parent wie executionservice,
       damit das Boot-1.3-dependencyManagement wirksam wird. -->

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>1.3.5.RELEASE</version>
    <relativePath/>
  </parent>

  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.eclipse.rdf4j</groupId>
      <artifactId>rdf4j-runtime</artifactId>
      <version>5.3.1</version>
      <type>pom</type>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <mainClass>spike.Rdf4j5Boot13Check</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

Hinweis: `maven-compiler-plugin` aus Boot 1.3.5 ist Version 3.1 und kennt `<release>` nicht (Befund aus Plan 1, Task 7) — deshalb hier bewusst `source`/`target` statt `release`.

- [ ] **Step 3: Probe-Programm schreiben**

`spikes/rdf4j5-boot13/src/main/java/spike/Rdf4j5Boot13Check.java`:

```java
package spike;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

/**
 * Gate fuer Etappe 3: laeuft der RDF4J-5.3.1-Client unter dem
 * Spring-Boot-1.3.5-dependencyManagement (Jackson/httpclient/slf4j) auf JDK 11
 * gegen einen echten RDF4J-5-Server?
 *
 * Geprueft werden genau die drei Pfade, an denen der Produktcode haengt:
 *   (1) Repository-Verwaltung ueber RemoteRepositoryManager,
 *   (2) SPARQL-GraphQuery ueber Repositories.graphQuery  <-- die Spike-A-Bruchstelle,
 *   (3) Rio-Serialisierung Turtle UND JSON-LD            <-- der Jackson-Pfad.
 *
 * Aufruf: mvn -f spikes/rdf4j5-boot13/pom.xml compile exec:java [-Dgate.server=<url>]
 * Exit 0 = gruen, Exception/Exit != 0 = rot (Meldung ist das Ergebnis).
 */
public final class Rdf4j5Boot13Check {

    private static final String REPO_ID = "gate_e";

    private Rdf4j5Boot13Check() { }

    public static void main(final String[] args) throws Exception {
        String server = System.getProperty("gate.server", "http://localhost:8090/rdf4j-server");
        System.out.println("jackson-databind : " + locate("com.fasterxml.jackson.databind.ObjectMapper"));
        System.out.println("httpclient       : " + locate("org.apache.http.impl.client.HttpClientBuilder"));
        System.out.println("slf4j-api        : " + locate("org.slf4j.LoggerFactory"));
        System.out.println("rdf4j-model      : " + locate("org.eclipse.rdf4j.model.impl.SimpleValueFactory"));

        RemoteRepositoryManager manager = new RemoteRepositoryManager(server);
        manager.init();
        System.out.println("(1) RemoteRepositoryManager.init OK, repos=" + manager.getAllRepositories().size());

        manager.addRepositoryConfig(
                new RepositoryConfig(REPO_ID, new SailRepositoryConfig(new MemoryStoreConfig())));
        Repository repo = manager.getRepository(REPO_ID);
        try (RepositoryConnection conn = repo.getConnection()) {
            String turtle = "@prefix ex: <http://ajan.gate/> .\n"
                    + "ex:a ex:p ex:b .\n"
                    + "ex:b ex:q \"literal\" .\n";
            conn.add(new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)), "", RDFFormat.TURTLE);
        }
        System.out.println("(1b) upload OK");

        // (2) exakt der Aufruf, an dem Spike A gescheitert ist
        Model result = Repositories.graphQuery(repo,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }", r -> QueryResults.asModel(r));
        if (result.size() != 2) {
            throw new IllegalStateException("(2) graphQuery: expected 2 statements, got " + result.size());
        }
        System.out.println("(2) Repositories.graphQuery OK, statements=" + result.size());

        // (3) Rio round-trip Turtle und JSON-LD (JSON-LD zieht Jackson)
        roundTrip(result, RDFFormat.TURTLE);
        roundTrip(result, RDFFormat.JSONLD);

        manager.removeRepository(REPO_ID);
        manager.shutDown();
        System.out.println("GATE E: GREEN");
    }

    private static void roundTrip(final Model model, final RDFFormat format) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(model, out, format);
        Model back = Rio.parse(new ByteArrayInputStream(out.toByteArray()), "", format);
        if (back.size() != model.size()) {
            throw new IllegalStateException("(3) " + format.getName() + " round-trip lost statements: "
                    + model.size() + " -> " + back.size());
        }
        System.out.println("(3) Rio round-trip OK for " + format.getName());
    }

    private static String locate(final String className) {
        try {
            return Class.forName(className).getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (ClassNotFoundException | NullPointerException e) {
            return "<not resolvable: " + e + ">";
        }
    }
}
```

- [ ] **Step 4: Variante A laufen lassen und Belege sichern**

```bash
mvn -f spikes/rdf4j5-boot13/pom.xml -B -ntp dependency:tree > spikes/rdf4j5-boot13/tree-A.txt
mvn -f spikes/rdf4j5-boot13/pom.xml -B -ntp compile exec:java
```

Expected: `dependency:tree` zeigt die tatsächlich aufgelösten Versionen von `jackson-databind`, `httpclient` und `slf4j-api` (Erwartung nach V5: 2.6.6 / 4.5.2 / 1.7.21). Der Lauf endet entweder mit `GATE E: GREEN` oder mit einer Exception — **beides ist ein gültiges Ergebnis**; Konsolenausgabe vollständig in die Scratchpad-Datei `gate-e-run-A.txt` sichern (Statuszeilen, Stacktrace, Exit-Code).

- [ ] **Step 5: Variante B — Overrides einzeln zuschalten**

Nur ausführen, wenn Variante A rot war. Die drei Properties **einzeln nacheinander** in `spikes/rdf4j5-boot13/pom.xml` unter `<properties>` ergänzen und nach jeder Ergänzung erneut laufen lassen, damit die **minimale** nötige Menge herauskommt (nicht alle drei auf einmal):

```xml
<jackson.version>2.21.0</jackson.version>
<httpclient.version>4.5.14</httpclient.version>
<slf4j.version>1.7.36</slf4j.version>
```

Reihenfolge: erst `jackson.version` (der wahrscheinlichste Verursacher, V5), dann `httpclient.version`, dann `slf4j.version`. Nach jedem Lauf `dependency:tree` und Konsolenausgabe sichern (`tree-B1.txt`/`gate-e-run-B1.txt`, `-B2`, `-B3`). Sobald `GATE E: GREEN` erscheint, aufhören — die bis dahin gesetzten Properties sind das Ergebnis.

Expected: eine der Stufen liefert `GATE E: GREEN`. Falls **auch mit allen drei Overrides** rot: die genaue Fehlermeldung ist das Ergebnis (s. Step 7).

- [ ] **Step 6: Aufräumen**

```bash
docker rm -f rdf4j5-gate
```

- [ ] **Step 7: Ergebnis dokumentieren**

In `docs/superpowers/specs/2026-07-11-spike-results.md` **vor** dem Abschnitt „Konsequenzen fuer die Folgeplaene" einfügen:

```markdown
## Gate E (Plan 2, Task 1): RDF4J 5.3.1 unter Spring Boot 1.3.5 / Java 11

- Server: eclipse/rdf4j-workbench:5.1.2 (Kontextpfad /rdf4j-server), Protokollversion <n>
- Variante A (ohne Overrides): <gruen | rot mit exakter Fehlermeldung>
  - aufgeloeste Versionen: jackson-databind <x>, httpclient <y>, slf4j-api <z>
- Variante B (minimale Overrides): <Liste der gesetzten Properties | entfaellt>
  - aufgeloeste Versionen danach: <...>
- Geprueft: (1) RemoteRepositoryManager.init + addRepositoryConfig, (2) Repositories.graphQuery
  (die Spike-A-Bruchstelle), (3) Rio-Round-Trip Turtle und JSON-LD.
- **Ergebnis:** Etappe 3 eigenstaendig durchfuehrbar: <ja | nein>
- **Konsequenz fuer Task 5:** <exakte Property-Liste fuer executionservice/pom.xml | Rueckfallebene gezogen>
```

Zusätzlich **offene Frage 4** im Abschnitt „Offene Fragen" als beantwortet markieren (Verweis auf Gate E), ohne den historischen Text zu löschen.

- [ ] **Step 8: Entscheidung fällen**

- `GATE E: GREEN` (mit oder ohne Overrides) ⇒ Tasks 2–7 werden ausgeführt.
- Rot auch mit allen drei Overrides ⇒ **Abbruchbedingung greift**: Plan 2 endet hier, Ergebnis an den Auftraggeber melden (Zusammenlegung Etappe 3+4). Tasks 2–7 **nicht** starten.

- [ ] **Step 9: Commit**

```bash
git add spikes/rdf4j5-boot13 docs/superpowers/specs/2026-07-11-spike-results.md
git commit -m "spike: gate E - rdf4j 5.3.1 under spring boot 1.3.5 / java 11 (record result)"
git push
```

**Verifikation:** Kein Produktcode berührt ⇒ `mvn install` und die E2E-Suite sind unverändert vom letzten grünen Stand (d894fc9d). Ein Beleglauf `mvn -f e2e/pom.xml validate` (Exit 0) genügt; die Gate-Belege sind Konsole + `tree-*.txt`.

---

### Task 2: Triplestore-Neubau — Embedded-Tomcat-9-Launcher mit RDF4J-5-WARs

**Kontext:** Ersetzt `tomcat8-maven-plugin` (retired) und das eingecheckte 4.0.0-WAR (V16) durch ein reguläres Java-Modul. Nach diesem Task serviert der Triplestore RDF4J 5.3.1, während der Client noch auf 3.6.3 steht — **hier öffnet sich das Rot-Fenster** (s. Rot-Fenster-Regel).

**Files:**
- Modify: `triplestore/pom.xml` (kompletter Ersatz des `<build>`-Blocks, `<repositories>`/`<pluginRepositories>` raus)
- Create: `triplestore/src/main/java/de/dfki/asr/ajan/triplestore/TriplestoreLauncher.java`
- Delete: `triplestore/lib/rdf4j-server/rdf4j-server/4.0.0/rdf4j-server-4.0.0.war` (und die leeren Elternverzeichnisse bis `triplestore/lib/`)
- Modify: `startTriplestore.sh`, `startTriplestore.bat`
- Modify: `.github/startTriplestore.sh`, `.github/startTriplestore.bat`
- Modify: `Dockerfile:27`
- Modify: `README.md:35`
- Modify: `e2e/src/test/java/de/dfki/asr/ajan/e2e/AjanSystem.java:59`

**Interfaces:**
- Consumes: nichts aus Task 1 (nur dessen Freigabe).
- Produces:
  - Artefakt `triplestore/target/triplestore-0.1.jar` (ausführbares Fat-JAR, Main-Class `de.dfki.asr.ajan.triplestore.TriplestoreLauncher`).
  - CLI-Vertrag: `java -jar triplestore-0.1.jar --httpPort=8090`; zusätzlich akzeptiert werden `-httpPort 8090`, `-httpPort=8090` und `--httpPort 8090` (die vorhandenen Skripte nutzen beide Schreibweisen). Ohne Argument: Port 8080 (wie beim bisherigen exec-war-Artefakt).
  - HTTP-Oberfläche: `http://localhost:<port>/rdf4j/...` (RDF4J-Server-REST-API) und `http://localhost:<port>/workbench` (Workbench-UI).
  - Extraktionsverzeichnis: `./.extract/` relativ zum Arbeitsverzeichnis (bereits durch `.gitignore:8` (`*.extract/`) ignoriert).

- [ ] **Step 1: `triplestore/pom.xml` ersetzen**

Vollständiger neuer Inhalt:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <artifactId>triplestore</artifactId>

    <parent>
        <groupId>de.dfki.asr.ajan</groupId>
        <artifactId>ajan-parent</artifactId>
        <version>0.1</version>
    </parent>

    <!-- Embedded-Tomcat-Launcher statt tomcat8-maven-plugin (retired).
         Tomcat 9 und NICHT 10/11: die RDF4J-5-WARs sind javax.servlet
         (web-app 2.4, spring-webmvc 5.3.x, jstl 1.2) - Jakarta wuerde nicht
         deployen. Die WAR-Version ist hier bewusst hart gepinnt, weil die
         Parent-Property zum Zeitpunkt dieses Moduls noch auf 3.6.3 steht;
         sie wird mit dem zentralen Bump auf ${org.eclipse.rdf4j.version}
         umgestellt. -->
    <properties>
        <tomcat.version>9.0.109</tomcat.version>
        <rdf4j.server.version>5.3.1</rdf4j.server.version>
        <mainClass>de.dfki.asr.ajan.triplestore.TriplestoreLauncher</mainClass>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-core</artifactId>
            <version>${tomcat.version}</version>
        </dependency>
        <!-- Das RDF4J-Server-WAR enthaelt 23 JSPs; ohne Jasper bleibt die
             Workbench-Navigation und Teile der Server-UI tot. -->
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-jasper</artifactId>
            <version>${tomcat.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-el</artifactId>
            <version>${tomcat.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Die beiden WARs werden NICHT als <dependency> deklariert (sie
                 gehoeren nicht auf den Compile-Classpath), sondern als
                 Ressourcen in das Fat-JAR kopiert und zur Laufzeit entpackt. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <id>copy-rdf4j-wars</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>copy</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${project.build.outputDirectory}/webapps</outputDirectory>
                            <overWriteReleases>true</overWriteReleases>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>org.eclipse.rdf4j</groupId>
                                    <artifactId>rdf4j-http-server</artifactId>
                                    <version>${rdf4j.server.version}</version>
                                    <type>war</type>
                                    <destFileName>rdf4j.war</destFileName>
                                </artifactItem>
                                <artifactItem>
                                    <groupId>org.eclipse.rdf4j</groupId>
                                    <artifactId>rdf4j-http-workbench</artifactId>
                                    <version>${rdf4j.server.version}</version>
                                    <type>war</type>
                                    <destFileName>workbench.war</destFileName>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.3</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>${mainClass}</mainClass>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

Entfallen sind damit: `<repositories>` (File-Repo `lib`), `<pluginRepositories>` (tote Repos icm/alfresco) und der gesamte `tomcat8-maven-plugin`-Block.

- [ ] **Step 2: Launcher-Klasse schreiben**

`triplestore/src/main/java/de/dfki/asr/ajan/triplestore/TriplestoreLauncher.java`:

```java
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

package de.dfki.asr.ajan.triplestore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * Startet einen Embedded Tomcat 9 und deployed die mitgelieferten
 * RDF4J-Server- und Workbench-WARs unter /rdf4j und /workbench.
 *
 * Ersetzt das frueher per tomcat8-maven-plugin (exec-war-only) gebaute
 * triplestore-0.1-war-exec.jar. Ports, Kontextpfade und CLI bleiben identisch:
 *   java -jar triplestore-0.1.jar --httpPort=8090
 *
 * Tomcat 9 (nicht 10/11): die RDF4J-5-WARs sind javax.servlet.
 */
public final class TriplestoreLauncher {

    private static final int DEFAULT_PORT = 8080;
    private static final String PORT_OPTION = "httpPort";
    private static final String EXTRACT_DIR = ".extract";

    private TriplestoreLauncher() { }

    public static void main(final String[] args) throws IOException, LifecycleException {
        int port = parsePort(args);
        Path base = Paths.get(EXTRACT_DIR).toAbsolutePath().normalize();
        Files.createDirectories(base);

        Path serverWar = extract("webapps/rdf4j.war", base.resolve("rdf4j.war"));
        Path workbenchWar = extract("webapps/workbench.war", base.resolve("workbench.war"));

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toString());
        tomcat.setPort(port);
        tomcat.getConnector();
        // Parity mit der frueheren Plugin-Konfiguration (enableNaming=true).
        tomcat.enableNaming();
        tomcat.getHost().setAppBase(base.toString());
        // Die WARs werden explizit deployed; kein zusaetzliches Auto-Deploy,
        // sonst wuerde Tomcat sie ein zweites Mal unter /rdf4j bzw. /workbench
        // aus dem appBase-Verzeichnis aufsammeln.
        tomcat.getHost().setDeployOnStartup(false);
        tomcat.getHost().setAutoDeploy(false);

        tomcat.addWebapp("/rdf4j", serverWar.toString());
        tomcat.addWebapp("/workbench", workbenchWar.toString());

        tomcat.start();
        System.out.println("AJAN triplestore started: http://localhost:" + port + "/rdf4j"
                + " (workbench: http://localhost:" + port + "/workbench)");
        tomcat.getServer().await();
    }

    /**
     * Akzeptiert alle Schreibweisen, die die bestehenden Start-Skripte und
     * supervisord verwenden: --httpPort=N, -httpPort N, -httpPort=N,
     * --httpPort N. Unbekannte Argumente werden ignoriert.
     */
    static int parsePort(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String stripped = arg.startsWith("--") ? arg.substring(2)
                    : arg.startsWith("-") ? arg.substring(1) : null;
            if (stripped == null) {
                continue;
            }
            if (stripped.startsWith(PORT_OPTION + "=")) {
                return Integer.parseInt(stripped.substring(PORT_OPTION.length() + 1).trim());
            }
            if (stripped.equals(PORT_OPTION) && i + 1 < args.length) {
                return Integer.parseInt(args[i + 1].trim());
            }
        }
        return DEFAULT_PORT;
    }

    /**
     * Entpackt ein WAR aus dem Fat-JAR neben den Tomcat-Basedir. Idempotent:
     * bei gleicher Groesse wird nicht erneut geschrieben, damit wiederholte
     * Starts (E2E-Suite startet das System pro IT-Klasse neu) nicht jedes Mal
     * ueber 100 MB kopieren.
     */
    private static Path extract(final String resource, final Path target) throws IOException {
        try (InputStream in = TriplestoreLauncher.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("bundled webapp missing from jar: " + resource);
            }
            if (Files.exists(target)) {
                long bundled = countBytes(resource);
                if (Files.size(target) == bundled) {
                    return target;
                }
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private static long countBytes(final String resource) throws IOException {
        try (InputStream in = TriplestoreLauncher.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return -1;
            }
            long total = 0;
            byte[] buffer = new byte[65536];
            int read = in.read(buffer);
            while (read >= 0) {
                total += read;
                read = in.read(buffer);
            }
            return total;
        }
    }
}
```

- [ ] **Step 3: Eingechecktes 4.0.0-WAR entfernen**

```bash
git rm -r triplestore/lib
```

Expected: 33 MB Binär-Altlast weg; `git status` zeigt `triplestore/lib/rdf4j-server/rdf4j-server/4.0.0/rdf4j-server-4.0.0.war` als gelöscht. Das POM referenziert das File-Repository nach Step 1 nicht mehr.

- [ ] **Step 4: Bauen und den Launcher isoliert verifizieren**

```bash
mvn -pl triplestore -am install
ls -lh triplestore/target/triplestore-0.1.jar
```

Expected: `BUILD SUCCESS`; das JAR existiert (Größenordnung ~115 MB — beide WARs sind eingebettet, das alte war-exec-JAR war 70 MB; der Zuwachs stammt aus dem Sprung von RDF4J 4.0.0/3.6.3 auf 5.3.1 und ist erwartet).

Danach in einem separaten Terminal manuell prüfen (im Repo-Root, kein anderer Triplestore auf 8090):

```bash
java -jar triplestore/target/triplestore-0.1.jar --httpPort=8090
```

und in einem zweiten Terminal:

```bash
curl -fsS http://localhost:8090/rdf4j/protocol
curl -fsS http://localhost:8090/rdf4j/repositories
curl -fsS -o /dev/null -w "%{http_code}\n" http://localhost:8090/workbench
```

Expected: `/rdf4j/protocol` liefert `12`; `/rdf4j/repositories` liefert eine (leere) SPARQL-Results-Tabelle mit HTTP 200; `/workbench` liefert 200 oder 302 (Redirect-Filter des Workbench-WARs). Alle drei Werte im Task-Report festhalten. Danach den Prozess beenden und zusätzlich die zweite CLI-Schreibweise gegenprüfen: `java -jar triplestore/target/triplestore-0.1.jar -httpPort 8090` muss auf demselben Port lauschen.

- [ ] **Step 5: Konsumenten des Artefaktnamens nachziehen**

`startTriplestore.bat` (eine Zeile):

```bat
java -jar triplestore/target/triplestore-0.1.jar -httpPort 8090
```

`startTriplestore.sh`:

```bash
#!/bin/bash
java -jar triplestore-0.1.jar -httpPort 8090
```

`.github/startTriplestore.bat`:

```bat
java -jar triplestore-0.1.jar -httpPort 8090
```

`.github/startTriplestore.sh` (Zeile 3):

```bash
java -jar triplestore-0.1.jar -httpPort 8090
```

`Dockerfile:27`:

```dockerfile
COPY triplestore-0.1.jar  /app/triplestore.jar
```

`README.md:35`:

```markdown
* run `startTriplestore.bat` or cmd: `java -jar triplestore/target/triplestore-0.1.jar --httpPort=8090`
```

`e2e/src/test/java/de/dfki/asr/ajan/e2e/AjanSystem.java:59`:

```java
            Path triplestoreJar = resolveSingleArtifact(root.resolve("triplestore/target"), "triplestore-*.jar");
```

Hinweis: Der Glob bleibt eindeutig — `maven-shade-plugin` legt das unshaded Original als `original-triplestore-0.1.jar` ab, was auf `triplestore-*.jar` nicht passt. Beim ersten E2E-Lauf verifizieren, dass `resolveSingleArtifact` genau einen Treffer meldet.

**`.github/workflows/docker-image.yml` wird NICHT angefasst** (Zeilen 42/173/174 verweisen weiter auf `triplestore-0.1-war-exec.jar`). Das ist die in Entscheidung 2 dokumentierte, bewusste Lücke; sie wird in Etappe 5 geschlossen und ist ungefährlich, weil der Workflow ausschließlich auf `master` triggert und dieser Branch nicht gemerged wird.

- [ ] **Step 6: Voller Build und charakterisiertes Rot-Fenster**

```bash
mvn install
mvn -f e2e/pom.xml verify
```

Expected: `mvn install` **grün** (21 Module). `mvn -f e2e/pom.xml verify` ist **erwartet ROT** mit exakt der in der Rot-Fenster-Regel beschriebenen Signatur (16 Tests, 10 Failures, `AgentLifecycleIT` 6/6 + `RdfResponseIT` 4/4 rot, die anderen drei IT-Klassen grün, Erstfehler 500 `Unable to query repository!`).

Die tatsächliche Zusammenfassung (`Tests run: … Failures: … Errors: …` sowie den Erstfehler) im Task-Report festhalten. **Weicht die Signatur ab, Task stoppen und melden** — insbesondere ein rotes `SystemSmokeIT` (Triplestore selbst kaputt) oder ein rotes `PluginLoadIT`.

- [ ] **Step 7: Commit**

```bash
git add triplestore Dockerfile README.md startTriplestore.sh startTriplestore.bat .github/startTriplestore.sh .github/startTriplestore.bat e2e/src/test/java/de/dfki/asr/ajan/e2e/AjanSystem.java
git commit -m "build: replace tomcat8 exec-war triplestore with embedded tomcat 9 launcher and rdf4j 5 wars"
git push
```

**Verifikation:** `mvn install` grün; Launcher manuell gegen `/rdf4j/protocol`, `/rdf4j/repositories`, `/workbench` und beide CLI-Schreibweisen belegt; E2E rot mit dokumentierter Spike-A-Signatur. Der CI-Lauf `ci-branch` ist ab hier bis Task 6 **erwartet rot** (E2E-Schritt) — das im Task-Report notieren, nicht „reparieren".

---

### Task 3: RDF4J-API-Vorbereitung im Produktcode (noch unter 3.6.3)

**Kontext:** V6/V7/V8 benennen drei APIs, die es in 5.3.1 nicht mehr gibt. Dieser Task ersetzt sie durch Konstrukte, die **unter 3.6.3 und 5.3.1 gleichermaßen kompilieren**, und sichert das Verhalten vorher mit Charakterisierungstests ab. Dadurch ist der Versions-Flip in Task 5 ein reiner Property-Wechsel statt einer Umbau-Aktion unter rotem Compiler.

Reine Refaktorierung: die Tests werden **gegen die heutige Implementierung** geschrieben und müssen **vor und nach** dem Umbau grün sein. Kein Verhalten wird geändert.

**Files:**
- Modify: `common/pom.xml` (Test-Dependencies ergänzen)
- Create: `common/src/test/java/de/dfki/asr/ajan/common/SPARQLUtilQueryTest.java`
- Modify: `common/src/main/java/de/dfki/asr/ajan/common/SPARQLUtil.java` (Zeilen 58-59, 63, 118-141, 156-161, 250-272)
- Modify: `common/src/main/java/de/dfki/asr/ajan/common/RDF4JTripleStoreManager.java:225`
- Modify: `executionservice/src/main/java/de/dfki/asr/ajan/data/AgentModelManager.java:44-45, 60-63`

**Interfaces:**
- Consumes: nichts.
- Produces: `SPARQLUtil` ohne `queryrender.builder`- und `SailQueryPreparer`-Abhängigkeit. Alle öffentlichen Signaturen bleiben **unverändert**:
  - `public static Model queryModel(Model, ParsedGraphQuery)`
  - `public static List<BindingSet> queryModel(Model, ParsedTupleQuery)`
  - `public static ParsedGraphQuery getDescribeQuery(Iterator<Resource>)`
  - `public static ParsedTupleQuery getSelectQuery(TupleExpr, List<String>)`
  - `public static ParsedTupleQuery getSelectQuery(String, List<String>)`
  - `public static SailRepository createRepository(Model)`

- [ ] **Step 1: Caller-Inventar erstellen (Grundlage für die Tests)**

```bash
grep -rn --include=*.java -E "SPARQLUtil\.(queryModel|getSelectQuery|getDescribeQuery|getTupleExpr|getUpdateExpr|queryRepository)" . | grep -v "/target/"
```

Expected (beim Planschreiben ermittelt, als Soll-Abgleich): `behaviour/.../ACTNUtil.java:118`, `behaviour/.../BoundModel.java:51`, `behaviour/.../PerformHttpAbortRequest.java:51`, `behaviour/.../PerformHttpExecuteRequest.java:60`, `behaviour/.../Action.java:212`, `behaviour/.../BehaviorAskQuery.java:66`, `behaviour/.../BehaviorConstructQuery.java:56`, `executionservice/.../AgentModelManager.java:63,78,80,83,101,103`, `pluginsystem/plugins/MappingPlugin/.../MappingUtil.java:106,116`, `pluginsystem/plugins/STRIPSPlugin/.../OperatorBuilder.java:136`. Abweichungen im Report notieren.

- [ ] **Step 2: Test-Dependencies in `common/pom.xml` ergänzen**

`common` hat heute kein Testverzeichnis. In `common/pom.xml` im `<dependencies>`-Block (Versionen kommen aus dem `dependencyManagement` des Parents: TestNG 7.10.2, Hamcrest 2.2) ergänzen:

```xml
    <dependency>
      <artifactId>testng</artifactId>
      <groupId>org.testng</groupId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <artifactId>hamcrest-core</artifactId>
      <groupId>org.hamcrest</groupId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <artifactId>rdf4j-rio-turtle</artifactId>
      <groupId>org.eclipse.rdf4j</groupId>
      <scope>test</scope>
    </dependency>
```

(`rdf4j-rio-turtle` kommt versionslos über den bereits vorhandenen `rdf4j-bom`-Import.)

- [ ] **Step 3: Charakterisierungstests schreiben**

`common/src/test/java/de/dfki/asr/ajan/common/SPARQLUtilQueryTest.java`:

```java
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
 * 3.6.3 -> 5.3.1 umgebaut werden muessen (QueryBuilderFactory und
 * SailQueryPreparer existieren in RDF4J 5 nicht mehr). Die Tests sind vor
 * dem Umbau gegen die alte Implementierung geschrieben und muessen danach
 * unveraendert gruen bleiben.
 */
public class SPARQLUtilQueryTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final IRI A = VF.createIRI("http://ajan.test/a");
	private static final IRI B = VF.createIRI("http://ajan.test/b");
	private static final IRI C = VF.createIRI("http://ajan.test/c");

	private static final String DATA =
			"@prefix t: <http://ajan.test/> .\n"
			+ "t:a t:p t:b .\n"
			+ "t:b t:q \"literal\" .\n"
			+ "t:c t:r t:a .\n";

	private Model data() throws IOException {
		return SPARQLUtil.createModel(DATA, RDFFormat.TURTLE);
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
```

- [ ] **Step 4: Tests gegen die ALTE Implementierung laufen lassen**

```bash
mvn -pl common -am install
```

Expected: `BUILD SUCCESS`, im `common`-Modul `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`. **Erst weitermachen, wenn diese Baseline grün ist** — sie ist der Maßstab für den Umbau. Schlägt ein Test fehl, ist die Erwartung falsch, nicht der Produktcode: Assertion an das beobachtete IST-Verhalten anpassen und die Abweichung im Report dokumentieren.

- [ ] **Step 5: `SPARQLUtil` umbauen**

(a) Diese Imports löschen:

```java
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.repository.sail.SailQueryPreparer;
```

und diese ergänzen:

```java
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;
```

(`ParsedTupleQuery` (Zeile 54), `ParsedGraphQuery`, `GraphQueryResult`, `QueryResults`, `TupleQueryResult`, `SailRepositoryConnection` und `SPARQLParser` sind bereits importiert und bleiben.)

(b) `queryModel(Model, ParsedGraphQuery)` und `queryModel(Model, ParsedTupleQuery)` ersetzen — statt `SailQueryPreparer` wird die Query gerendert und über die Connection vorbereitet. Das ist derselbe Weg, den `queryRepository(Repository, ParsedQuery)` heute schon in Produktion geht (V15):

```java
	public static Model queryModel(final Model model, final ParsedGraphQuery query) throws QueryEvaluationException {
		SailRepository repo = createRepository(model);
		Model resultModel;
		try (SailRepositoryConnection conn = repo.getConnection()) {
			conn.begin();
			// RDF4J 5 hat SailQueryPreparer entfernt; die Query wird deshalb
			// gerendert und ueber die Connection vorbereitet (derselbe Weg wie
			// in queryRepository(Repository, ParsedQuery)).
			GraphQuery graphQuery = conn.prepareGraphQuery(renderQuery(query));
			GraphQueryResult results = graphQuery.evaluate();
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
			TupleQuery tupleQuery = conn.prepareTupleQuery(renderQuery(query));
			TupleQueryResult results = tupleQuery.evaluate();
			resultModel = getBindingSetList(results);
			conn.commit();
		}
		repo.shutDown();
		return resultModel;
	}
```

(c) `createRepository` auf `init()` umstellen (V6; `init()` existiert auch in 3.6.3):

```java
	public static SailRepository createRepository(final Model model) {
		SailRepository repo = new SailRepository(new MemoryStore());
		repo.init();
		Repositories.consume(repo, conn -> conn.add(model));
		return repo;
	}
```

(d) `getSelectQuery(TupleExpr, List<String>)` ersetzen — der Builder war wirkungslos (V14):

```java
	public static ParsedTupleQuery getSelectQuery(final TupleExpr tupleExpr, final List<String> varNames) {
		// Frueher ueber QueryBuilderFactory.select(): dessen Projektion wurde
		// unmittelbar durch setTupleExpr(tupleExpr) ueberschrieben, varNames
		// blieb also ohne Wirkung. Der Konstruktor bildet das 1:1 ab und
		// existiert in RDF4J 3.6 wie 5.x.
		return new ParsedTupleQuery(tupleExpr);
	}
```

Die überladene Variante `getSelectQuery(String, List<String>)` bleibt unverändert (sie ruft die obige auf). Der Parameter `varNames` bleibt in der Signatur, damit die Aufrufer (`ACTNUtil.java:118`) unverändert bleiben; der Kommentar erklärt, warum er ungenutzt ist. Falls Checkstyle/PMD über den ungenutzten Parameter meckert, `@SuppressWarnings("PMD.UnusedFormalParameter")` an die Methode setzen.

(e) `getDescribeQuery(Iterator<Resource>)` und die zwei Helfer `setDescribeQueryParameters`/`setDescribeFilter`/`setSameTerm` ersetzen — die Query wird als SPARQL-String gebaut und geparst. Die Form entspricht exakt der bisher gerenderten (V13):

```java
	public static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
		// Frueher ueber QueryBuilderFactory.construct(); das Paket
		// org.eclipse.rdf4j.queryrender.builder existiert in RDF4J 5 nicht
		// mehr. Erzeugt wird dieselbe CONSTRUCT-Query wie bisher.
		StringBuilder query = new StringBuilder(
				"CONSTRUCT { ?descr_subj ?descr_pred ?descr_obj } "
				+ "WHERE { ?descr_subj ?descr_pred ?descr_obj . FILTER ( ");
		boolean first = true;
		while (resourceIterator.hasNext()) {
			Resource resource = resourceIterator.next();
			if (!first) {
				query.append(" || ");
			}
			first = false;
			String value = "<" + resource.stringValue() + ">";
			query.append("sameTerm(").append(value).append(", ?descr_subj) || ")
				.append("sameTerm(").append(value).append(", ?descr_obj)");
		}
		query.append(" ) }");
		return (ParsedGraphQuery) new SPARQLParser().parseQuery(query.toString(), null);
	}
```

Die Methoden `setDescribeQueryParameters`, `setDescribeFilter` und `setSameTerm` ersatzlos löschen; danach die dadurch unbenutzten Imports (`org.eclipse.rdf4j.query.algebra.Or`, `SameTerm`, `ValueConstant`, `ValueExpr`, `Var`) entfernen — `TupleExpr` und `UpdateExpr` bleiben.

**Achtung Blank Nodes:** `resource.stringValue()` liefert bei einem BNode nur die ID ohne Präfix. Die bisherigen Aufrufer liefern IRIs (`AgentResourceManager.getResources` über `AJANVocabulary.AGENT_HAS_INITKNOWLEDGE`), aber falls beim Testlauf eine `MalformedQueryException` auftaucht, ist das die Ursache — dann im Report melden und mit `resource instanceof IRI` absichern.

- [ ] **Step 6: `RDF4JTripleStoreManager` auf `init()` umstellen**

In `common/src/main/java/de/dfki/asr/ajan/common/RDF4JTripleStoreManager.java:225`:

```java
		if (!repoManager.isInitialized()) {
			repoManager.init();
		}
```

(Die Konstruktoren dieser Klasse rufen bereits `init()` — hier war eine Stelle übrig geblieben.)

- [ ] **Step 7: `AgentModelManager` auf die vorhandene Describe-Methode umstellen**

In `executionservice/src/main/java/de/dfki/asr/ajan/data/AgentModelManager.java` die Imports der Zeilen 44-45 löschen:

```java
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
```

und `getTemplateFromTDB` umbauen (V13: `QueryBuilderFactory.describe(r)` und `SPARQLUtil.getDescribeQuery([r])` erzeugen dieselbe Query):

```java
	public Model getTemplateFromTDB(final Repository repo, final Resource resource) {
		// QueryBuilderFactory.describe(resource) gibt es in RDF4J 5 nicht mehr;
		// SPARQLUtil.getDescribeQuery erzeugt exakt dieselbe CONSTRUCT-Query.
		ParsedQuery query = SPARQLUtil.getDescribeQuery(Collections.singletonList(resource).iterator());
		Model template = SPARQLUtil.queryRepository(repo, query);
```

Der Rest der Methode bleibt unverändert. Import `java.util.Collections` ergänzen; `ParsedQuery` ist bereits importiert.

- [ ] **Step 8: Tests und Build verifizieren**

```bash
mvn install
```

Expected: `BUILD SUCCESS`, 21 Module; `common` weiterhin `Tests run: 6, Failures: 0, Errors: 0` — **dieselben** Tests wie in Step 4, jetzt gegen die neue Implementierung. Kein `grep`-Treffer mehr für `queryrender.builder` oder `SailQueryPreparer`:

```bash
grep -rn --include=*.java -E "queryrender\.builder|SailQueryPreparer" . | grep -v "/target/"
```

Expected: keine Ausgabe.

E2E bleibt im Rot-Fenster (unverändert 10/16 rot); ein Lauf ist hier optional, wenn er läuft, muss die Signatur unverändert sein.

- [ ] **Step 9: Commit**

```bash
git add common executionservice
git commit -m "refactor: replace rdf4j apis removed in 5.x (query builder, SailQueryPreparer, initialize)"
git push
```

**Verifikation:** `mvn install` grün mit 6 neuen, vor **und** nach dem Umbau grünen Charakterisierungstests in `common`; die beiden entfernten APIs kommen im Produktcode nicht mehr vor; öffentliche Signaturen unverändert.

**Geltungsbereich dieser Tests:** Sie decken die Query-*Konstruktion und -Auswertung* gegen ein In-Memory-`SailRepository` ab — also genau den umgebauten Code. Die von Spike A gefundene Bruchstelle war jedoch die Auswertung gegen einen **entfernten** RDF4J-5-Server; dafür sind Gate E (Task 1, `Repositories.graphQuery` gegen einen echten 5er-Server) und die E2E-Suite (Task 6) die Belege. Beide Ebenen zusammen erfüllen Spec-Umfangspunkt 3 der Etappe 3; keine allein.

---

### Task 4: rdfbeans auf RDF4J 5.3.1

**Kontext:** `rdfbeans` ist ein eigenständiges Modul ohne Parent mit eigener Property `rdf4j.version=2.0M1` (V12) — es kompiliert heute gegen eine 10 Jahre alte API und läuft gegen 3.6.3. Der einzige echte Bruch ist `CloseableIteration` (V9). Das Modul bringt 27 Testklassen mit (Ledger: 56/56 grün) — ein starkes Netz für genau diesen Umbau.

**Files:**
- Modify: `rdfbeans/pom.xml:75` (`rdf4j.version`)
- Modify: `rdfbeans/src/main/java/org/cyberborean/rdfbeans/RDFBeanManager.java` (Zeilen 314, 319, 342, 344, 698, 782)
- Modify: `rdfbeans/src/main/java/org/cyberborean/rdfbeans/proxy/RDFBeanDelegator.java:181`
- Modify: `rdfbeans/src/test/java/org/cyberborean/rdfbeans/test/examples/ExampleClassTest.java:111`
- Modify: `rdfbeans/src/test/java/org/cyberborean/rdfbeans/test/foafexample/FOAFExampleTest.java:130`
- Modify: `rdfbeans/src/test/java/org/cyberborean/rdfbeans/test/inversions/InversionsClass1Test.java:284`
- Modify: `rdfbeans/src/test/java/org/cyberborean/rdfbeans/test/inversions/InversionsClass2Test.java:284`
- Modify: `rdfbeans/src/test/java/org/cyberborean/rdfbeans/datatype/ListTest.java:58`, `.../MultipleTypeTest.java:59`, `.../test/RDFBeansTestBase.java:23` (`initialize()` → `init()`)

**Interfaces:**
- Consumes: nichts.
- Produces: `rdfbeans` kompiliert und testet gegen RDF4J 5.3.1. Die öffentliche Signatur `RDFBeanManager.getAll(Class<T>)` ändert sich von `CloseableIteration<T, Exception>` zu `CloseableIteration<T>` — im AJAN-Produktcode gibt es **keinen** Aufrufer (verifiziert: `grep -rn "\.getAll(" --include=*.java` liefert außerhalb von `rdfbeans/` keine Treffer); AJAN nutzt rdfbeans ausschließlich über die Annotationen `@RDFBean`/`@RDF`/`@RDFSubject` und `RDFBeanManager.marshal/unmarshal`.

- [ ] **Step 1: Version umstellen**

`rdfbeans/pom.xml:75`:

```xml
		<rdf4j.version>5.3.1</rdf4j.version>
```

Direkt darunter einen Kommentar setzen:

```xml
		<!-- Eigene Property, weil rdfbeans (gevendort) keinen Parent hat.
		     Muss mit ${org.eclipse.rdf4j.version} im Reaktor-Parent
		     uebereinstimmen; die Zusammenfuehrung beider Quellen ist als
		     Aufraeumarbeit fuer Etappe 4 vorgemerkt. -->
```

- [ ] **Step 2: Kompilieren und die echten Fehler einsammeln**

```bash
mvn -pl rdfbeans install
```

Expected: **Kompilierfehler**. Erwartete Meldungen: `type org.eclipse.rdf4j.common.iteration.CloseableIteration does not take parameters` bzw. `wrong number of type arguments` an den in „Files" genannten Stellen, plus `cannot find symbol: method initialize()` in den drei Testklassen. Die vollständige Fehlerliste im Report festhalten — sie ist die Arbeitsliste für Step 3.

- [ ] **Step 3: `CloseableIteration` auf einen Typparameter umstellen**

Regel: Der zweite Typparameter (die Exception) entfällt ersatzlos; RDF4Js Exceptions (`RepositoryException`, `QueryEvaluationException`, `RDF4JException`) sind `RuntimeException`s, deshalb dürfen die `throws`-Klauseln unverändert bleiben.

`RDFBeanDelegator.java:181`:

```java
		CloseableIteration<Statement> sts;
```

`RDFBeanManager.java:314` (Signatur) und 319 (anonyme Klasse):

```java
	public <T> CloseableIteration<T> getAll(final Class<T> rdfBeanClass)
```

```java
			return new CloseableIteration<T>() {
```

`RDFBeanManager.java:342` und 344:

```java
		final CloseableIteration<Statement> sts = conn.getStatements(null, RDF.TYPE, type, false);
```

```java
		return new CloseableIteration<T>() {
```

`RDFBeanManager.java:698`:

```java
		try (CloseableIteration<Statement> ts =
```

`RDFBeanManager.java:782`:

```java
			CloseableIteration<Statement> statements;
```

**Zusätzlich zu prüfen:** `CloseableIteration<E>` erweitert in RDF4J 5 `java.util.Iterator<E>` (V9). Anonyme Implementierungen müssen deshalb alle Methoden bedienen, die `Iterator` verlangt (`hasNext`, `next`) — `remove()` hat eine Default-Implementierung. Meldet der Compiler eine fehlende Methode, diese entsprechend der bestehenden Iterationslogik ergänzen (typisch: `remove()` mit `throw new UnsupportedOperationException()`), und die Ergänzung im Report benennen.

`RDFBeanManager.java:89` ist ein Javadoc-Beispiel (`repository.initialize();`) — dort auf `repository.init();` korrigieren, damit die Doku nicht auf entfernte API zeigt.

- [ ] **Step 4: Tests auf `init()` umstellen**

In `rdfbeans/src/test/java/org/cyberborean/rdfbeans/datatype/ListTest.java:58`, `.../MultipleTypeTest.java:59` und `.../test/RDFBeansTestBase.java:23` jeweils `repo.initialize();` bzw. `inMem.initialize();` durch `…init();` ersetzen. In den vier Testklassen mit `CloseableIteration<X, Exception>` (`ExampleClassTest.java:111`, `FOAFExampleTest.java:130`, `InversionsClass1Test.java:284`, `InversionsClass2Test.java:284`) den zweiten Typparameter streichen.

- [ ] **Step 5: rdfbeans bauen und Tests laufen lassen**

```bash
mvn -pl rdfbeans install
```

Expected: `BUILD SUCCESS` mit `Tests run: 56, Failures: 0, Errors: 0, Skipped: 0` (Vergleichswert aus Plan 1, Task 5). Weicht die Testanzahl ab, im Report begründen. Schlagen Tests fachlich fehl (nicht nur Kompilierung), ist das ein echter RDF4J-5-Verhaltensunterschied in rdfbeans — dann Ursache benennen und fixen, nicht die Assertion aufweichen.

- [ ] **Step 6: Vollen Reaktor bauen**

```bash
mvn install
```

Expected: `BUILD SUCCESS`, 21 Module. Hinweis für den Implementierenden: In diesem Zwischenstand ist `rdfbeans` gegen 5.3.1 kompiliert, während der übrige Reaktor noch 3.6.3 auf dem Classpath hat. Das **kompiliert** (AJAN nutzt von rdfbeans nur Annotationen und `marshal`/`unmarshal`), ist zur **Laufzeit** aber inkonsistent — genau deshalb liegt dieser Task innerhalb des Rot-Fensters und wird durch Task 5 sofort aufgelöst. Bricht der Reaktor-Build hier wider Erwarten (z.B. weil `behaviour` doch eine rdfbeans-Signatur mit `CloseableIteration` berührt), Task stoppen und melden: dann müssen Task 4 und 5 zu einem Commit zusammengezogen werden.

- [ ] **Step 7: Commit**

```bash
git add rdfbeans
git commit -m "build: migrate vendored rdfbeans to rdf4j 5.3.1 (single-parameter CloseableIteration)"
git push
```

**Verifikation:** `mvn -pl rdfbeans install` grün mit 56/56 Tests; voller `mvn install` grün.

---

### Task 5: Zentraler RDF4J-Flip auf 5.3.1 — Kernmodule

**Kontext:** Jetzt wird die zentrale Property umgelegt (V12). Gleichzeitig fallen `rdf4j-sail-spin` (V10, Entscheidung 1) und die in Task 1 ermittelten Boot-1.3-Versions-Overrides (V5) an. Nach diesem Task sind `rdfbeans`, `common`, `behaviour`, `functions` und `executionservice` auf 5.3.1; das Pluginsystem folgt in Task 6.

**Files:**
- Modify: `pom.xml:161` (`org.eclipse.rdf4j.version`)
- Modify: `triplestore/pom.xml` (`rdf4j.server.version` → Parent-Property)
- Modify: `common/pom.xml` (`rdf4j-sail-spin` entfernen)
- Modify: `common/src/main/java/de/dfki/asr/ajan/knowledge/ExecutionBeliefBase.java`
- Modify: `common/src/main/java/de/dfki/asr/ajan/common/RDF4JTripleStoreManager.java` (SPIN-Zweige)
- Modify: `executionservice/pom.xml` (Versions-Property-Overrides aus Task 1)

**Interfaces:**
- Consumes: die in Task 1, Step 5 ermittelte minimale Override-Liste.
- Produces: `org.eclipse.rdf4j.version=5.3.1` als einzige Quelle der Wahrheit für den Reaktor; `TripleStoreManager.Inferencing` bleibt vierwertig, die SPIN-Zweige werfen `UnsupportedOperationException`.

- [ ] **Step 1: Zentrale Property umlegen**

`pom.xml:161`:

```xml
    <org.eclipse.rdf4j.version>5.3.1</org.eclipse.rdf4j.version>
```

`triplestore/pom.xml`: die Property `rdf4j.server.version` löschen und die beiden `artifactItem`-Versionen auf `${org.eclipse.rdf4j.version}` umstellen; den Kommentar über den Properties entsprechend kürzen (der Hinweis auf die spätere Umstellung entfällt, der Tomcat-9-Grund bleibt).

- [ ] **Step 2: SPIN aus `common/pom.xml` entfernen**

Diesen Block ersatzlos löschen:

```xml
    <dependency>
      <artifactId>rdf4j-sail-spin</artifactId>
      <groupId>org.eclipse.rdf4j</groupId>
    </dependency>
```

- [ ] **Step 3: `ExecutionBeliefBase` umbauen**

Den Import `org.eclipse.rdf4j.sail.spin.SpinSail` löschen und `createRepository` ersetzen:

```java
	private Repository createRepository(final TripleStoreManager.Inferencing useInferencing) {
		switch (useInferencing) {
			case RDFS:
				return new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
			case SPIN:
			case RDFS_SPIN:
				// RDF4J hat das SPIN-Sail mit 4.0 entfernt (rdf4j-sail-spin
				// existiert ab 4.0 nicht mehr auf Maven Central). Beide Zweige
				// waren schon vor der Migration unerreichbar: RDFAgentBuilder
				// und ParameterAgentBuilder setzen Inferencing.NONE hart. Der
				// Enum-Wert bleibt erhalten, damit die Konfigurationsoberflaeche
				// unveraendert bleibt; die Auswahl scheitert jetzt laut.
				throw new UnsupportedOperationException(
						"SPIN inferencing is no longer supported: RDF4J removed rdf4j-sail-spin in 4.0");
			default:
				return new SailRepository(new MemoryStore());
		}
	}
```

Die Variable `SpinSail spinSail;` entfällt. Die Imports `SchemaCachingRDFSInferencer`, `DedupingInferencer` und `MemoryStore` prüfen: `DedupingInferencer` wird nur noch im gelöschten `RDFS_SPIN`-Zweig gebraucht — den Import entfernen, sonst schlägt Checkstyle/PMD auf unbenutzte Imports an.

- [ ] **Step 4: `RDF4JTripleStoreManager` umbauen**

Import `org.eclipse.rdf4j.sail.spin.config.SpinSailConfig` löschen. Die beiden privaten Methoden `createRemoteSPINRepository` und `createRemoteRDFSSPINRepository` (ab Zeile 172) löschen und die zugehörigen `switch`-Zweige (Zeilen 150-154) zusammenlegen:

```java
			case SPIN:
			case RDFS_SPIN:
				// s. ExecutionBeliefBase: rdf4j-sail-spin gibt es ab RDF4J 4.0
				// nicht mehr; beide Zweige waren bereits vorher unerreichbar.
				throw new UnsupportedOperationException(
						"SPIN inferencing is no longer supported: RDF4J removed rdf4j-sail-spin in 4.0");
```

Anschließend prüfen, ob `SchemaCachingRDFSInferencerConfig` und `DedupingInferencerConfig` noch verwendet werden (der `RDFS`-Zweig braucht sie); nicht mehr benutzte Imports entfernen.

- [ ] **Step 5: Boot-1.3-Versions-Overrides in `executionservice/pom.xml` setzen**

Genau die in Task 1, Step 5 als notwendig ermittelten Properties in den `<properties>`-Block von `executionservice/pom.xml` eintragen — **nicht mehr** als nötig. Beispielform (die konkreten Einträge kommen aus dem Gate-E-Ergebnis):

```xml
    <!-- spring-boot-dependencies:1.3.5 verwaltet jackson 2.6.6 / httpclient
         4.5.2 / slf4j 1.7.21 und wuerde die transitiven Versionen von RDF4J
         5.3.1 herunterziehen. Belegt in Gate E (docs/superpowers/specs/
         2026-07-11-spike-results.md); nur die dort als noetig nachgewiesenen
         Overrides sind gesetzt. -->
    <jackson.version>2.21.0</jackson.version>
```

War Gate E ohne Overrides grün, entfällt dieser Step vollständig — dann stattdessen einen Kommentar mit dem Verweis auf Gate E setzen, damit die Frage nicht in Etappe 4 erneut aufgeworfen wird.

- [ ] **Step 6: Kernmodule bauen und Kompilierfehler abarbeiten**

```bash
mvn -pl rdfbeans,common,behaviour,functions,executionservice -am install
```

Expected: `BUILD SUCCESS`. Auftretende Kompilierfehler sind echte 3.6→5-API-Brüche jenseits der in V6/V7/V8 bekannten (die sind in Task 3 erledigt). Jeden Fehler einzeln beheben und im Report mit Datei, Zeile, alter und neuer API dokumentieren — **keine** Sammel-Refaktorierung, keine Verhaltensänderung. Die 6 Charakterisierungstests in `common` müssen grün bleiben; das ist der Kern-Nachweis, dass der Umstieg das Query-Verhalten nicht verändert hat.

Häufig zu erwartende Kandidaten (jeweils erst prüfen, ob sie überhaupt auftreten): `Repository.initialize()` an weiteren Stellen, `RepositoryResult`/`Iterations`-Signaturen, entfernte deprecated Methoden auf `Model`/`Models`.

- [ ] **Step 7: Effektive Versionen belegen**

```bash
mvn -pl executionservice dependency:tree -Dincludes=com.fasterxml.jackson.core:jackson-databind,org.apache.httpcomponents:httpclient,org.slf4j:slf4j-api,org.eclipse.rdf4j:rdf4j-model
```

Expected: `rdf4j-model` auf 5.3.1 sowie die aus Gate E erwarteten Jackson-/httpclient-/slf4j-Versionen. Ausgabe in den Report.

- [ ] **Step 8: Commit**

```bash
git add pom.xml triplestore/pom.xml common executionservice
git commit -m "build: bump rdf4j to 5.3.1 reactor-wide and drop removed spin sail"
git push
```

**Verifikation:** Teil-Reaktor (`rdfbeans`, `common`, `behaviour`, `functions`, `executionservice` + deren Abhängigkeiten) grün, inkl. 56 rdfbeans- und 6 common-Tests; `dependency:tree` belegt 5.3.1 und die Override-Wirkung. Der volle `mvn install` schlägt in diesem Zwischenstand am Pluginsystem fehl — das ist erwartet und wird in Task 6 aufgelöst.

---

### Task 6: Pluginsystem und alle 10 Plugins unter RDF4J 5 — E2E-Gate schließen

**Kontext:** Der Bump aus Task 5 wirkt über den zentralen Parent auf das gesamte Pluginsystem: 9 POMs deklarieren `rdf4j-model`/`rdf4j-repository-api`, 75 Java-Dateien in **allen 10** Plugins importieren `org.eclipse.rdf4j.*` (Verteilung laut Spec/Ledger: MOSIM 21, StandardBTNodes 14, Mapping 11, MQTT 10, ASP 6, STRIPS 6, OPCUA 4, Python 1, RMLMapping 1, ScriptExecutor 1; OPCUA und ScriptExecutor erben die Dependency ohne eigene Deklaration). Dieser Task macht den Reaktor vollständig grün und **schließt das Rot-Fenster**.

**Files:**
- Modify: `pluginsystem/**` — welche Dateien konkret, ergibt der Compiler (Step 1). Erwartete Kandidaten mit RDF4J-Nutzung jenseits von Model/Repository-API: `pluginsystem/plugins/ASPPlugin/.../util/Deserializer.java`, `pluginsystem/plugins/STRIPSPlugin/.../utils/StateLoader.java` (beide nutzen `initialize()`), `pluginsystem/plugins/MappingPlugin/.../utils/MappingUtil.java`, `pluginsystem/plugins/STRIPSPlugin/.../utils/OperatorBuilder.java` (beide rufen `SPARQLUtil`).

**Interfaces:**
- Consumes: `org.eclipse.rdf4j.version=5.3.1` aus Task 5; die in Task 3 unveränderten `SPARQLUtil`-Signaturen.
- Produces: vollständig grüner Reaktor und grüne E2E-Suite ⇒ Etappen-Abnahmekriterium erfüllt.

- [ ] **Step 1: Vollen Reaktor bauen und Fehlerliste erzeugen**

```bash
mvn install
```

Expected: Bricht im Pluginsystem ab. Vollständige Fehlerliste (Modul, Datei, Zeile, Meldung) im Report festhalten, bevor irgendetwas geändert wird.

- [ ] **Step 2: `initialize()`-Aufrufe in den Plugins umstellen**

Bekannte Stellen (V6):

- `pluginsystem/plugins/ASPPlugin/src/main/java/de/dfki/asr/ajan/pluginsystem/aspplugin/util/Deserializer.java:187,189` — **Achtung:** Das sind Aufrufe auf `taskInfo.getExecutionBeliefs()`/`getAgentBeliefs()`, also auf AJANs eigene `AbstractBeliefBase.initialize()` (`common/.../AbstractBeliefBase.java:37`), **nicht** auf RDF4Js `Repository`. Sie bleiben unverändert.
- `pluginsystem/plugins/STRIPSPlugin/src/main/java/de/dfki/asr/ajan/pluginsystem/stripsplugin/utils/StateLoader.java:108` — `repo.initialize()` auf einem RDF4J-`Repository`; auf `repo.init()` umstellen.

Vor jeder Änderung prüfen, welcher Typ vorliegt (`AbstractBeliefBase` vs. `org.eclipse.rdf4j.repository.Repository`) — der Compiler beantwortet das eindeutig.

- [ ] **Step 3: Verbleibende Kompilierfehler abarbeiten**

Jede weitere Fundstelle einzeln beheben, mit demselben Prinzip wie in Task 5, Step 6: minimale API-Anpassung, keine Verhaltensänderung, jede Änderung im Report mit alter und neuer API. Ändert sich in einem Plugin die Semantik (z.B. weil eine Methode nicht 1:1 ersetzbar ist), das explizit als Befund melden statt still umzuinterpretieren.

- [ ] **Step 4: Voller Build**

```bash
mvn install
```

Expected: `BUILD SUCCESS`, 21 Module. Die Plugin-Fat-JARs unter `pluginsystem/deployments/` sind neu gebaut.

- [ ] **Step 5: E2E-Gate — Rot-Fenster schließen**

```bash
mvn -f e2e/pom.xml verify
```

Expected: `BUILD SUCCESS`, **16 Tests, 0 Failures, 0 Errors, 0 Skipped**. Damit ist belegt:
- der neue Triplestore serviert RDF4J 5 unter `/rdf4j` auf Port 8090 (`SystemSmokeIT`),
- der migrierte Client legt Agenten über SPARQL-GraphQuery an (`AgentLifecycleIT` — genau die Spike-A-Bruchstelle),
- Content-Negotiation und Fehlerpfade sind unverändert (`RdfResponseIT`, `ErrorPathIT` — die charakterisierten IST-Werte aus den Global Constraints),
- alle 10 Plugins laden weiterhin (`PluginLoadIT`).

Bleibt etwas rot, ist das ein echter Migrationsdefekt — Ursache benennen und beheben, **nicht** die E2E-Erwartung anpassen (die Suite ist das Sicherheitsnetz und definiert „läuft wie bisher").

- [ ] **Step 6: Commit und CI**

```bash
git add pluginsystem
git commit -m "build: migrate plugin system and all 10 plugins to rdf4j 5.3.1"
git push
gh run list --workflow=ci-branch --limit 1
```

Expected: `ci-branch` grün (Build + E2E) — der erste grüne CI-Lauf seit Task 2.

**Verifikation:** `mvn install` 21/21 grün **und** `mvn -f e2e/pom.xml verify` 16/16 grün **und** CI `ci-branch` grün. Das ist das Abnahmekriterium der Etappe.

---

### Task 7: Abschluss — Doku, Spec-Korrekturen, Ledger

**Kontext:** Etappe 3 ist inhaltlich fertig; jetzt werden die Dokumente auf den Stand gebracht, damit Plan 3 (Etappen 4–6) auf einer widerspruchsfreien Grundlage aufsetzt. Zwei Spec-Aussagen sind durch V3 und V10 überholt.

**Files:**
- Modify: `e2e/pom.xml:30-33` (Kommentar zum RDF4J-Pin)
- Modify: `docs/superpowers/specs/2026-07-11-java-modernization-design.md` (Abschnitte 2.4, 3 `triplestore`, Etappe 3, Etappe 4, Etappe 5, Risiko 2/3)
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Offene Fragen", Konsequenzen)
- Modify: `.superpowers/sdd/progress.md`

**Interfaces:**
- Consumes: alle Ergebnisse der Tasks 1–6.
- Produces: Freigabe-Grundlage für Plan 3.

- [ ] **Step 1: E2E-Pin-Kommentar korrigieren**

In `e2e/pom.xml` den Kommentar oberhalb der beiden RDF4J-Test-Dependencies ersetzen (die Versionen `3.6.3` bleiben, s. Entscheidung 3):

```xml
    <!-- BEWUSST auf RDF4J 3.6.3 belassen, obwohl das Projekt seit Etappe 3
         auf 5.3.1 steht (Auftraggeber-Entscheidung 2026-08-11): die Suite
         testet ausschliesslich von aussen ueber HTTP und soll alle
         Migrationsetappen unveraendert ueberleben. Ein vom Produktcode
         entkoppelter Rio-Parser ist dafuer das robustere Werkzeug; er
         parst die Antworten des Service, nicht dessen Interna.
         Test-scope only: die E2E-Suite hat keine Produktionsabhaengigkeit
         auf RDF4J. -->
```

- [ ] **Step 2: Spec korrigieren**

In `docs/superpowers/specs/2026-07-11-java-modernization-design.md`:

1. **Abschnitt 2, Punkt 4 (Triplestore)** und **Abschnitt 3, Zeile `triplestore`**: „(Jakarta-Linie)" streichen und ersetzen durch die verifizierte Aussage — die RDF4J-5.3.1-WARs sind javax.servlet (`web-app 2.4`, `spring-webmvc 5.3.39`, `jstl 1.2`), deshalb **Embedded Tomcat 9.0.x**; Tomcat 10/11 ist ausgeschlossen. Der Wechsel auf die Jakarta-Linie kann erst erfolgen, wenn RDF4J selbst Jakarta-WARs liefert, und ist **kein** Bestandteil von Etappe 4. Zusätzlich vermerken, dass Tomcat 9 auch unter Java 21 läuft, der Java-21-Bump in Etappe 4 also keinen Container-Wechsel im Triplestore erzwingt.
2. **Abschnitt 3, Zeile `RDF4J`** und **Etappe 3**: Zielversion **5.3.1** konkret nennen (statt „5.x"), mit dem Beleg Java-11-Bytecode.
3. **Etappe 3**: als **abgeschlossen** markieren, analog zur Formatierung von Etappe 2 („— **abgeschlossen**"), mit Verweis auf diesen Plan und die erreichten Nachweise (21/21 Module, 16/16 E2E).
4. **Etappe 3, Punkt 4** (E2E-RDF4J-Pin): die offene Entscheidung durch die getroffene ersetzen (bleibt 3.6.3, Kommentar korrigiert).
5. **Neu in Etappe 3 dokumentieren:** SPIN-Entscheidung (rdf4j-sail-spin ab RDF4J 4.0 nicht mehr verfügbar; Enum bleibt, SPIN-Zweige werfen; Laufzeitverhalten unverändert, weil beide Agent-Builder `Inferencing.NONE` hart setzen).
6. **Etappe 5**: den offenen Punkt ergänzen, dass `.github/workflows/docker-image.yml` (Zeilen 42/173/174) noch auf `triplestore-0.1-war-exec.jar` zeigt und auf `triplestore-0.1.jar` nachzuziehen ist.
7. **Risiko 2** (RDF4J-API-Bruch): auf **eingetreten und abgearbeitet** setzen, mit der konkreten Bruchliste (SailQueryPreparer, queryrender.builder, CloseableIteration-Arität, sail-spin, `initialize()`).
8. **Risiko 3**: als **erledigt** markieren (Etappe 3 grün abgeschlossen, Rückfallebene nicht gezogen — bzw. das tatsächliche Gate-E-Ergebnis).

- [ ] **Step 3: Offene Fragen der Spike-Ergebnisse schließen**

In `docs/superpowers/specs/2026-07-11-spike-results.md`, Abschnitt „Offene Fragen":

- **Frage 4** (RDF4J 5.x im Boot-1.3-/Java-11-Kontext): als **beantwortet durch Gate E** markieren, mit Ergebnis und den gesetzten Overrides. Der historische Text bleibt stehen.
- **Frage 5** (Tomcat-Version des Launcher-Moduls): als **beantwortet** markieren — Tomcat 9.0.109, weil die RDF4J-5-WARs javax.servlet sind (V3, V4); Nachzug in Etappe 4 ist **nicht** nötig, Tomcat 9 läuft auch unter Java 21.
- Die Fragen 1, 2, 3 und 6 bleiben unverändert offen (Plan 3).

- [ ] **Step 4: Ledger fortschreiben**

In `.superpowers/sdd/progress.md` einen Abschnitt `=== ETAPPE 3 (Triplestore-Neubau + RDF4J-5-Client) ===` anfügen, im Stil der bestehenden Einträge, mit: Gate-E-Ergebnis, Rot-Fenster-Verlauf (geöffnet in Task 2, geschlossen in Task 6) samt beobachteter Signatur, den harten API-Brüchen mit Fundstellen, der SPIN-Entscheidung, der offenen docker-image.yml-Lücke für Etappe 5 sowie den Verifikationszahlen (`mvn install` 21/21, e2e 16/16, CI-Lauf-ID).

- [ ] **Step 5: Abschlussverifikation**

```bash
mvn install
mvn -f e2e/pom.xml verify
```

Expected: beide `BUILD SUCCESS` (21 Module, 16/16 Tests) — die Doku-Änderungen dürfen daran nichts ändern; die Ausnahme ist `e2e/pom.xml`, weshalb der E2E-Lauf hier trotz reiner Kommentaränderung wiederholt wird.

- [ ] **Step 6: Commit**

```bash
git add e2e/pom.xml docs/superpowers/specs .superpowers/sdd/progress.md
git commit -m "docs: close etappe 3 (tomcat 9 launcher, rdf4j 5.3.1, spin removal, e2e pin decision)"
git push
```

**Verifikation:** `mvn install` und `mvn -f e2e/pom.xml verify` grün; Spec und Spike-Ergebnisse enthalten keine überholten Aussagen mehr (insbesondere kein „Jakarta-Linie" beim Triplestore und keine offenen Fragen 4/5); der Ledger beschreibt Etappe 3 vollständig.

---

## Nach diesem Plan

- **Whole-Branch-Review Etappe 3** über den gesamten Diff dieses Plans (Muster: Etappen 0–2). Prüfschwerpunkte: (a) trägt der Launcher wirklich dieselbe Oberfläche wie das alte war-exec-JAR (Ports, beide Kontextpfade, beide CLI-Schreibweisen, JNDI/`enableNaming`), (b) sind die 6 Charakterisierungstests aussagekräftig genug für den Query-Umbau oder nageln sie nur Triviales fest, (c) ist die SPIN-Entscheidung wirklich verhaltensneutral, (d) hat der Plugin-Umbau irgendwo Semantik verschoben, (e) ist die docker-image.yml-Lücke sauber dokumentiert.
- **Plan 3** (Etappen 4–6: Kern-Sprung, Deployment/Feinschliff, Java 25) wird erst danach geschrieben. Offene Fragen 1, 2, 3 und 6 der Spike-Ergebnisse gehören dort hinein — insbesondere der von Etappe 2 geforderte explizite Umschaltpunkt auf den MVC-Plan-B.
- **Nicht in Etappe 3 erledigt und bewusst offen:** `.github/workflows/docker-image.yml` zeigt weiter auf `triplestore-0.1-war-exec.jar` (Etappe 5); die doppelte RDF4J-Versionsquelle (`pom.xml` vs. `rdfbeans/pom.xml`) ist zusammenzuführen (Etappe 4); `e2e/pom.xml` bleibt bewusst auf RDF4J 3.6.3.
