# Modernisierung Plan 3: Kern-Sprung (Etappe 4) — Java 21, Spring Boot 4, Jakarta, pf4j 3.15

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Den gesamten Reaktor auf Java 21 heben, den executionservice von Spring Boot 1.3.5/RESTEasy 3/Undertow/javax auf Spring Boot 4.1.2/RESTEasy 6 (offizieller Starter)/Tomcat/Jakarta umstellen, pf4j auf 3.15.0 bringen und Swagger auf OpenAPI 3 migrieren — bei unveränderter öffentlicher HTTP-Oberfläche.

**Architecture:** Erst wird das Sicherheitsnetz um genau die Oberfläche erweitert, für die es heute blind ist (CORS, Swagger, Redirects, Content-Types, Plugin-Deployment-Modus), und zwar charakterisierend gegen den **alten** Stack. Dann werden alle Teilschritte, die noch unter Boot 1.3.5 grün bleiben können, einzeln vorgezogen (JDK-21-Toolchain bei Target 11, pf4j-Bump, Bibliotheks-Aufräumarbeiten, SPARQL-Härtung). Erst danach folgt der unteilbare Kern-Flip (Boot 4 + Jakarta + Release 21 über alle Module), dem nur noch Swagger als eigener Task folgt. Damit kommen bei einem roten Flip nur noch Ursachen in Frage, die sich nicht vorziehen ließen.

**Tech Stack:** Java 21 (Temurin/MS-Build), Maven 3.9.11, Spring Boot 4.1.2, `org.jboss.resteasy:resteasy-servlet-spring-boot-starter:6.3.0.Final` (RESTEasy 6.2.11, Jakarta REST 3.1), Tomcat 11 (Boot-verwaltet), Jackson 2 (`com.fasterxml`, Boot-verwaltet über `jackson-2-bom`), pf4j 3.15.0, swagger-core 2.2.55 (`swagger-jaxrs2-jakarta`), swagger-ui-Webjar 5.34.0, TestNG 7.10.2, JUnit 5 (nur `e2e/`).

**Spec:** `docs/superpowers/specs/2026-07-11-java-modernization-design.md` (Abschnitt 4, Etappe 4; Risiken 1, 4, 6)
**Spike-Grundlage:** `docs/superpowers/specs/2026-07-11-spike-results.md` (Spikes B, C, D; offene Fragen 1, 2, 6)
**Ledger:** `.superpowers/sdd/progress.md` (Abschnitt „ETAPPE 3", Liste „ZWINGEND IN ETAPPE 4")

---

## Zuschnitt

Plan 3 umfasst **nur Etappe 4**. Die Etappen 5 (Deployment/Feinschliff) und 6 (Java 25) bekommen einen eigenen Plan 4, sobald Etappe 4 abgeschlossen ist — beide hängen inhaltlich vom Ausgang des Kern-Sprungs ab (Dockerfile-Basis, CI-Distribution, BeliefUpdateStallTest), und Etappe 4 allein ist bereits die größte Etappe des Epics.

## Vorbefunde (beim Planschreiben am 2026-10-30 verifiziert)

Geprüft gegen das Repo (Branch-Head `ea743d30`) und Maven Central. Belege stehen in den Recherche-Notizen dieser Session; jeder Task nennt das Kommando zum Nachvollziehen, wo es darauf ankommt.

