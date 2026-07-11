# Modernisierung Plan 1: Sicherheitsnetz, Build-Hygiene, Spikes (Etappen 0–2)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** E2E-Smoke-Suite als Sicherheitsnetz aufbauen, das Build-Setup entrümpeln (rdfbeans vendoren, tote Maven-Plugins raus, einheitlich Java 11) und mit vier Spikes die offenen Entscheidungen für die Etappen 3–6 klären.

**Architecture:** Die E2E-Suite ist ein **eigenständiges Maven-Projekt** unter `e2e/` (bewusst NICHT im Parent-Reaktor), das die gebauten JARs als externe Prozesse startet und über HTTP testet — dadurch überlebt sie alle Migrationsetappen unverändert. Build-Hygiene passiert im bestehenden Reaktor ohne Framework-Sprünge. Spikes leben unter `spikes/` (ebenfalls außerhalb des Reaktors) und produzieren nur ein Ergebnis-Dokument.

**Tech Stack:** Java 11, Maven, JUnit 5 + maven-failsafe (nur `e2e/`), `java.net.http.HttpClient`, Docker (nur Spike A).

**Spec:** `docs/superpowers/specs/2026-07-11-java-modernization-design.md`

## Global Constraints

- Alle Arbeit auf Branch `modernization/java21`; `master` wird nicht angefasst, nichts wird nach `master` gemerged oder dorthin gepusht.
- Der bestehende Workflow `.github/workflows/docker-image.yml` wird in diesem Plan NICHT verändert (triggert nur auf `master`; kein Publish-Risiko).
- Kein `docker push`, keine Tags `aantakli/ajan-service*`; lokale Docker-Tests nur mit lokalen Tags.
- Öffentliche HTTP-API bleibt unverändert (in diesem Plan wird ohnehin kein API-Code angefasst).
- Ports sind fix: Triplestore 8090, executionservice 8080 (die Use-Case-Daten referenzieren `http://localhost:8090/...` hart).
- In diesem Plan gilt Java-Target **11** (Java 21 kommt erst im Kern-Sprung, Plan 3).
- Riskante Bausteine (Spring Boot, RDF4J-Major-Version, pf4j) werden in diesem Plan NICHT angehoben — nur per Spike untersucht.
- Nach jedem Task: `mvn install` grün; ab Task 2 zusätzlich E2E grün (`mvn -f e2e/pom.xml verify`).
- Build-Kommandos laufen im Repo-Root `AJAN-service/` unter JDK 11 (`java -version` → 11.x), außer wo explizit anders angegeben (Spike D: JDK 21).

---

### Task 1: E2E-Grundgerüst — Prozess-Orchestrierung + Triplestore-Smoke-Test

**Files:**
- Create: `e2e/pom.xml`
- Create: `e2e/src/test/java/de/dfki/asr/ajan/e2e/AjanSystem.java`
- Create: `e2e/src/test/java/de/dfki/asr/ajan/e2e/Http.java`
- Test: `e2e/src/test/java/de/dfki/asr/ajan/e2e/SystemSmokeIT.java`

**Interfaces:**
- Consumes: gebaute Artefakte `triplestore/target/triplestore-0.1-war-exec.jar` und `executionservice/target/executionservice-0.1.jar` (aus `mvn install` im Root).
- Produces (für Task 2/3 und Spike A):
  - `AjanSystem implements AutoCloseable` mit `static AjanSystem start()`, `String triplestoreUrl()` (z.B. `http://localhost:8090/rdf4j`), `String serviceBase()` (z.B. `http://localhost:8080/ajan`), `java.nio.file.Path serviceLog()`, `void close()`.
  - System-Property `ajan.e2e.triplestoreUrl`: wenn gesetzt, startet `AjanSystem` KEINEN eigenen Triplestore, sondern nutzt die gegebene URL (Hook für Spike A).
  - System-Property `ajan.root`: Pfad zum Repo-Root (Default: `..` relativ zu `e2e/`).
  - `Http.send(String method, String url, String contentType, String body)` → `java.net.http.HttpResponse<String>`.

- [ ] **Step 1: Projekt bauen, damit die JARs existieren**

Run: `mvn install -q` (im Repo-Root)
Expected: `BUILD SUCCESS`; danach existieren `triplestore/target/triplestore-0.1-war-exec.jar` und `executionservice/target/executionservice-0.1.jar`.

- [ ] **Step 2: `e2e/pom.xml` anlegen**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.dfki.asr.ajan</groupId>
  <artifactId>ajan-e2e</artifactId>
  <version>0.1</version>
  <packaging>jar</packaging>
  <name>AJAN E2E Smoke Suite</name>
  <!-- Bewusst KEIN Modul des ajan-parent-Reaktors: die Suite muss alle
       Migrationsetappen unveraendert ueberleben und testet nur von aussen. -->

  <properties>
    <maven.compiler.release>11</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <version>3.2.5</version>
        <configuration>
          <systemPropertyVariables>
            <ajan.root>${project.basedir}/..</ajan.root>
          </systemPropertyVariables>
          <!-- ITs starten echte Prozesse auf festen Ports: strikt sequenziell -->
          <forkCount>1</forkCount>
          <reuseForks>true</reuseForks>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>integration-test</goal>
              <goal>verify</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 3: `Http.java` schreiben**

```java
package de.dfki.asr.ajan.e2e;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class Http {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Http() {}

    public static HttpResponse<String> send(String method, String url, String contentType, String body)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30));
        HttpRequest.BodyPublisher pub = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        if (contentType != null) {
            b.header("Content-Type", contentType);
        }
        return CLIENT.send(b.method(method, pub).build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Pollt bis 200 oder Timeout; wirft AssertionError mit letzter Antwort. */
    public static void awaitOk(String url, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        String last = "no response";
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<String> r = send("GET", url, null, null);
                if (r.statusCode() == 200) {
                    return;
                }
                last = r.statusCode() + ": " + r.body();
            } catch (java.io.IOException e) {
                last = e.toString();
            }
            Thread.sleep(2000);
        }
        throw new AssertionError("Timeout waiting for 200 from " + url + "; last: " + last);
    }
}
```

- [ ] **Step 4: `AjanSystem.java` schreiben**

```java
package de.dfki.asr.ajan.e2e;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Startet Triplestore + executionservice aus den gebauten JARs als externe
 * Prozesse (analog startAll-Skripte / supervisord im Docker-Image) und
 * raeumt sie wieder ab. Charakterisiert den IST-Zustand — bei Migrations-
 * etappen darf sich am Verhalten dieser Klasse nichts aendern muessen.
 */
public final class AjanSystem implements AutoCloseable {

    private static final int TRIPLESTORE_PORT = 8090;
    private static final int SERVICE_PORT = 8080;

    private final Process triplestore; // null, wenn extern (Spike A)
    private final Process service;
    private final Path serviceLog;

    private AjanSystem(Process triplestore, Process service, Path serviceLog) {
        this.triplestore = triplestore;
        this.service = service;
        this.serviceLog = serviceLog;
    }

    public static AjanSystem start() throws IOException, InterruptedException {
        Path root = Paths.get(System.getProperty("ajan.root", "..")).toAbsolutePath().normalize();
        Path logDir = root.resolve("e2e/target/logs");
        Files.createDirectories(logDir);
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

        String externalTriplestore = System.getProperty("ajan.e2e.triplestoreUrl");
        Process ts = null;
        String triplestoreUrl;
        if (externalTriplestore != null && !externalTriplestore.isEmpty()) {
            triplestoreUrl = externalTriplestore;
        } else {
            triplestoreUrl = "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
            failIfPortInUse(TRIPLESTORE_PORT);
            // Isoliertes RDF4J-Datenverzeichnis: kalter Start wie im Docker-Image
            Path rdf4jData = root.resolve("e2e/target/rdf4j-data");
            deleteRecursively(rdf4jData);
            List<String> cmd = new ArrayList<>(List.of(java,
                    "-Dorg.eclipse.rdf4j.appdata.basedir=" + rdf4jData,
                    "-jar", root.resolve("triplestore/target/triplestore-0.1-war-exec.jar").toString(),
                    "--httpPort=" + TRIPLESTORE_PORT));
            ts = new ProcessBuilder(cmd)
                    .directory(root.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(logDir.resolve("triplestore.log").toFile())
                    .start();
            Http.awaitOk(triplestoreUrl + "/repositories", Duration.ofSeconds(120));
        }

        failIfPortInUse(SERVICE_PORT);
        Path serviceLog = logDir.resolve("service.log");
        List<String> cmd = new ArrayList<>(List.of(java,
                "-Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents",
                "-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains",
                "-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services",
                "-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors",
                "-Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/editorData",
                "-Dpf4j.mode=development",
                "-Dpf4j.pluginsDir=pluginsystem/plugins",
                "-Dserver.port=" + SERVICE_PORT,
                "-DloadTTLFiles=true",
                "-Dtriplestore.url=" + triplestoreUrl,
                "-jar", root.resolve("executionservice/target/executionservice-0.1.jar").toString()));
        Process svc = new ProcessBuilder(cmd)
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(serviceLog.toFile())
                .start();
        try {
            Http.awaitOk("http://localhost:" + SERVICE_PORT + "/ajan/agents", Duration.ofSeconds(180));
        } catch (AssertionError e) {
            destroy(svc);
            destroy(ts);
            throw new AssertionError(e.getMessage() + "\n--- service.log tail ---\n" + tail(serviceLog, 60), e);
        }
        return new AjanSystem(ts, svc, serviceLog);
    }

    public String triplestoreUrl() {
        String ext = System.getProperty("ajan.e2e.triplestoreUrl");
        return (ext != null && !ext.isEmpty()) ? ext : "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
    }

    public String serviceBase() {
        return "http://localhost:" + SERVICE_PORT + "/ajan";
    }

    public Path serviceLog() {
        return serviceLog;
    }

    @Override
    public void close() {
        destroy(service);
        destroy(triplestore);
    }

    private static void destroy(Process p) {
        if (p == null) {
            return;
        }
        p.destroy();
        try {
            if (!p.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)) {
                p.destroyForcibly().waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            p.destroyForcibly();
        }
    }

    private static void failIfPortInUse(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            throw new IllegalStateException("Port " + port + " ist bereits belegt — laufende AJAN/Triplestore-Instanz stoppen.");
        } catch (IOException expected) {
            // Port frei
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private static String tail(Path file, int lines) {
        try {
            List<String> all = Files.readAllLines(file);
            return String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
        } catch (IOException e) {
            return "(log not readable: " + e + ")";
        }
    }
}
```

- [ ] **Step 5: Failing Test `SystemSmokeIT.java` schreiben**

```java
package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSmokeIT {

    private static AjanSystem system;

    @BeforeAll
    static void startSystem() throws Exception {
        system = AjanSystem.start();
    }

    @AfterAll
    static void stopSystem() {
        if (system != null) {
            system.close();
        }
    }

    @Test
    void triplestoreListsRepositoriesIncludingAgents() throws Exception {
        var r = Http.send("GET", system.triplestoreUrl() + "/repositories", null, null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("agents"),
                "repositories listing should contain 'agents' repo, was:\n" + r.body());
    }

    @Test
    void serviceListsAgentsEndpoint() throws Exception {
        var r = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, r.statusCode());
    }
}
```

- [ ] **Step 6: Suite laufen lassen und kalibrieren**