| # | Befund | Beleg |
|---|---|---|
| W1 | Neueste Boot-4.1-Linie ist **4.1.2** (Spec nannte 4.1.0; Patch-Anhebung ohne Linienwechsel). Boot 4.1.2 verlangt Java ≥17 und unterstützt bis Java 27 — also 21 **und** 25. | `maven-metadata.xml` von `spring-boot-dependencies`; docs.spring.io/spring-boot/system-requirements.html |
| W2 | `spring-boot-dependencies:4.1.2` verwaltet: Tomcat 11.0.27, Jackson 3 (`tools.jackson`, 3.1.7) **und weiterhin Jackson 2** (`com.fasterxml.jackson:jackson-bom` 2.21.7, Property `jackson-2-bom.version`), `jakarta.ws.rs-api` **4.0.0** (Property `jakarta-ws-rs.version`), `jakarta.servlet-api` 6.1.0, slf4j 2.0.20, lombok 1.18.48, hamcrest 3.0. **Nicht** verwaltet: TestNG, Apache httpclient 4.x. | `spring-boot-dependencies-4.1.2.pom` |
| W3 | `resteasy-servlet-spring-boot-starter:6.3.0.Final` ist die neueste Version, gebaut gegen Boot 3.4.0 / RESTEasy **6.2.11.Final** / Jakarta REST **3.1.0**. **Korrektur zu Spike B:** Der Starter zieht sehr wohl Transitive (resteasy-core/-spi 6.2.11, resteasy-spring 3.2.0, resteasy-jackson2-provider, spring-boot-starter-web) — schließt aber `spring-boot-starter-tomcat` **aus**. ⇒ `spring-boot-starter-tomcat` muss explizit deklariert werden. | `mvn dependency:tree` eines Probe-Projekts unter Boot 4.1.2 |
| W4 | Probe unter Boot 4.1.2 + JDK 21: `@Component @ApplicationPath` + `@Component @Path` + `@Component @Provider ContainerResponseFilter` → Start ok, 200, Filter greift, Jackson-2-JSON funktioniert neben Boot-Jackson-3. Einzige Auffälligkeit: WARN `TypeNotPresentException: ...WebMvcAutoConfiguration not present` (der Starter referenziert die Boot-3-Klasse in `@AutoConfiguration(after=…)`). Die `Application`-Klasse muss `public` sein (sonst RESTEASY003900). | Probe-Projekt (Scratchpad der Planungssession) |
| W5 | RESTEasy 6.2 ist gegen Jakarta REST 3.1 gebaut, Boot 4.1.2 hebt die API auf 4.0.0. Die Probe lief mit 4.0.0, ist aber nicht der volle REST-Layer. ⇒ **Pin `jakarta-ws-rs.version=3.1.0`** im executionservice (konservativ: die Version, gegen die die Implementierung gebaut ist). | `resteasy-dependencies-6.2.19.Final.pom` (`version.jakarta.ws.rs`) |
| W6 | `org.jboss.resteasy.annotations.interception.ServerInterceptor` existiert in keinem RESTEasy-6.2.x-Jar (geprüft 6.2.11 und 6.2.19, core und core-spi). | `jar tf resteasy-core-6.2.19.Final.jar` |
| W7 | `ResteasyServletContext` (`executionservice/.../executionservice/ResteasyServletContext.java`) existiert **ausschließlich** für Swagger 1.5: Er legt pro Request den `ServletContext` in RESTEasys Kontext-Map, damit `ApiListingResource` ihn per `@Context` bekommt (Kommentar verweist auf swagger-core#1491). Mit Swagger 1.5 entfällt sein Zweck. ⇒ Offene Frage 2 der Spike-Ergebnisse („Ersatzmechanik für `@ServerInterceptor`") beantwortet sich durch **Entfernen**, nicht durch Ersetzen. | Quelltext + Kommentar Zeilen 44-46 |
| W8 | executionservice hat **keine** `application.properties`/`.yml`; alle Konfiguration läuft über `@Value` mit Defaults und `-D`-System-Properties (`startAJAN.sh`, `docker/supervisord.conf`). Keine `server.undertow.*`-Properties ⇒ Spec-Risiko 6 (Undertow-Defaults) reduziert sich auf Container-Verhalten, nicht auf Property-Umbenennung. | `ls executionservice/src/main` (nur `java/`) |
| W9 | executionservice: 92 `javax`-Importzeilen in 26 Dateien, **alle** wandern nach Jakarta (`ws.rs`, `servlet`, `annotation`); keine JDK-`javax`-Pakete. Außerhalb: `common` (`AgentUtil.java:40`, `MimeUtil.java:32`), `behaviour` (`HttpConnection.java:34-35`) — nur `javax.ws.rs.core`. MOSIM: `@javax.annotation.Generated` in 95 Thrift-generierten Dateien (Source-Retention, kein Jakarta-Bedarf, braucht nur das Artefakt `javax.annotation:javax.annotation-api` zur Compile-Zeit, heute nur transitiv vorhanden). Mapping/MQTT: `javax.xml.transform` (JDK, bleibt). | grep über alle Module |
| W10 | Boot-1.x-APIs im Code: `SwaggerUIConfiguration` (`extends WebMvcConfigurerAdapter` + `@EnableWebMvc`), Test `BeliefUpdateStallTest` (`SpringApplicationConfiguration`, `WebIntegrationTest`, seit Boot 1.4 entfernt). Der Test ist per Surefire-Exclude ausgeschlossen, wird aber **kompiliert** — er muss mit portiert werden. `spring-boot-maven-plugin` hat eine `springloaded:1.2.6.RELEASE`-Dependency (muss raus). | `executionservice/pom.xml:29-39,85` |
| W11 | Das heutige Fat-JAR des executionservice enthält **Undertow 1.3.22 und tomcat-embed 8.0.33 gleichzeitig** (Tomcat ist nur an `spring-boot-starter` ausgeschlossen, nicht an `-web`). | `jar tf executionservice/target/executionservice-0.1.jar` |
| W12 | CORS heute: Spring-`CorsFilter`-Bean in `ExecutionServiceApplication` mit `addAllowedOrigin("*")`, alle Header, 7 Methoden, auf `/**`. **Falle:** In Spring 4.2 ist `allowCredentials` per Default `true` (Antwort spiegelt den Origin und setzt `Access-Control-Allow-Credentials: true`); ab Spring 5.3 ist der Default `null`, und `*` + `allowCredentials=true` wirft `IllegalArgumentException`. Das tatsächliche Ist-Verhalten wird in Task 1 **gemessen**, nicht angenommen. | `ExecutionServiceApplication.java:50-65`; Spring-Javadoc `CorsConfiguration` |
| W13 | Swagger heute: `/ajan/swagger.json` (Swagger 2.0 über `ApiListingResource`), UI-Webjar 2.1.4 unter `/swagger-ui/**`, `GET /` leitet auf `/swagger-ui/index.html?url=/ajan/swagger.json` um. Swagger-UI ≥4.1.3 ignoriert den `url`-Query-Parameter per Default (`queryConfigEnabled=false`); die UI-5-Konfiguration läuft über `swagger-initializer.js`. | `SwaggerUIConfiguration.java`; swagger-ui-Doku |
| W14 | pf4j: Version **literal `3.6.0` an 4 Stellen**, keine Property (`loader/pom.xml:54`, `plugins/pom.xml:21` (Processor-Pfad), `plugins/pom.xml:108`, `ScriptExecutorPlugin/pom.xml:54`). Der Kommentar `AJANPluginLoader.java:45-46` („ohne extensions.idx") ist falsch — `LegacyExtensionFinder` **liest** `META-INF/extensions.idx`. | grep `pf4j` über alle POMs |
| W15 | `plugins/pom.xml:9-27` pinnt `maven-compiler-plugin` **3.8.1** (überschreibt Root 3.13.0) und trägt `annotationProcessorPaths` (lombok 1.18.30 + pf4j 3.6.0). executionservice und rdfbeans haben **keine** `annotationProcessorPaths` und verlassen sich auf implizite Annotation-Verarbeitung (Lombok). Ab JDK 23 läuft die nicht mehr implizit; ab JDK 21 gibt javac einen Hinweis aus. | POMs; inside.java 2024-06-18 |
| W16 | Lombok: JDK 21 ab 1.18.30, **JDK 25 ab 1.18.40**; aktuell 1.18.48 (= Boot-4.1.2-Pin). | projectlombok.org/changelog |
| W17 | `MQTTPlugin/pom.xml:206-210` deklariert `executionservice` **ohne Scope** (= compile) ⇒ landet im Fat-JAR des Plugins. Außerdem hat MQTTPlugin **keine** `plugin.*`-Properties: Das Deployment-JAR trägt leeres `Plugin-Id:`/`Plugin-Class:` im Manifest. | `MQTTPlugin/pom.xml`; `unzip -p pluginsystem/deployments/MQTTPlugin-0.1-all.jar META-INF/MANIFEST.MF` |
| W18 | Tests: `behaviour` hat `<skipTests>true</skipTests>` (`behaviour/pom.xml:175`) — dort laufen heute **keine** Tests. executionservice: 0 laufende Tests. Plugins/functions/triplestore: keine Tests. Laufend: rdfbeans (JUnit 4, 27 Dateien), common (TestNG, 2 Dateien), e2e (16 Tests). | POMs |
| W19 | PMD: `maven-pmd-plugin` 3.6 mit `targetJdk 1.8` (Root-`pluginManagement` und `executionservice/pom.xml:52`), aktiv in behaviour, common, executionservice. Spotbugs 4.7.x in behaviour. Beide sind älter als JDK 21. | POMs |
| W20 | opencsv 4.6 → 5.x: entfernte Konstruktoren, `readNext()` wirft zusätzlich `CsvValidationException`. Aufrufstellen nur: `common/AgentUtil.java:307` (`new CSVReader(InputStreamReader)`), `MappingPlugin/.../MappingUtil.java:140` (`new CSVWriter(sw)`, `writeNext`). | grep `com.opencsv` |
| W21 | Tomcat 9 (Triplestore-Launcher, bleibt javax) läuft offiziell auf jeder stabilen Java-Version ≥8 — Java 21 erzwingt dort nichts. | tomcat.apache.org/whichversion.html |
| W22 | Die E2E-Suite startet Triplestore und Service mit `System.getProperty("java.home")` (`AjanSystem.java:44`) — also mit dem JDK, unter dem Maven läuft. Ein JDK-Wechsel des Maven-Laufs schaltet die Service-Laufzeit automatisch mit um. | `AjanSystem.java:44,60,80` |

**Eigenständig getroffene Entscheidungen** (vom Auftraggeber am 2026-10-30 zur Selbstfreigabe delegiert; nicht neu aufzurollen, außer ein Task-Gate widerlegt die Annahme):

1. **Boot 4.1.2 statt 4.1.0** (W1): Patch-Anhebung innerhalb der von der Spec gewählten Linie.
2. **Jackson 2 bleibt** für den Produktcode (`JsonNode` in `AgentResource`, `JsonConsumer`, `common/AgentUtil`, `common/Credentials`) und für RESTEasy (`resteasy-jackson2-provider`). Kein Port auf Jackson 3 (`tools.jackson`) in dieser Etappe — der Code nutzt Jackson nur als Datentyp, und RESTEasy 6.2 kennt nur Jackson 2 (W2, W4).
3. **`jakarta-ws-rs.version=3.1.0`** im executionservice gepinnt (W5).
4. **`ResteasyServletContext` wird gelöscht**, nicht ersetzt (W7) — erst in Task 7, zusammen mit Swagger 1.5.
5. **Swagger-URL bleibt `/ajan/swagger.json`**, liefert aber OpenAPI 3 (Spec: „Docs-Endpunkt bleibt erreichbar; Formatwechsel auf OpenAPI 3 ist akzeptiert"). `GET /` leitet weiterhin auf die UI um; die UI zeigt per eigenem `swagger-initializer.js` auf `/ajan/swagger.json` (W13).
6. **pf4j bleibt bei 3.15.0** (Spike-D-verifiziert), obwohl 3.16.0 existiert; die Anhebung fällt unter „restliche Dependency-Updates" in Etappe 5.
7. **Plan B (MVC-Umbau) wird nicht vorsorglich angefasst.** Der Umschaltpunkt ist in Task 6 definiert und ist eine **Auftraggeber-Entscheidung**.

---

## Global Constraints

- Alle Arbeit auf Branch `modernization/java21`; `master` wird nicht angefasst, nichts wird nach `master` gemerged oder dorthin gepusht. Push des Epic-Branches nach `origin` ist erlaubt.
- `.github/workflows/docker-image.yml` wird **NICHT** verändert (Nachzug Etappe 5). `.github/workflows/ci-branch.yml` darf und muss angepasst werden (Task 2).
- Kein `docker push`, keine Tags `aantakli/ajan-service*`.
- Die öffentliche HTTP-API bleibt unverändert. Charakterisierte IST-Werte, die erhalten bleiben **müssen**: `GET /ajan/agents/{id}` mit `Accept: text/turtle` → 200 + `text/turtle; charset=UTF-8`; mit `Accept: application/ld+json` → 200 + `application/ld+json; charset=UTF-8`; unbekannter Agent → 404 + Turtle-Fehlerkörper; malformed Turtle → 400 + `text/html; charset=UTF-8`; sowie alle in Task 1 neu charakterisierten Werte.
- Ports fix: Triplestore **8090**, executionservice **8080**; Kontextpfade `/rdf4j`, `/workbench`, REST-Basis `/ajan`.
- Zielversionen: Java-Release **21** (einzige Quelle: `maven.compiler.release` im Root-POM, plus executionservice als Management-Insel über `java.version`); Spring Boot **4.1.2**; RESTEasy-Starter **6.3.0.Final**; `jakarta.ws.rs-api` **3.1.0**; pf4j **3.15.0**; RDF4J bleibt **5.3.1**; Triplestore bleibt Embedded **Tomcat 9** (javax); Lombok **1.18.48**.
- `e2e/` bleibt auf `maven.compiler.release=11` und RDF4J 3.6.3 (Etappe-3-Entscheidung: die Suite testet von außen und überlebt alle Etappen).
- Build-Umgebung (nichts im PATH):
  - JDK 21: `C:\Users\yanni\.jdks\ms-21.0.11` — **ab Task 2 Default für alle Kommandos**
  - JDK 11: `C:\Users\yanni\.jdks\temurin-11` — nur Task 1 und Vergleichsläufe
  - Maven: `C:\Users\yanni\AppData\Local\Programs\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd`
  - PowerShell-Muster: `$env:JAVA_HOME="C:\Users\yanni\.jdks\ms-21.0.11"; & "C:\Users\yanni\AppData\Local\Programs\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" install`
  - E2E-Läufe binden die Ports 8080/8090 — niemals zwei gleichzeitig.
- **Verifikation pro Task:** `mvn install` grün **und** `mvn -f e2e/pom.xml verify` grün (Ausnahme: Rot-Fenster unten). Der Erfolg zählt nur mit der Summenzeile der Failsafe-Reports im Task-Report (`Tests run: N, Failures: 0, Errors: 0`).
- **Fix-Richtungen nur mit verifizierter Annahme** (Lehre Etappe 3): Wo dieser Plan eine Ursache vermutet, gibt er ein Prinzip und ein Beweis-Gate vor, keinen Code. Ein Implementer, der die Annahme widerlegt findet, stoppt und berichtet, statt sie passend zu machen.

### Rot-Fenster-Regel

Zwischen **Task 6** (Kern-Flip) und **Task 7** (Swagger) ist genau eine erwartete Abweichung erlaubt: Die in Task 1 angelegten Swagger-Tests (`swaggerSpecIsServed`, `swaggerUiIsServed`, `rootRedirectsToSwaggerUi`) dürfen rot sein, weil Swagger 1.5 in Task 6 entfernt und erst in Task 7 ersetzt wird. **Jeder andere rote E2E-Test nach Task 6 ist ein echter Befund** und stoppt den Task. Nach Task 7 ist die Suite wieder vollständig grün.

### Umschaltpunkt Plan B (Auftraggeber-Entscheidung)

Der MVC-Umbau (Spec Entscheidung 2/3, Plan B) wird ausgelöst, **wenn und nur wenn** nach Task 6 ein E2E- oder Charakterisierungs-Test rot bleibt, dessen Ursache per `superpowers:systematic-debugging` nachweislich im RESTEasy-Starter/Boot-4-Zusammenspiel liegt (nicht im eigenen Migrationscode) und sich nicht mit Konfiguration oder einem eigenen JAX-RS-Provider beheben lässt. In diesem Fall: Task 6 **nicht** committen, Befund mit Beleg in den Ledger, **Auftraggeber fragen**. Plan B ist eine Scope-Änderung von mehreren Tagen und wird nicht eigenmächtig begonnen.

---

## Review Focus

Diese fünf Eingabeklassen sind die wahrscheinlichsten Quellen einer Regression, die heute kein Test sieht; jeder Zeile ist unten ein Test im zuständigen Task zugeordnet.

1. **Browser-Client des AJAN-editor mit `Origin`-Header** (CORS-Preflight und einfacher Request): Der Editor erwartet exakt dieselben `Access-Control-*`-Antwortheader wie heute. Spring 6 hat andere Defaults (W12). → Task 1 `corsSimpleRequest`, `corsPreflight`; geschützt in Task 6.
2. **Request-Body mit Parametern im Content-Type** (`application/json; charset=UTF-8`, `text/turtle;charset=utf-8`): `JsonConsumer` vergleicht den MediaType per exaktem String (`mt.toString().equals(...)`). RESTEasy 6 kann `MediaType.toString()` anders serialisieren. → Task 1 `jsonBodyWithCharsetIsAccepted`; geschützt in Task 6.
3. **Plugin-Deployment-Modus** (`pf4j.mode=deployment` aus `docker/supervisord.conf`): Die E2E-Suite prüft nur den Entwicklungsmodus; der pf4j-Bump und der Extension-Finder-Wechsel wirken aber genau im Deployment-Modus (offene Frage 6). → Task 3 `loadcheck`-Modul.
4. **Aufruf eines Endpunkts außerhalb eines Requests** (`AgentManager.java:182` ruft `UriBuilder.fromUri` beim Start): braucht den JAX-RS-`RuntimeDelegate` auf dem Klassenpfad zur Startzeit; fehlt er, startet der Service, aber die Agenten-Initialisierung scheitert. → abgedeckt durch `AgentLifecycleIT` (Agent anlegen) — Task 6 muss den Startlog zusätzlich auf `RuntimeDelegate`-Fehler prüfen.
5. **Swagger-Aufruf aus dem Browser** (`GET /` → UI → Spec): UI 5 ignoriert `?url=` (W13), sodass die UI still die Petstore-Demo statt der AJAN-API zeigen würde. → Task 7 `swaggerUiPointsToAjanSpec`.

---

### Task 1: Sicherheitsnetz erweitern — Web-Oberfläche charakterisieren (alter Stack)

**Files:**
- Create: `e2e/src/test/java/de/dfki/asr/ajan/e2e/WebSurfaceIT.java`
- Modify: `e2e/src/test/java/de/dfki/asr/ajan/e2e/Http.java` (Methode mit frei wählbaren Request-Headern)

**Interfaces:**
- Produces: `Http.sendWithHeaders(String method, String url, Map<String,String> headers, String body) → HttpResponse<String>`; Testklasse `WebSurfaceIT` mit den unten benannten Testmethoden. Tasks 6 und 7 verlassen sich auf genau diese Methodennamen (Rot-Fenster-Regel).

Läuft unter **JDK 11** gegen den unveränderten Stack (Branch-Head `ea743d30`). Methode wie `ErrorPathIT`: erst messen, dann den gemessenen Wert festnageln, jeder Test trägt einen `// OBSERVED (2026-10-30): …`-Kommentar mit dem Rohbefund.

- [ ] **Step 1: Ist-Werte messen.** System starten (`mvn -f e2e/pom.xml verify` einmal laufen lassen oder die Startkommandos aus `AjanSystem` von Hand), dann mit `curl -si` messen und die Rohantworten (Statuszeile + alle Header) in den Task-Report kopieren:
  - `GET /ajan/agents` mit `Origin: http://editor.example` (einfacher CORS-Request)
  - `OPTIONS /ajan/agents` mit `Origin: http://editor.example`, `Access-Control-Request-Method: POST`, `Access-Control-Request-Headers: content-type`
  - `GET /ajan/swagger.json`, `GET /` (ohne Redirect folgen), `GET /swagger-ui/index.html`
  - `GET /ajan/tripledatabases` mit `Accept: application/json`
  - `GET /ajan/ping`
  - `POST /ajan/agents/{id}?capability=HelloWorld_BT` mit `Content-Type: application/json; charset=UTF-8` und Body `{}` gegen einen frisch angelegten Agenten (Anlage wie `AgentLifecycleIT.createAgentFromHelloWorldTemplate`) — zusätzlich dieselbe Anfrage mit exakt `Content-Type: application/json`.

- [ ] **Step 2: Tests schreiben, die die gemessenen Werte festnageln.** Testmethoden (Namen sind verbindlich):
  - `corsSimpleRequest` — assert Status wie gemessen; `Access-Control-Allow-Origin` exakt wie gemessen (erwartet laut W12 entweder `*` oder der gespiegelte Origin); `Access-Control-Allow-Credentials` exakt wie gemessen bzw. `assertTrue(header.isEmpty())`, falls nicht gesendet.
  - `corsPreflight` — Status; `Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials`; `Access-Control-Allow-Methods` als **Menge** verglichen (Reihenfolge ist kein Vertrag); `Access-Control-Allow-Headers` enthält `content-type` (case-insensitive). `Vary` wird **nicht** festgenagelt.
  - `swaggerSpecIsServed` — 200; Content-Type wie gemessen, aber nur der Medientyp-Anteil ohne Parameter (`application/json`); Body enthält `"/agents"`. **Nicht** auf `"swagger"` oder `"openapi"` prüfen (Formatwechsel ist erlaubt).
  - `rootRedirectsToSwaggerUi` — Status 3xx wie gemessen; `Location` endet auf `/swagger-ui/index.html?url=/ajan/swagger.json` **oder** zeigt auf `/swagger-ui/index.html` (Task 7 darf den Query-Parameter fallenlassen — der Test akzeptiert beides, prüft aber, dass `Location` auf `/swagger-ui/index.html` zeigt).
  - `swaggerUiIsServed` — `GET /swagger-ui/index.html` → 200, Medientyp `text/html`.
  - `tripleDatabasesAsJson` — Status und Medientyp wie gemessen; Body ist ein JSON-Array (beginnt nach `trim()` mit `[`).
  - `pingAnswers` — Status und Medientyp wie gemessen.
  - `jsonBodyWithCharsetIsAccepted` und `jsonBodyPlainIsAccepted` — Status wie gemessen (beide getrennt festnageln; wenn sich die beiden heute unterscheiden, ist genau diese Differenz der Vertrag und wird im Kommentar benannt).
  Kein Test nutzt `Thread.sleep`; Agentenanlage und -löschung pro Testklasse in `@BeforeAll`/`@AfterAll`, Agent-ID `E2EWebSurfaceAgent`, damit `AgentLifecycleIT` unberührt bleibt.

- [ ] **Step 3: Suite unter JDK 11 laufen lassen.**
  Run: `$env:JAVA_HOME="C:\Users\yanni\.jdks\temurin-11"; & <mvn.cmd> install -DskipTests; & <mvn.cmd> -f e2e/pom.xml verify`
  Expected: `Tests run: 16+N, Failures: 0, Errors: 0` (N = Anzahl neuer Tests). Zweimal hintereinander grün (Flakiness-Check).

- [ ] **Step 4: Positivkontrolle.** Den `corsSimpleRequest`-Erwartungswert testweise auf einen falschen Wert ändern → Test muss rot werden; zurücksetzen. Ergebnis im Report.

- [ ] **Step 5: Commit**
  `git commit -m "test(e2e): characterize CORS, swagger, redirects and JSON body handling before the boot 4 jump"`

---

### Task 2: Toolchain-Gate — Build und Laufzeit auf JDK 21 bei Target 11

**Files:**
- Modify: `pom.xml` (Lombok-Version, `pluginManagement`: PMD, Compiler-Processor-Pfade)
- Modify: `pluginsystem/plugins/pom.xml:9-27` (Compiler-Override 3.8.1 entfernen, Processor-Pfad-Lombok auf Property)
- Modify: `executionservice/pom.xml` (Compiler `annotationProcessorPaths` für Lombok; PMD-Version/`targetJdk`)
- Modify: `rdfbeans/pom.xml` (nur falls rdfbeans Lombok nutzt — vorher grep; sonst unverändert)
- Modify: `behaviour/pom.xml` (Spotbugs-Version, falls auf JDK 21 rot)
- Modify: `.github/workflows/ci-branch.yml` (`java-version: '21'`)

**Interfaces:**
- Produces: Reaktor baut und testet unter JDK 21 mit `maven.compiler.release=11`. Root-Property `lombok.version=1.18.48`, von allen Processor-Pfaden referenziert.

Ziel: alle Werkzeug-Probleme des JDK-Wechsels **vor** dem Framework-Sprung sichtbar machen. Bytecode bleibt Java 11.

- [ ] **Step 1: Baseline unter JDK 21 messen, ohne Änderung.**
  Run: `$env:JAVA_HOME="C:\Users\yanni\.jdks\ms-21.0.11"; & <mvn.cmd> clean install 2>&1 | Tee-Object task2-baseline.log`
  Jede Fehlermeldung mit Modul und Plugin in den Report. Erwartete Kandidaten (unbewiesen): PMD 3.6, Spotbugs 4.7.x, javac-Hinweis zu impliziter Annotation-Verarbeitung.

- [ ] **Step 2: Lombok und Annotation-Verarbeitung explizit machen.** Root-Property `lombok.version=1.18.48`; jedes Modul, das Lombok-Annotationen nutzt, bekommt Lombok in `annotationProcessorPaths` (Root-`pluginManagement` deckt die Kinder ab; executionservice als Insel und `plugins/pom.xml` explizit; in `plugins/pom.xml` zusätzlich pf4j bleibt im Processor-Pfad). Der Compiler-Override 3.8.1 in `plugins/pom.xml` entfällt (Root 3.13.0 gilt).
  Prüfung: `mvn install` unter JDK 21 zeigt **keinen** javac-Hinweis „Annotation processing is enabled because one or more processors were found on the class path" mehr (grep im Log → 0 Treffer), und alle 10 Deployment-JARs enthalten weiterhin `META-INF/extensions.idx` (Schleife über `pluginsystem/deployments/*-all.jar` mit `jar tf … | findstr extensions.idx` → 10 Treffer).

- [ ] **Step 3: Statische Analyse auf JDK 21 bringen.** Nur was in Step 1 rot war: `maven-pmd-plugin` auf die aktuelle Version mit `targetJdk` = `${maven.compiler.release}`; Spotbugs-Maven-Plugin auf die aktuelle 4.9.x. Regeln aus `build-resources` beibehalten. Falls PMD 7 Regeln aus dem Ruleset nicht mehr kennt, werden **nur** umbenannte Regeln auf den neuen Namen gezogen; neu auftretende Verstöße werden behoben, nicht unterdrückt. Jede entfernte Regel steht mit Begründung im Report.

- [ ] **Step 4: Volle Verifikation unter JDK 21.**
  Run: `mvn clean install` und `mvn -f e2e/pom.xml verify` mit `JAVA_HOME=ms-21.0.11`.
  Expected: Reaktor grün; E2E `Failures: 0, Errors: 0` — damit laufen Triplestore und Boot-1.3.5-Service **auf JDK 21** (W22).
  **Gate:** Ist E2E rot und liegt die Ursache nachweislich darin, dass Boot 1.3.5/Undertow 1.3 auf einer JDK-21-Laufzeit nicht laufen, dann: Fehlersignatur in den Ledger; `AjanSystem` bekommt eine optionale System-Property `ajan.e2e.serviceJavaHome` (Default: `java.home`), CI und lokale Läufe starten die Services bis Task 6 mit JDK 11; Task 6 entfernt diese Krücke wieder aus CI. Kein anderer Workaround.

- [ ] **Step 5: CI umstellen.** `ci-branch.yml`: `java-version: '21'` (und ggf. die Gate-Krücke aus Step 4 über ein zweites `setup-java`). Push, CI-Lauf grün, Run-ID in den Report.

- [ ] **Step 6: Commit**
  `git commit -m "build: build and run the reactor on JDK 21 (release stays 11), explicit annotation processing"`

---

### Task 3: pf4j 3.15.0 — Deployment-Modus erst charakterisieren, dann heben

**Files:**
- Create: `pluginsystem/loadcheck/pom.xml`, `pluginsystem/loadcheck/src/test/java/de/dfki/asr/ajan/pluginsystem/loadcheck/DeploymentModeLoadTest.java`
- Modify: `pluginsystem/pom.xml` (Modul `loadcheck` **nach** `plugins`; Property `pf4j.version=3.15.0` in dependencyManagement)
- Modify: `pluginsystem/loader/pom.xml:54`, `pluginsystem/plugins/pom.xml:21,108`, `pluginsystem/plugins/ScriptExecutorPlugin/pom.xml:54` (auf `${pf4j.version}`)
- Modify: `pluginsystem/loader/src/main/java/de/dfki/asr/ajan/pluginsystem/AJANPluginLoader.java` (`LegacyExtensionFinder` → `IndexedExtensionFinder`, Kommentar :45-46 korrigieren)

**Interfaces:**
- Consumes: `AJANPluginLoader#init()`, `#getPLUGIN_MANAGER()`, `getExtensions(NodeExtension.class)`, `getExtensions(EndpointExtension.class)` (bestehend).
- Produces: Reaktormodul `de.dfki.asr.ajan.pluginsystem:loadcheck` (packaging `jar`, nur Tests, kein Artefakt-Konsument), läuft in `mvn install`.

Schließt offene Frage 6 der Spike-Ergebnisse: `startPlugins()`, Extension-Auflösung über den **überschriebenen** Finder und `provided`-Handling im Deployment-Modus.

- [ ] **Step 1: `loadcheck`-Modul unter pf4j 3.6.0 anlegen.** Test-Dependencies: `loader`, `behaviour`, `common` (die `provided`-Seite der Plugins) — **keine** Plugin-Module als Dependency (MQTTPlugin würde sonst das Boot-repackagte executionservice-JAR auf den Klassenpfad ziehen). Die Reihenfolge „nach `plugins`" ergibt sich aus der Modul-Deklaration in `pluginsystem/pom.xml`; im Report mit der „Reactor Build Order" aus dem Maven-Log belegen. Plugins, deren Extensions Host-Klassen aus dem executionservice brauchen (MOSIM, MQTT), können hier an fehlenden Klassen scheitern — das ist ein Artefakt der Testumgebung, wird trotzdem als Ist-Wert festgenagelt und im Kommentar so benannt. Der Test setzt `pf4j.mode=deployment` und `pf4j.pluginsDir=${project.basedir}/../deployments` (per Surefire-`systemPropertyVariables`), ruft `new AJANPluginLoader().init()` und charakterisiert pro Deployment-JAR:
  - `pluginState` (erwartet `STARTED` oder der tatsächlich gemessene Zustand),
  - Anzahl der `NodeExtension`- und `EndpointExtension`-Extensions je Plugin-ID.
  Erst messen (Werte ausgeben), dann als Assertions festnageln mit `// OBSERVED`-Kommentar. **MQTTPlugin** (W17, leeres Manifest) wird so festgenagelt, wie es sich heute verhält — falls es nicht lädt, ist das der Ist-Wert und wird nicht in diesem Task repariert (Ledger-Eintrag für Etappe 5).
  ```java
  @Test
  public void everyDeploymentJarReachesObservedState() { /* Map<String, PluginState> EXPECTED_STATES, aus der Messung */ }
  @Test
  public void extensionCountsPerPluginMatchObserved() { /* Map<String, int[]> {nodeExt, endpointExt} */ }
  ```

- [ ] **Step 2: Grün unter 3.6.0.** `mvn install` → `loadcheck` grün. Positivkontrolle: einen Erwartungswert verfälschen → rot; zurücksetzen. Commit:
  `git commit -m "test(pluginsystem): characterize deployment-mode plugin loading and extension resolution"`

- [ ] **Step 3: pf4j auf 3.15.0 und `IndexedExtensionFinder`.** Property `pf4j.version`, alle vier Fundstellen (W14) darauf; `createExtensionFinder()` liefert `new IndexedExtensionFinder(this)`; Kommentar beschreibt das reale Verhalten (liest `META-INF/extensions.idx`, das der Annotation-Processor beim Plugin-Build erzeugt).
  slf4j-Hinweis: pf4j ≥3.10 ist gegen slf4j 2 gebaut, executionservice pinnt über Boot 1.3.5 slf4j 1.7.21. Der Startlog des Service darf **keine** `NoSuchMethodError`/`NoClassDefFoundError` aus `org.slf4j` enthalten — wenn doch: stoppen und berichten (kein slf4j-Override in diesem Task ohne Befund).

- [ ] **Step 4: Verifikation.** `mvn install` (inkl. `loadcheck` **unverändert** grün — die festgenagelten Werte dürfen sich nicht ändern) und `mvn -f e2e/pom.xml verify` grün (inkl. `PluginLoadIT`). Startlog des E2E-Laufs auf `org.slf4j`-Fehler greppen → 0.

- [ ] **Step 5: Commit**
  `git commit -m "build: bump pf4j to 3.15.0 and switch to IndexedExtensionFinder"`

---

### Task 4: Bibliotheks-Bereinigung vor dem Flip (Pflichtliste Etappe 3 + opencsv + Hamcrest)

**Files:**
- Modify: `rdfbeans/src/main/java/org/cyberborean/rdfbeans/RDFBeanManager.java` (`getAll()`-Iterator), neue Klasse `rdfbeans/src/main/java/org/cyberborean/rdfbeans/exceptions/RDFBeanRuntimeException.java` (oder der passende bestehende Exception-Ordner — vorher prüfen)
- Modify: `rdfbeans/pom.xml:75-79` (eigene `rdf4j.version` entfernen; Wert aus Root übernehmen — Mechanik s. Step 2)
- Modify: `functions/pom.xml:14-17` (Pin 3.6.3 → zentrale Property)
- Modify: `common/src/test/java/.../SparqlJsonGuardTest.java` (Pro-Modul-Untergrenze)
- Modify: `triplestore/src/main/java/.../TriplestoreLauncher.java` (dangling `-httpPort`)
- Modify: `common/AgentUtil.java:307`, `pluginsystem/plugins/MappingPlugin/.../MappingUtil.java:140`, opencsv-Versionen in `common/pom.xml`, `executionservice/pom.xml`, `MappingPlugin/pom.xml`, `MQTTPlugin/pom.xml` (→ Root-dependencyManagement, eine Version)
- Modify: Root-`pom.xml` (Hamcrest 3.0 statt `hamcrest-core` 2.2, opencsv-Version)
- Test: bestehende rdfbeans-Tests + neue Tests je Punkt

**Interfaces:**
- Produces: `org.cyberborean.rdfbeans.exceptions.RDFBeanRuntimeException extends RuntimeException` (Konstruktor `(RDFBeanException cause)`); Root-Property `opencsv.version=5.12.0`.

Alle Punkte sind unter Boot 1.3.5/Java 11-Target lösbar und stammen aus der Ledger-Liste „ZWINGEND IN ETAPPE 4" — sie werden **vor** dem Flip erledigt, damit dieser frei von Fremdursachen bleibt. Jeder Punkt ist ein eigener Commit.

- [ ] **Step 1: rdfbeans `getAll()`** — Tests zuerst (JUnit 4, bestehende Testklasse von `RDFBeanManager`):
  - `getAllIteratorThrowsNoSuchElementWhenExhausted` — nach Erschöpfung (`hasNext()==false`) wirft `next()` `java.util.NoSuchElementException` (heute: `null` im `type == null`-Zweig).
  - `getAllWrapsUnmarshallingFailureInNamedRuntimeException` — ein nicht unmarshallbarer Eintrag liefert `RDFBeanRuntimeException` mit `getCause() instanceof RDFBeanException`.
  Dann Implementierung; Commit `fix(rdfbeans): honour the Iterator contract in getAll()`.

- [ ] **Step 2: eine RDF4J-Versionsquelle.** rdfbeans bekommt `ajan-parent` als Parent (eigene Koordinaten `org.cyberborean:rdfbeans:2.2` bleiben explizit gesetzt) und verliert `rdf4j.version`; `functions` nutzt `${org.eclipse.rdf4j.version}`. Prüfung: `mvn dependency:tree -pl rdfbeans,functions | findstr rdf4j` zeigt ausschließlich `5.3.1`; `grep -rn "3\.6\.3" --include=pom.xml .` trifft nur noch `e2e/pom.xml`. Falls das Erben von `ajan-parent` rdfbeans PMD/Checkstyle-Regeln aufzwingt, die bei Fremdcode rot werden: in rdfbeans `<skip>` für diese Plugins mit Kommentar „gevendorter Fremdcode". Commit `build: single rdf4j version source for rdfbeans and functions`.

- [ ] **Step 3: SparqlJsonGuardTest pro Modul.** Test `eachScannedModuleContributesFiles` — für jedes der fünf Module eigene Untergrenze (≥10 Dateien), Fehlermeldung nennt das Modul. Positivkontrolle: Pfad eines Moduls verfälschen → rot. Commit `test(common): per-module floor for the sparql-json guard`.

- [ ] **Step 4: Triplestore `-httpPort` ohne Wert.** Test (Launcher-Testklasse anlegen, falls keine existiert): `danglingHttpPortFailsFast` — Argument `--httpPort` bzw. `-httpPort` als letztes Argument ohne Wert führt zu `IllegalArgumentException` mit Text, der `httpPort` enthält (heute: stiller Fallback auf 8080 = Port des executionservice). Commit `fix(triplestore): reject a dangling httpPort argument`.

- [ ] **Step 5: opencsv 5.12.0 + Hamcrest 3.0.** Charakterisierungstests zuerst (TestNG in `common`): `csvRoundTripOfQuotedFieldsIsUnchanged` für `AgentUtil` (Eingabe mit Kommas, Anführungszeichen und Zeilenumbruch in einem Feld; Ergebnis-Modell vor und nach dem Bump identisch); für `MappingUtil` existiert kein Testmodul — die Änderung dort ist rein mechanisch (Builder statt Konstruktor) und wird über Compile + E2E abgesichert. `CsvValidationException` wird an der Aufrufstelle in die dort bereits geworfene Ausnahme übersetzt (Signatur der Methode bleibt). Commit `build: opencsv 5.12.0 and hamcrest 3.0`.

- [ ] **Step 6: Verifikation.** `mvn install` + `mvn -f e2e/pom.xml verify` grün, `loadcheck` unverändert grün.

---

### Task 5: SPARQL-Injection-Härtung (vorbestehend, Etappe-3-Befund)

**Files:**
- Modify: `common/src/main/java/de/dfki/asr/ajan/common/SPARQLUtil.java` (`queryRepository(Repository, ParsedQuery)`, `queryNamedGraph`, `queryModel(Model, String)`-Aufrufpfad)
- Modify: `executionservice/.../AgentModelManager.java:63,80,97-103` (nur falls die Härtung am Aufrufer ansetzt)
- Test: `common/src/test/java/de/dfki/asr/ajan/common/SPARQLUtilInjectionTest.java`

**Interfaces:**
- Produces: Jede von außen beeinflusste IRI, die in SPARQL-Text interpoliert wird, ist vorher syntaktisch als IRIREF validiert; ungültige IRIs führen zu einer Ausnahme statt zu einer Query.

Befund (Plan 2 „Nach diesem Plan", Whole-Branch-Review Etappe 3): `RenderUtils` setzt IRIs **unescaped** als `<…>` ein; `queryNamedGraph` baut `GRAPH <…>` per String; erreichbar über `AgentModelManager.java:63,80,98,103` mit IRIs aus clientgeliefertem Template-RDF. Wirkung: reine Offenlegung (ganzer Repository-Inhalt statt beschriebener Ressource).

**Prinzip statt Codevorgabe** (Lehre Etappe 3): Die Härtung muss an der Stelle greifen, an der die IRI zu Text wird. Erlaubte Mittel: Validierung gegen die IRIREF-Grammatik (keine Zeichen aus `<>"{}|^`\` und keine Steuer-/Leerzeichen; RDF4J bietet `ParsedIRI`), oder Umstellung auf Bindings/Algebra, wo der Pfad das hergibt. **Nicht** erlaubt: Blacklist einzelner Payloads, Escaping durch String-Replace.

- [ ] **Step 1: Angriff als Test reproduzieren (muss vor dem Fix rot sein).**
  - `describeWithInjectedIriDoesNotLeakRepository` — Repository mit zwei unverbundenen Ressourcen `ex:a` und `ex:secret`; Aufruf des Describe-Pfads mit einer IRI der Form `http://x/a> ?p ?o } UNION { ?s ?p ?o . FILTER(true) } #` (exakte Payload so wählen, dass sie gegen den **heutigen** Code tatsächlich `ex:secret` zurückliefert — im Report belegen). Assert: Ergebnis enthält `ex:secret` **nicht** (oder Aufruf wirft `IllegalArgumentException`).
  - `namedGraphWithInjectedIriDoesNotLeakOtherGraphs` — analog für `queryNamedGraph`.
  - `legitimateIrisStillWork` — gültige IRIs mit Query-String, Fragment und Prozent-Encoding liefern dasselbe Ergebnis wie vor dem Fix.
  Run: `mvn -pl common test -Dtest=SPARQLUtilInjectionTest` → die beiden Angriffstests ROT, `legitimateIrisStillWork` grün. Rot-Beleg in den Report.

- [ ] **Step 2: Härtung umsetzen** nach obigem Prinzip. Run wie oben → alle grün.

- [ ] **Step 3: Verifikation.** `mvn install` + E2E grün.

- [ ] **Step 4: Commit** `fix(common): validate IRIs before interpolating them into SPARQL text`

---

### Task 6: Kern-Flip — Spring Boot 4.1.2, Jakarta, Java 21 über den gesamten Reaktor

**Files (Hauptstellen; die mechanische javax→jakarta-Umstellung betrifft die 26 Dateien aus W9):**
- Modify: `pom.xml` (`maven.compiler.release=21`; dependencyManagement: `jakarta.ws.rs:jakarta.ws.rs-api:3.1.0`, `com.fasterxml.jackson:jackson-bom:2.21.7` (import), `javax.annotation:javax.annotation-api:1.3.2` (nur für MOSIM), Entfernen von `jaxrs-api 2.12.1`)
- Modify: `executionservice/pom.xml` (Parent `spring-boot-starter-parent:4.1.2`; Properties `java.version=21`, `jakarta-ws-rs.version=3.1.0`; Dependencies s. Step 2; `springloaded` raus; PMD `targetJdk` 21; Compiler-Literal `release 11` → `${java.version}`)
- Modify: `common/pom.xml`, `behaviour/pom.xml` (`jakarta.ws.rs-api` statt `jaxrs-api`), `common/AgentUtil.java:40`, `common/MimeUtil.java:32`, `behaviour/.../HttpConnection.java:34-35`
- Modify: alle 25 javax-Dateien im executionservice (W9) inkl. `SwaggerUIConfiguration.java` (`implements WebMvcConfigurer`)
- Modify: `executionservice/src/test/java/de/dfki/asr/ajan/rest/BeliefUpdateStallTest.java` (`@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)`; bleibt per Surefire ausgeschlossen)
- Modify: `executionservice/.../ExecutionServiceApplication.java` (Swagger-1.5-`@Import`/`BeanConfig` raus — Swagger kommt in Task 7; `CorsFilter`-Konfiguration so, dass Task-1-Tests grün bleiben)
- Delete: Swagger-1.5-Abhängigkeiten `io.swagger:swagger-jaxrs`; `ResteasyServletContext.java` (W7) — seine einzige Aufgabe war Swagger 1.5
- Modify: `pluginsystem/plugins/MOSIMPlugin/pom.xml` (`javax.annotation-api` explizit, `provided`), `pluginsystem/plugins/MQTTPlugin/pom.xml:206-210` (Scope, s. Step 5)
- Modify: `.github/workflows/ci-branch.yml` (falls Task 2 die JDK-11-Krücke eingeführt hat: entfernen)

**Interfaces:**
- Consumes: Task-1-Testmethoden (müssen grün bleiben außer den drei Swagger-Tests), `loadcheck` aus Task 3 (unverändert grün), alle Pins aus den Global Constraints.
- Produces: executionservice als Boot-4.1.2-Anwendung mit `@ApplicationPath("/ajan") public class ExecutionServiceApplication extends jakarta.ws.rs.core.Application`, als Spring-Bean registriert; Reaktor-Bytecode Java 21 (class major 65).

Dieser Task ist **unteilbar**: Boot 1.3/RESTEasy 3 bedienen nur `javax.ws.rs`, `common`/`behaviour` reichen `MultivaluedMap` an den executionservice durch, und die Plugins kompilieren gegen `behaviour`/`executionservice`. Der Reviewer prüft ihn als Ganzes; der Implementer darf lokal in Zwischen-Commits arbeiten, die am Ende zu **einem** Commit zusammengefasst werden (oder als eine Kette, deren jeder Commit kompiliert — kompiliert ein Zwischenstand nicht, wird gesquasht).

- [ ] **Step 1: Release 21 + Jakarta in den Bibliotheken.** Root `maven.compiler.release=21`; `common`/`behaviour` auf `jakarta.ws.rs-api` (nur Compile-Abhängigkeit für `MultivaluedMap`, kein RESTEasy); MOSIM `javax.annotation-api` explizit `provided` (W9). Prüfung: `mvn install -pl build-resources,rdfbeans,common,behaviour,functions,triplestore` grün; `javap -v` einer Klasse aus `common` zeigt `major version: 65`.

- [ ] **Step 2: executionservice auf Boot 4.1.2.** Dependencies am Ende **genau**: `spring-boot-starter-tomcat` (W3), `org.jboss.resteasy:resteasy-servlet-spring-boot-starter:6.3.0.Final`, `spring-boot-starter-webmvc` (für `CorsFilter`, `SwaggerUIConfiguration`-Resource-Handler und Redirect — Spring MVC bleibt, wie heute, neben RESTEasy), bestehende Fachabhängigkeiten (common, behaviour, libthrift, gdx-ai, oro, xercesImpl, opencsv, jep, zip4j, swagger-ui-Webjar vorerst unverändert 2.1.4), `spring-boot-starter-test` (test), TestNG/Hamcrest (test). Raus: `spring-boot-starter-undertow`, PayPal-Starter, `resteasy-spring:3.0.9.Final`, `swagger-jaxrs`, `springloaded`. Property `jakarta-ws-rs.version=3.1.0`.
  Prüfung: `mvn -pl executionservice dependency:tree` enthält **kein** `undertow`, kein `com.paypal`, kein `resteasy-jaxrs`, kein `javax.ws.rs`, `jakarta.ws.rs-api:3.1.0`, `tomcat-embed-core:11.0.27`, genau eine RESTEasy-Linie (`6.2.11.Final`).

- [ ] **Step 3: Code-Umstellung executionservice.** javax→jakarta in allen 25 Dateien; `SwaggerUIConfiguration implements WebMvcConfigurer` — `@EnableWebMvc` entfernen **nur**, wenn `rootRedirectsToSwaggerUi`/`swaggerUiIsServed` ohne es nicht schlechter werden als mit (beide sind im Rot-Fenster; maßgeblich ist hier, dass die Resource-Handler-Registrierung und der Redirect-Controller aktiv sind — im Startlog sichtbar); `BeliefUpdateStallTest` portieren (kompiliert, bleibt ausgeschlossen). Die RESTEasy-Registrierung läuft über den Starter im Modus `auto`; `ExecutionServiceApplication` ist `public` und eine Spring-Bean (W4). Alle `@Provider`- und `@Path`-Klassen sind bereits `@Component` (W4-Voraussetzung erfüllt — im Report bestätigen).

- [ ] **Step 4: CORS-Parität.** `corsSimpleRequest` und `corsPreflight` aus Task 1 müssen grün sein. Falls Task 1 gemessen hat, dass der Origin gespiegelt und `Access-Control-Allow-Credentials: true` gesendet wird, ist das unter Spring 6 nur mit `addAllowedOriginPattern("*")` + `setAllowCredentials(true)` zu erreichen (`addAllowedOrigin("*")` + Credentials wirft dort beim ersten Request) — das ist **die** zu prüfende Hypothese (W12); verifiziert wird sie ausschließlich durch die beiden Tests.

- [ ] **Step 5: Plugins.** Alle 10 Plugins kompilieren unter Release 21 gegen die neuen Kernmodule. MQTTPlugin (W17): `jar tf pluginsystem/deployments/MQTTPlugin-0.1-all.jar` vor und nach der Umstellung vergleichen; enthält das Fat-JAR Klassen aus `de/dfki/asr/ajan/executionservice` oder `org/springframework`, wird die Dependency auf `<scope>provided</scope>` gesetzt (der Host stellt diese Klassen bereit, wie bei MOSIM). Beleg (Klassenanzahl vorher/nachher) in den Report. `loadcheck` aus Task 3 muss **unverändert** grün bleiben — geänderte Erwartungswerte sind ein Stopp-Grund.

- [ ] **Step 6: Volle Verifikation.**
  Run: `mvn clean install` und `mvn -f e2e/pom.xml verify` (JDK 21).
  Expected: Reaktor grün; E2E: alle Tests grün **außer** `swaggerSpecIsServed`, `swaggerUiIsServed`, `rootRedirectsToSwaggerUi` (Rot-Fenster). Insbesondere grün: alle charakterisierten IST-Werte (Global Constraints), `malformedTurtleReturns400` mit `text/html; charset=UTF-8`, `jsonBodyWithCharsetIsAccepted`, `jsonBodyPlainIsAccepted`, `PluginLoadIT`.
  Startlog des Service: kein `RuntimeDelegate`-Fehler (Review Focus 4), die bekannte WARN `TypeNotPresentException …WebMvcAutoConfiguration` ist erlaubt und wird im Report als erwartet vermerkt.
  **Abweichende Content-Types oder Fehlerkörper** (z. B. der 400er bei malformed Turtle kommt als anderer Typ) werden durch einen eigenen `ExceptionMapper`/Provider auf den charakterisierten Wert zurückgebracht — **vorher** per systematic-debugging belegen, welche Ausnahme RESTEasy 6 dort wirft. Lässt es sich nicht mit eigenem JAX-RS-Code beheben: Umschaltpunkt Plan B (oben) — stoppen, Auftraggeber fragen.

- [ ] **Step 7: Commit**
  `git commit -m "build!: spring boot 4.1.2, jakarta, java 21 across the reactor (resteasy starter 6.3, tomcat)"`

---

### Task 7: Swagger auf OpenAPI 3 — Rot-Fenster schließen

**Files:**
- Modify: `executionservice/pom.xml` (`io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.55`, `org.webjars:swagger-ui:5.34.0` statt 2.1.4)
- Create: `executionservice/src/main/java/de/dfki/asr/ajan/executionservice/OpenApiSpecResource.java`
- Create: `executionservice/src/main/resources/static/swagger-ui/swagger-initializer.js` (oder Resource-Handler-Vorrang — s. Step 3)
- Modify: `executionservice/.../SwaggerUIConfiguration.java` (Webjar-Pfad 5.34.0; Redirect), Resource-Klassen mit `@Api`/`@ApiOperation` (`AgentExecutionService`, `AgentsService`, `AgentResource`) → `io.swagger.v3.oas.annotations` (`@Tag`, `@Operation`)
- Test: `e2e/.../WebSurfaceIT.java` (neuer Test `swaggerUiPointsToAjanSpec`)

**Interfaces:**
- Produces: `@Component @Path("/swagger.json") public class OpenApiSpecResource extends io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource` mit einer `@GET @Produces("application/json")`-Methode, die die OpenAPI-3-Spec der Ressourcen in `de.dfki.asr.ajan.rest` liefert (Resource-Package über `SwaggerConfiguration#resourcePackages`).

- [ ] **Step 1: Test schreiben.** `swaggerUiPointsToAjanSpec` — `GET /swagger-ui/swagger-initializer.js` → 200, Body enthält `/ajan/swagger.json` und **nicht** `petstore`. Run → rot (Datei liefert heute die Webjar-Defaults bzw. 404).

- [ ] **Step 2: OpenAPI-3-Spec unter `/ajan/swagger.json`** über `OpenApiSpecResource`; Annotationen auf v3 umstellen (Inhalt der Beschreibungen 1:1 übernehmen). Prüfung: `swaggerSpecIsServed` grün, Body enthält `"openapi"` und `"/agents"`.

- [ ] **Step 3: Swagger-UI 5.34.0.** Resource-Handler auf `classpath:/META-INF/resources/webjars/swagger-ui/5.34.0/`; ein eigenes `swagger-initializer.js` mit `url: "/ajan/swagger.json"` muss **vor** der Webjar-Datei gefunden werden (zweite Resource-Location mit Vorrang oder eigener Handler nur für diese Datei). Redirect `GET /` → `/swagger-ui/index.html`.
  Prüfung: `swaggerUiIsServed`, `rootRedirectsToSwaggerUi`, `swaggerUiPointsToAjanSpec` grün.

- [ ] **Step 4: Browser-Gegenprobe** (E2E ist für das Rendern blind — Lehre Etappe 3): Service lokal starten, `http://localhost:8080/` im Browser öffnen (Chrome-DevTools-MCP, falls verfügbar; sonst `curl` auf `index.html` + `swagger-initializer.js` + eine CSS/JS-Datei mit Content-Type-Prüfung), belegen: UI lädt, zeigt die AJAN-Endpunkte (`/agents`), Konsole ohne 404. Screenshot oder Log in den Report.

- [ ] **Step 5: Verifikation.** `mvn install` + `mvn -f e2e/pom.xml verify` **vollständig** grün (Rot-Fenster geschlossen).

- [ ] **Step 6: Commit** `feat(executionservice): serve openapi 3 at /ajan/swagger.json with swagger-ui 5`

---

### Task 8: Abschluss — Doku, Spec-Korrekturen, Ledger

**Files:**
- Modify: `docs/superpowers/specs/2026-07-11-java-modernization-design.md` (Etappe 4 „abgeschlossen" mit CI-Run-ID; Boot 4.1.2; Korrektur „Starter bringt keine Transitiven" (W3); offene Fragen 1, 2, 6 als beantwortet markieren; Risiken 1, 4, 6 aktualisieren)
- Modify: `docs/superpowers/specs/2026-07-11-spike-results.md` (Korrekturvermerk zu Spike B, W3)
- Modify: `README.md` (Java-21-Voraussetzung, Swagger-URL), Start-Skripte nur falls sie JVM-Optionen enthalten, die unter 21 nicht mehr gelten
- Modify: `.superpowers/sdd/progress.md`

- [ ] **Step 1:** Spec und Spike-Ergebnisse wie oben nachziehen; jede Korrektur mit Datum und Verweis auf den Task.
- [ ] **Step 2:** Ledger: Etappe-4-Abschluss mit Branch-Head, `mvn install`-Modulzahl, E2E-Summenzeile, CI-Run-ID; Liste „offen für Plan 4 (Etappen 5/6)": `docker-image.yml` + Dockerfile auf Temurin 21 (das Image ist bis dahin **nicht** lauffähig, weil das JRE 11 Java-21-Bytecode nicht lädt — der Workflow läuft nur auf `master`, das Risiko ist damit eingegrenzt); MQTTPlugin-Manifest (W17); `-war-exec`-Filter; Jasper/JSP-Gegenprobe; `BeliefUpdateStallTest`; `behaviour` `skipTests` und die 4 vorbestehenden Testfehler; pf4j 3.16; THIRDPARTYLICENSE-Neugenerierung; manueller AJAN-editor-Gegentest (Spec Abschnitt 7) — braucht den Auftraggeber.
- [ ] **Step 3:** `mvn install` + E2E final grün, Push, CI grün.
- [ ] **Step 4: Commit** `docs: close etappe 4 (boot 4.1.2, jakarta, java 21, pf4j 3.15, openapi 3)`

---

## Abhängigkeiten und Parallelisierung

Task 1 → Task 2 → {Task 3, Task 4, Task 5} → Task 6 → Task 7 → Task 8.

Tasks 3, 4 und 5 berühren disjunkte Dateien, teilen sich aber die E2E-Ports 8080/8090 und den lokalen `~/.m2`-Stand der Reaktor-Artefakte. Sie werden deshalb **sequenziell** ausgeführt (Reihenfolge 3 → 4 → 5); parallel nur die Code-Arbeit in getrennten Worktrees, wenn die Verifikationsläufe nacheinander stattfinden.

## Nach diesem Plan

- **Whole-Branch-Review Etappe 4** (Muster: Etappen 0–3). Schwerpunkte: (a) Ist die HTTP-Oberfläche wirklich identisch — inkl. der Browser-Aspekte, die E2E nicht sieht (CORS-Antworten im echten Browser, Swagger-UI)? (b) Hat der Flip Verhalten verschoben, das kein Test festhält (Container-Defaults Undertow → Tomcat: maximale Header-Größe, URL-Encoding von `;` und `%2F` in Pfaden, Keep-Alive)? (c) Ist das `provided`-Handling der Plugins sauber (keine Spring/executionservice-Klassen in Plugin-Fat-JARs)? (d) Sind alle Ledger-Pflichtpunkte aus Etappe 3 erledigt?
- **Plan 4** (Etappe 5 Deployment/Feinschliff + Etappe 6 Java 25) wird erst danach geschrieben.