Run: `mvn -f e2e/pom.xml verify`
Expected: `BUILD SUCCESS`, 2 Tests grün. Das ist ein Charakterisierungstest des IST-Zustands: Falls eine Assertion am realen Verhalten scheitert (z.B. Repo-Listing-Format), die Assertion an das beobachtete IST-Verhalten anpassen (NICHT den Service) und die Abweichung im Commit-Text dokumentieren. Erst weiter, wenn grün.

- [ ] **Step 7: `.gitignore` um E2E-Artefakte ergänzen und committen**

Prüfen, ob `target/` bereits global ignoriert wird (`git check-ignore e2e/target` nach einem Lauf). Falls nein, `e2e/target/` in `.gitignore` ergänzen.

```bash
git add e2e/ .gitignore
git commit -m "test: add standalone E2E smoke suite (process orchestration + triplestore/service smoke)"
```

---

### Task 2: E2E-Agent-Lifecycle-Test (HelloWorld-Agent)

**Files:**
- Test: `e2e/src/test/java/de/dfki/asr/ajan/e2e/AgentLifecycleIT.java`

**Interfaces:**
- Consumes: `AjanSystem`, `Http` aus Task 1; Use-Case-Daten: AgentTemplate `http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6` mit Endpoint-Capability `HelloWorld_BT` (aus `executionservice/use-case/agents/agents.trig`).
- Produces: nichts (reiner Test).

- [ ] **Step 1: Failing Test schreiben**

```java
package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentLifecycleIT {

    private static final String AGENT_ID = "E2EAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";

    private static AjanSystem system;

    @BeforeAll
    static void startSystem() throws Exception {
        system = AjanSystem.start();
    }

    @AfterAll
    static void stopSystem() {
        if (system != null) {
            system.close();
        }
    }

    @Test
    @Order(1)
    void createAgentFromHelloWorldTemplate() throws Exception {
        String turtle = "@prefix ajan: <http://www.ajan.de/ajan-ns#> .\n"
                + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
                + "_:init rdf:type ajan:AgentInitialisation ;\n"
                + "  ajan:agentId \"" + AGENT_ID + "\" ;\n"
                + "  ajan:agentTemplate <" + TEMPLATE + "> .\n";
        var r = Http.send("POST", system.serviceBase() + "/agents/", "text/turtle", turtle);
        assertTrue(r.statusCode() >= 200 && r.statusCode() < 300,
                "agent creation should succeed, was " + r.statusCode() + ": " + r.body());
    }

    @Test
    @Order(2)
    void agentAppearsInListingAndDetails() throws Exception {
        var list = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, list.statusCode());
        assertTrue(list.body().contains(AGENT_ID), "agent listing should contain " + AGENT_ID);

        var details = Http.send("GET", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, details.statusCode());
    }

    @Test
    @Order(3)
    void agentKnowledgeVisibleInTriplestore() throws Exception {
        // Spec: "Ergebnis-RDF im Triplestore pruefen". Charakterisierung:
        // Nach der Agent-Anlage muss der Agent im Triplestore sichtbar sein.
        // Kalibrierung beim ersten Lauf: Legt der Service pro Agent ein eigenes
        // Repository an, greift die Assertion unten; landet das Agenten-RDF
        // stattdessen in einem Sammel-Repo (service.log/Repo-Listing pruefen),
        // Assertion durch eine SPARQL-Anfrage auf dieses Repo ersetzen, die
        // Triples mit "E2EAgent" findet.
        var repos = Http.send("GET", system.triplestoreUrl() + "/repositories", null, null);
        assertEquals(200, repos.statusCode());
        assertTrue(repos.body().contains(AGENT_ID),
                "triplestore should hold RDF for created agent, repositories were:\n" + repos.body());
    }

    @Test
    @Order(4)
    void sendMessageToHelloWorldCapability() throws Exception {
        String body = "<http://ajan.e2e/ping> <http://ajan.e2e/says> \"ping\" .\n";
        var r = Http.send("POST",
                system.serviceBase() + "/agents/" + AGENT_ID + "?capability=HelloWorld_BT",
                "text/turtle", body);
        assertTrue(r.statusCode() >= 200 && r.statusCode() < 300,
                "sending message should succeed, was " + r.statusCode() + ": " + r.body());
    }

    @Test
    @Order(5)
    void deleteAgentAndVerifyGone() throws Exception {
        var del = Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertTrue(del.statusCode() >= 200 && del.statusCode() < 300,
                "delete should succeed, was " + del.statusCode() + ": " + del.body());

        var list = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, list.statusCode());
        assertFalse(list.body().contains(AGENT_ID), "agent should be gone after delete");
    }
}
```

- [ ] **Step 2: Laufen lassen und kalibrieren**

Run: `mvn -f e2e/pom.xml verify`
Expected: `BUILD SUCCESS`. Wie in Task 1 Step 6: weicht das IST-Verhalten ab (z.B. konkreter Status-Code, Agent-URI statt -Id im Listing), Assertions an das beobachtete Verhalten schärfen (Charakterisierung!) und dokumentieren. Statuscode-Toleranzen (`2xx`) danach durch die beobachteten exakten Codes ersetzen.

- [ ] **Step 3: Commit**

```bash
git add e2e/src/test/java/de/dfki/asr/ajan/e2e/AgentLifecycleIT.java
git commit -m "test: add E2E agent lifecycle test (create/list/message/delete HelloWorld agent)"
```

---

### Task 3: E2E-Plugin-Ladecheck

**Files:**
- Test: `e2e/src/test/java/de/dfki/asr/ajan/e2e/PluginLoadIT.java`

**Interfaces:**
- Consumes: `AjanSystem.serviceLog()` aus Task 1; Plugin-IDs aus den `plugin.properties` der 10 Plugins.
- Produces: nichts (reiner Test).

- [ ] **Step 1: Failing Test schreiben**

Die 10 Plugin-IDs (verifiziert aus `pluginsystem/plugins/*/plugin.properties`): `ASPPlugin`, `MappingPlugin`, `MOSIMPlugin`, `MQTTPlugin`, `OPCUAPlugin`, `PythonPlugin`, `RMLMappingPlugin`, `ScriptExecutorPlugin`, `STRIPSPlugin`, `StandardBTNodes`.

```java
package de.dfki.asr.ajan.e2e;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginLoadIT {

    // Entwicklungsmodus (pf4j.mode=development): alle 10 muessen laden.
    // Achtung: das CI-dist/Docker-Image enthaelt heute nur 8 (ohne OPCUA,
    // ScriptExecutor) — dieser Test laeuft bewusst gegen den Dev-Modus.
    private static final List<String> PLUGIN_IDS = List.of(
            "ASPPlugin", "MappingPlugin", "MOSIMPlugin", "MQTTPlugin", "OPCUAPlugin",
            "PythonPlugin", "RMLMappingPlugin", "ScriptExecutorPlugin", "STRIPSPlugin",
            "StandardBTNodes");

    private static AjanSystem system;

    @BeforeAll
    static void startSystem() throws Exception {
        system = AjanSystem.start();
    }

    @AfterAll
    static void stopSystem() {
        if (system != null) {
            system.close();
        }
    }

    @Test
    void allTenPluginsAppearInStartupLog() throws Exception {
        String log = Files.readString(system.serviceLog());
        for (String id : PLUGIN_IDS) {
            assertTrue(log.contains(id),
                    "service startup log should mention plugin '" + id + "'");
        }
    }
}
```

- [ ] **Step 2: Laufen lassen und kalibrieren**

Run: `mvn -f e2e/pom.xml verify`
Expected: `BUILD SUCCESS`. Falls ein Plugin im Log unter anderem Namen auftaucht, das Log (`e2e/target/logs/service.log`) inspizieren und die Assertion auf die tatsächliche pf4j-Logzeile schärfen (z.B. `Plugin 'XYZPlugin@...' resolved`). Lädt ein Plugin im IST-Zustand tatsächlich nicht, wird es NICHT stillschweigend entfernt, sondern als bekannter Defekt in `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Vorbefunde") notiert und die Assertion mit Verweis darauf ausgenommen.

- [ ] **Step 3: Commit**

```bash
git add e2e/src/test/java/de/dfki/asr/ajan/e2e/PluginLoadIT.java
git commit -m "test: add E2E plugin load check for all 10 plugins (dev mode)"
```

---

### Task 4: Branch-CI-Workflow ohne Publish

**Files:**
- Create: `.github/workflows/ci-branch.yml`

**Interfaces:**
- Consumes: E2E-Suite aus Task 1–3.
- Produces: CI-Gate für alle Folge-Tasks (jeder Push auf `modernization/**` baut + testet; kein Docker-Login, kein Push, kein Release).

- [ ] **Step 1: Workflow-Datei schreiben**

```yaml
name: ci-branch

on:
  push:
    branches:
      - 'modernization/**'

jobs:
  build-and-e2e:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          submodules: recursive
      - name: Cache local Maven repository
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-
      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '11'
      - name: Set up Maven
        uses: stCarolas/setup-maven@v4.3
        with:
          maven-version: 3.9.9
      - name: Build project
        run: mvn install
      - name: Run E2E smoke suite
        run: mvn -f e2e/pom.xml verify
      - name: Upload E2E logs on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: e2e-logs
          path: e2e/target/logs/
```

Hinweis: bewusst KEINE `secrets`, kein `docker/login-action`, kein Push, kein Release-Job. Der `submodules: recursive`-Checkout wird nach Task 5 funktionslos, schadet aber nicht und wird dort mit entfernt.

- [ ] **Step 2: Committen und Branch erstmalig pushen**

```bash
git add .github/workflows/ci-branch.yml
git commit -m "ci: add build+E2E workflow for modernization branches (no publish)"
git push -u origin modernization/java21
```

Expected: Push ist gefahrlos — der Publish-Workflow triggert nur auf `master`.

- [ ] **Step 3: CI-Lauf verifizieren**

Run: `gh run watch` (oder `gh run list --workflow=ci-branch --limit 1` bis Status `completed`)
Expected: Workflow `ci-branch` grün (Build + E2E). Erst weiter, wenn grün.

---

### Task 5: rdfbeans vendoren (Submodul auflösen)

**Files:**
- Delete: `.gitmodules`
- Modify: `rdfbeans/` (vom Gitlink zum normalen Verzeichnis)
- Modify: `.github/workflows/ci-branch.yml` (submodules-Checkout raus)
- Modify: `README.md` (Zeile `git submodule update --init --recursive` entfernen)

**Interfaces:**
- Consumes: nichts.
- Produces: `rdfbeans` als normales Reaktor-Modul; ab jetzt darf jeder Folge-Task rdfbeans-Quellcode direkt ändern (nötig für RDF4J 5 im Kern-Sprung).

- [ ] **Step 1: Sicherstellen, dass das Submodul initialisiert ist**

Run: `git submodule status` und `ls rdfbeans/pom.xml`
Expected: Commit-Hash ohne `-`-Präfix, `pom.xml` vorhanden. Falls nicht: `git submodule update --init --recursive`.

- [ ] **Step 2: Quellen sichern, Submodul entfernen, Quellen als normale Dateien einchecken**

```bash
cp -r rdfbeans ../rdfbeans-vendor-tmp
git submodule deinit -f rdfbeans
git rm -f rdfbeans
git rm -f .gitmodules
rm -rf .git/modules/rdfbeans
mv ../rdfbeans-vendor-tmp rdfbeans
rm -rf rdfbeans/.git
git add rdfbeans
```

Expected: `git status` zeigt `rdfbeans/**` als neue Dateien (kein Gitlink mehr), `.gitmodules` gelöscht. Die LGPL-Lizenzheader in den rdfbeans-Quellen bleiben unangetastet.

- [ ] **Step 3: CI-Workflow und README anpassen**

In `.github/workflows/ci-branch.yml` die zwei Zeilen `with: submodules: recursive` unter dem Checkout-Step entfernen. In `README.md` die Zeile ``* cmd: `git submodule update --init --recursive` `` entfernen. Der `master`-Workflow (`docker-image.yml`) wird NICHT angefasst (er nutzt kein `submodules:`-Flag).

- [ ] **Step 4: Build + E2E verifizieren**

Run: `mvn install -q && mvn -f e2e/pom.xml verify`
Expected: beide `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "build: vendor rdfbeans into repo (dissolve git submodule)"
git push
```

Expected: CI `ci-branch` grün.

---

### Task 6: Parent-POM: tote Build-Plugins raus, jar-Plugin aktuell, Surefire pinnen

**Files:**
- Modify: `pom.xml` (Parent, `build/pluginManagement/plugins`)
- Modify: `executionservice/pom.xml:81-89` (Surefire-Version)

**Interfaces:**
- Consumes: nichts.
- Produces: Surefire 3.5.2 überall (Voraussetzung für TestNG 7 in Task 8).

- [ ] **Step 1: findbugs-Plugin entfernen**

In `pom.xml` den kompletten Block des `findbugs-maven-plugin` (im `pluginManagement`, beginnt mit `<artifactId>findbugs-maven-plugin</artifactId>`) ersatzlos löschen. FindBugs ist seit 2017 tot; es ist nirgends an den Lifecycle eines Moduls gebunden außer über dieses Management.

- [ ] **Step 2: maven-javadoc-plugin mit Pegdown-Doclet entfernen**

In `pom.xml` den kompletten Block des `maven-javadoc-plugin` (mit `pegdown-doclet` 1.2.1) ersatzlos löschen. Das Doclet ist mit modernen JDKs inkompatibel; Javadoc-Generierung ist an keinen Lifecycle gebunden.

- [ ] **Step 3: maven-jar-plugin 2.6 → 3.4.2 und Surefire-Pin ergänzen**

Im `pluginManagement` von `pom.xml`: beim `maven-jar-plugin` `<version>2.6</version>` → `<version>3.4.2</version>`. Danach folgenden neuen Block in `pluginManagement/plugins` einfügen:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <groupId>org.apache.maven.plugins</groupId>
  <version>3.5.2</version>
</plugin>
```

In `executionservice/pom.xml` im vorhandenen `maven-surefire-plugin`-Block (der mit dem `BeliefUpdateStallTest`-Exclude) `<version>3.5.2</version>` ergänzen (das Modul erbt vom Spring-Boot-1.3-Parent, nicht vom ajan-parent — deshalb explizit).

- [ ] **Step 4: Build + E2E verifizieren**

Run: `mvn install -q && mvn -f e2e/pom.xml verify`
Expected: beide `BUILD SUCCESS`. Bekanntes Risiko: Surefire 3.x + TestNG 6.9.10 — falls Tests nicht mehr gefunden/gestartet werden, Fehlermeldung notieren und Task 8 (TestNG-Bump) VORZIEHEN, dann hierher zurückkehren.

- [ ] **Step 5: Commit**

```bash
git add pom.xml executionservice/pom.xml
git commit -m "build: drop dead findbugs/pegdown-javadoc plugins, bump jar plugin, pin surefire 3.5.2"
git push
```

---

### Task 7: Java-Target einheitlich auf 11

**Files:**
- Modify: `pom.xml` (Properties + Compiler-Plugin-Config)
- Modify: `executionservice/pom.xml:90-97,238-243`
- Modify: weitere POMs mit eigenen `source`/`target`-Overrides (per Grep ermittelt)

**Interfaces:**
- Consumes: nichts.
- Produces: `maven.compiler.release=11` als einzige Quelle der Wahrheit im Parent; Voraussetzung für TestNG 7.10 (Task 8).

- [ ] **Step 1: Alle Compiler-Konfigurationen finden**

Run: `grep -rn --include=pom.xml -e "maven.compiler" -e "<source>" -e "<target>" -e "<release>" .`
Expected (mindestens): `pom.xml` (Properties 1.8 + Compiler-Plugin `${maven.compiler.source}`), `executionservice/pom.xml` (Properties 1.8, Compiler-Plugin 11), `rdfbeans/pom.xml` (eigene Compiler-Config des gevendorten Moduls). Jeden weiteren Treffer in die folgenden Steps einbeziehen.

- [ ] **Step 2: Parent-POM umstellen**

In `pom.xml` die Properties

```xml
<maven.compiler.source>1.8</maven.compiler.source>
<maven.compiler.target>1.8</maven.compiler.target>
```

ersetzen durch

```xml
<maven.compiler.release>11</maven.compiler.release>
```

und in der `maven-compiler-plugin`-Konfiguration die Zeilen `<source>${maven.compiler.source}</source>` und `<target>${maven.compiler.target}</target>` ersetzen durch `<release>${maven.compiler.release}</release>`.

- [ ] **Step 3: executionservice umstellen**

In `executionservice/pom.xml`: im `maven-compiler-plugin`-Block `<source>11</source><target>11</target>` → `<release>11</release>`; in den Properties `maven.compiler.source`/`maven.compiler.target` (1.8) ersatzlos löschen (irreführend, das Plugin-Override gewinnt ohnehin).

- [ ] **Step 4: Restliche Treffer aus Step 1 angleichen**

Jedes Modul mit eigenem Override: Override löschen, wenn es vom ajan-parent erbt (erbt dann release=11); sonst (z.B. `rdfbeans/pom.xml`, falls eigener Parent) explizit auf `<release>11</release>` setzen.

- [ ] **Step 5: Build + E2E verifizieren**

Run: `mvn install -q && mvn -f e2e/pom.xml verify`
Expected: beide `BUILD SUCCESS`. (CI baut heute schon mit JDK 11 bei target 8 — Klassenpfad ändert sich nicht, nur das Bytecode-Level.)

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "build: unify java target to release 11 across all modules"
git push
```

---

### Task 8: Harmlose Dependency-Bumps (Test-Stack) + jcommander raus

**Files:**
- Modify: `pom.xml` (dependencyManagement: testng, hamcrest-core, jcommander)

**Interfaces:**
- Consumes: Surefire 3.5.2 (Task 6), Java 11 (Task 7).
- Produces: TestNG 7.10.2, Hamcrest-core 2.2 für alle Module.

- [ ] **Step 1: jcommander-Verwendung verifizieren und entfernen**

Run: `grep -rn --include=pom.xml jcommander .` und `grep -rn --include=*.java com.beust .`
Expected: einziger Treffer ist das `dependencyManagement` im Parent (Java-seitig bereits verifiziert: keine Imports). Dann in `pom.xml` den `jcommander`-Block aus `dependencyManagement` löschen. Gibt es doch Modul-Treffer: Block behalten, nur Version auf `1.82` heben.

- [ ] **Step 2: TestNG und Hamcrest bumpen**

In `pom.xml` `dependencyManagement`: `testng` `6.9.10` → `7.10.2`; `hamcrest-core` `1.3` → `2.2` (Artefakt-Id bewusst beibehalten — 2.2 ist ein Delegations-Shim auf `org.hamcrest:hamcrest`).

- [ ] **Step 3: Build + Tests + E2E verifizieren**

Run: `mvn install -q` — dabei prüfen, dass die TestNG-Tests in `behaviour` tatsächlich LAUFEN (Surefire-Ausgabe `Tests run: N` mit N > 0 im behaviour-Modul), nicht nur übersprungen werden. Dann `mvn -f e2e/pom.xml verify`.
Expected: `BUILD SUCCESS` beidseitig; Testanzahl unverändert zu vorher (Vergleich mit Task-7-Lauf). Kompilierfehler in Testcode durch TestNG-7-API-Änderungen (z.B. entfernte `org.testng.Assert`-Overloads) direkt im Testcode fixen — Produktivcode bleibt unangetastet.

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "build: bump testng to 7.10.2 and hamcrest-core to 2.2, drop unused jcommander"
git push
```

---

### Task 9: Spike A — RDF4J-3.6-Client gegen RDF4J-5-Server

**Files:**
- Create: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Spike A")

**Interfaces:**
- Consumes: E2E-Suite (Task 1–3) mit dem `ajan.e2e.triplestoreUrl`-Hook; Docker.
- Produces: Entscheidung „Etappe 3 eigenständig vor dem Kern-Sprung: ja/nein" im Ergebnis-Dokument.

- [ ] **Step 1: RDF4J-5-Server als Container starten**

```bash
docker run -d --name rdf4j5-spike -p 8090:8080 eclipse/rdf4j-workbench:5.1.2
curl -fsS http://localhost:8090/rdf4j/protocol
```

Expected: Container läuft, `/rdf4j/protocol` liefert die Protokollversion (notieren!). Wichtig: Port-Mapping auf **8090**, damit die hartkodierten `localhost:8090`-URIs der Use-Case-Daten stimmen; der lokale Triplestore darf währenddessen nicht laufen. Falls Tag `5.1.2` nicht existiert: verfügbare 5.x-Tags via `docker search`/Docker-Hub prüfen und neuesten 5.x-Tag nehmen, verwendeten Tag im Ergebnis notieren. Hinweis: Im offiziellen Image lautet der Kontextpfad `/rdf4j` — mit `curl` verifizieren; weicht er ab (`/rdf4j-server`), das Port-Mapping beibehalten und stattdessen einen Reverse-Pfad via `docker run`-Env prüfen oder das Ergebnis als „Pfad-Inkompatibilität" festhalten.

- [ ] **Step 2: E2E-Suite gegen den 5er-Server fahren**

Run: `mvn -f e2e/pom.xml verify -Dajan.e2e.triplestoreUrl=http://localhost:8090/rdf4j`
Expected — Erfolgsfall: alle ITs grün (Repos werden vom Service im 5er-Server angelegt, Agent-Lifecycle läuft). Fehlerfall: exakte Fehlermeldung + Stacktrace aus `e2e/target/logs/service.log` sichern.

- [ ] **Step 3: Aufräumen und Ergebnis dokumentieren**

```bash
docker rm -f rdf4j5-spike
```

`docs/superpowers/specs/2026-07-11-spike-results.md` anlegen mit Abschnitt:

```markdown
# Spike-Ergebnisse Modernisierung (Etappe 2)

## Spike A: RDF4J-3.6-Client ↔ RDF4J-5-Server
- Server-Image/Tag: <tag>
- Protokollversion Server: <n>
- E2E-Ergebnis: <gruen | rot mit Fehlermeldung>
- **Entscheidung:** Triplestore-Neubau als eigenstaendige Etappe 3: <ja/nein>
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-07-11-spike-results.md
git commit -m "docs: record spike A result (RDF4J 3.6 client vs RDF4J 5 server)"
git push
```

---

### Task 10: Spike B — RESTEasy-Spring-Boot-Starter unter Boot 4

**Files:**
- Create: `spikes/boot4-resteasy/pom.xml`
- Create: `spikes/boot4-resteasy/src/main/java/spike/SpikeApplication.java`
- Create: `spikes/boot4-resteasy/src/main/java/spike/JaxrsConfig.java`
- Create: `spikes/boot4-resteasy/src/main/java/spike/HelloResource.java`
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Spike B")

**Interfaces:**
- Consumes: nichts (Standalone-Projekt außerhalb des Reaktors; braucht lokal JDK 17+ für Boot 4 — JDK 21 empfohlen, s. Spike D).
- Produces: Entscheidung „JAX-RS-Port vs. MVC-Umbau" im Ergebnis-Dokument.

- [ ] **Step 1: Neueste Versionen ermitteln**

Auf https://central.sonatype.com nachschlagen und notieren: neueste `org.springframework.boot:spring-boot-starter-parent` 4.x sowie neueste Version von `org.jboss.resteasy.spring.boot:resteasy-servlet-spring-boot-starter` (prüfen, ob es eine explizit Boot-4-kompatible Linie gibt — Release Notes des Projekts github.com/resteasy/resteasy-spring-boot lesen).

- [ ] **Step 2: Spike-Projekt anlegen**

`spikes/boot4-resteasy/pom.xml` (Versionen aus Step 1 einsetzen):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.dfki.asr.ajan.spikes</groupId>
  <artifactId>boot4-resteasy</artifactId>
  <version>0.1</version>
  <packaging>jar</packaging>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version><!-- neueste 4.x aus Step 1 --></version>
    <relativePath/>
  </parent>

  <dependencies>
    <dependency>
      <groupId>org.jboss.resteasy.spring.boot</groupId>
      <artifactId>resteasy-servlet-spring-boot-starter</artifactId>
      <version><!-- neueste aus Step 1 --></version>
    </dependency>
  </dependencies>
</project>
```

`SpikeApplication.java`:

```java
package spike;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpikeApplication.class, args);
    }
}
```

`JaxrsConfig.java`:

```java
package spike;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/api")
public class JaxrsConfig extends Application {
}
```

`HelloResource.java`:

```java
package spike;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.springframework.stereotype.Component;

@Component
@Path("/hello")
public class HelloResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        return "hello";
    }
}
```

- [ ] **Step 3: Starten und testen**

Run: `mvn -f spikes/boot4-resteasy/pom.xml spring-boot:run` (separates Terminal), dann `curl -fsS http://localhost:8080/api/hello`
Expected — Erfolgsfall: Ausgabe `hello` → JAX-RS-Port ist gangbar. Fehlerfall (Dependency-Auflösung schlägt fehl, Autokonfiguration greift nicht, Servlet-API-Konflikt): exakte Fehlermeldung sichern → Entscheidung MVC-Umbau.

- [ ] **Step 4: Ergebnis dokumentieren und committen**

In `docs/superpowers/specs/2026-07-11-spike-results.md` ergänzen:

```markdown
## Spike B: RESTEasy-Spring-Boot-Starter unter Boot 4
- Boot-Version: <x>, Starter-Version: <y>
- Ergebnis: <gruen | rot mit Fehlermeldung>
- **Entscheidung:** REST-Layer im Kern-Sprung: <JAX-RS-Port | MVC-Umbau>
```

```bash
git add spikes/boot4-resteasy docs/superpowers/specs/2026-07-11-spike-results.md
git commit -m "spike: RESTEasy spring-boot starter on Boot 4 (record result)"
git push
```

---

### Task 11: Spike C — Undertow unter Boot 4

**Files:**
- Modify: `spikes/boot4-resteasy/pom.xml`
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Spike C")

**Interfaces:**
- Consumes: Spike-B-Projekt (Task 10).
- Produces: Entscheidung „Undertow behalten vs. Tomcat" im Ergebnis-Dokument.

- [ ] **Step 1: Auf Undertow umstellen**

In `spikes/boot4-resteasy/pom.xml` beim RESTEasy-Starter (bzw. beim Web-Starter, falls Spike B auf MVC hinauslief: `spring-boot-starter-web`) den Tomcat ausschließen und Undertow ergänzen:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

plus im vorhandenen Web/RESTEasy-Starter:

```xml
<exclusions>
  <exclusion>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
  </exclusion>
</exclusions>
```

- [ ] **Step 2: Starten und prüfen**

Run: `mvn -f spikes/boot4-resteasy/pom.xml spring-boot:run`, dann `curl -fsS http://localhost:8080/api/hello`
Expected — Erfolgsfall: Log enthält `Undertow started` und curl liefert `hello`. Fehlerfall (Starter existiert in Boot 4 nicht / Auflösung schlägt fehl): Fehlermeldung sichern → Entscheidung Tomcat.

- [ ] **Step 3: Ergebnis dokumentieren und committen**

In `docs/superpowers/specs/2026-07-11-spike-results.md` ergänzen:

```markdown
## Spike C: Undertow unter Boot 4
- Ergebnis: <gruen | rot mit Fehlermeldung>
- **Entscheidung:** Servlet-Container im Kern-Sprung: <Undertow | Tomcat>
```

```bash
git add spikes/boot4-resteasy docs/superpowers/specs/2026-07-11-spike-results.md
git commit -m "spike: undertow availability on Boot 4 (record result)"
git push
```

---

### Task 12: Spike D — pf4j aktuell + Fat-JAR-Loading unter JDK 21

**Files:**
- Create: `spikes/pf4j-java21/pom.xml`
- Create: `spikes/pf4j-java21/src/main/java/spike/PluginLoadCheck.java`
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Spike D")

**Interfaces:**
- Consumes: gebautes Fat-JAR `pluginsystem/deployments/StandardBTNodes-0.1-all.jar` (entsteht bei `mvn install`); lokal installiertes JDK 21 (Temurin; nur für diesen Spike).
- Produces: pf4j-Zielversion + Liste nötiger Loader-API-Anpassungen im Ergebnis-Dokument.

- [ ] **Step 1: Neueste pf4j-Version ermitteln**

Auf https://central.sonatype.com `org.pf4j:pf4j` nachschlagen, neueste Version notieren; Changelog/Migration-Notes von 3.6.0 → Zielversion überfliegen (github.com/pf4j/pf4j/releases) und API-Brüche stichpunktartig notieren (relevant für `pluginsystem/loader`).

- [ ] **Step 2: Spike-Projekt anlegen**

`spikes/pf4j-java21/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.dfki.asr.ajan.spikes</groupId>
  <artifactId>pf4j-java21</artifactId>
  <version>0.1</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.pf4j</groupId>
      <artifactId>pf4j</artifactId>
      <version><!-- neueste aus Step 1 --></version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>2.0.13</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <mainClass>spike.PluginLoadCheck</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

`PluginLoadCheck.java`:

```java
package spike;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;

/**
 * Prueft: findet und resolved die aktuelle pf4j-Version ein bestehendes
 * AJAN-Fat-JAR im Deployment-Modus (wie im Docker-Image) unter JDK 21?
 * Volles Starten ist nicht Ziel (provided-Deps fehlen hier bewusst) —
 * Descriptor-Resolution + Classloading-Verhalten sind die Fragen.
 */
public final class PluginLoadCheck {
    public static void main(String[] args) throws Exception {
        Path pluginsDir = Files.createTempDirectory("pf4j-spike");
        Path fatJar = Path.of("..", "..", "pluginsystem", "deployments", "StandardBTNodes-0.1-all.jar")
                .toAbsolutePath().normalize();
        Files.copy(fatJar, pluginsDir.resolve("StandardBTNodes-0.1.jar"), StandardCopyOption.REPLACE_EXISTING);

        System.setProperty("pf4j.mode", "deployment");
        DefaultPluginManager manager = new DefaultPluginManager(pluginsDir);
        manager.loadPlugins();
        for (PluginWrapper p : manager.getPlugins()) {
            System.out.println("RESOLVED: " + p.getPluginId() + " state=" + p.getPluginState());
        }
        if (manager.getPlugins().isEmpty()) {
            throw new IllegalStateException("no plugin resolved from " + pluginsDir);
        }
    }
}
```

- [ ] **Step 3: Unter JDK 21 laufen lassen**

Run (JDK 21 aktivieren, z.B. `JAVA_HOME` auf Temurin 21 setzen):
`mvn -f spikes/pf4j-java21/pom.xml compile exec:java`
Expected — Erfolgsfall: Ausgabe `RESOLVED: StandardBTNodes state=RESOLVED`. Fehlerfall: Fehlermeldung sichern (Descriptor-Format? Manifest-Attribute? Classloader?).

- [ ] **Step 4: Ergebnis dokumentieren und committen**

In `docs/superpowers/specs/2026-07-11-spike-results.md` ergänzen:

```markdown
## Spike D: pf4j aktuell + Fat-JAR unter JDK 21
- pf4j-Zielversion: <x>
- Ergebnis: <gruen | rot mit Fehlermeldung>
- Noetige Loader-Anpassungen (pluginsystem/loader): <Stichpunkte aus Changelog + Beobachtung>
```

```bash
git add spikes/pf4j-java21 docs/superpowers/specs/2026-07-11-spike-results.md
git commit -m "spike: pf4j latest + fat-jar resolution on JDK 21 (record result)"
git push
```

---

### Task 13: Spike-Konsolidierung und Freigabe für Plan 2

**Files:**
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Abschnitt „Konsequenzen")
- Modify: `docs/superpowers/specs/2026-07-11-java-modernization-design.md` (nur falls eine Spike-Entscheidung der Spec widerspricht)

**Interfaces:**
- Consumes: Ergebnisse Spike A–D.
- Produces: abgeschlossene Entscheidungsgrundlage; danach wird Plan 2 (Etappe 3: Triplestore) bzw. Plan 3 (Etappe 4: Kern-Sprung) geschrieben — NICHT Teil dieses Plans.

- [ ] **Step 1: Konsequenzen-Abschnitt schreiben**

In `2026-07-11-spike-results.md` anfügen:

```markdown
## Konsequenzen fuer die Folgeplaene
- Etappe 3 (Triplestore) eigenstaendig: <ja/nein, aus Spike A>
- REST-Layer Kern-Sprung: <JAX-RS-Port | MVC-Umbau, aus Spike B>
- Servlet-Container: <Undertow | Tomcat, aus Spike C>
- pf4j-Zielversion + Loader-Aufwand: <aus Spike D>
```

- [ ] **Step 2: Spec aktualisieren, falls nötig**

Widerspricht eine Entscheidung der Spec (z.B. MVC statt JAX-RS), den betreffenden Spec-Abschnitt (Abschnitt 2 bzw. 3) anpassen — die Spec bleibt die einzige Quelle der Wahrheit für die Folgepläne.

- [ ] **Step 3: Abschluss-Verifikation des gesamten Plans**

Run: `mvn install -q && mvn -f e2e/pom.xml verify`
Expected: beide `BUILD SUCCESS`; CI `ci-branch` für den letzten Push grün.

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/
git commit -m "docs: consolidate spike results and decisions for plan 2/3"
git push
```

---

## Nach diesem Plan

- **Plan 2** (Etappe 3, Triplestore-Neubau) und **Plan 3** (Etappen 4–6, Kern-Sprung + Feinschliff + Java 25) werden erst nach Task 13 geschrieben — sie hängen von den Spike-Ergebnissen ab. Das ist beabsichtigt: Pläne ohne diese Fakten wären Platzhalter-Pläne.
