# Design: Modernisierung AJAN-service auf Java 21/25

**Datum:** 2026-07-11
**Status:** Entwurf zur Review — **aktualisiert am 2026-07-31 mit den Ergebnissen der Spikes A–D (Etappe 2)**; die betroffenen Stellen sind unten mit dem jeweiligen Spike gekennzeichnet, Belege in `docs/superpowers/specs/2026-07-11-spike-results.md`.
**Scope:** Gesamtes Multi-Modul-Projekt AJAN-service (21 Maven-Module inkl. Pluginsystem)

## 1. Ausgangslage

| Bereich | Ist-Zustand | Problem |
|---|---|---|
| Java | Parent-POM: Java 8 target; executionservice: 11; Docker-Runtime: Temurin 11 JRE | inkonsistent, veraltet |
| Spring Boot | 1.3.5.RELEASE (2016) im executionservice | 8 Major-Versionen hinter aktuell |
| JAX-RS | RESTEasy 3.0.9 über den nicht mehr gepflegten PayPal-Starter (`com.paypal.springboot:resteasy-spring-boot-starter`); ~100 `javax.*`-Imports in 26 Dateien | javax→jakarta-Migration nötig |
| RDF4J | 3.6.3 im Code, Server-WAR gemischt mit 4.0.0 | aktuell ist 5.x; Versions-Mix |
| triplestore | `tomcat8-maven-plugin` (Projekt retired) baut exec-WAR mit RDF4J-Server + Workbench; tote Plugin-Repositories (icm, alfresco) | Packaging-Mechanismus muss ersetzt werden |
| Pluginsystem | pf4j 3.6.0; 10 Plugins (ASP, Mapping, MOSIM, MQTT, OPCUA, Python/JEP, RMLMapping, ScriptExecutor, STRIPS, StandardBTNodes); Fat-JAR-Deployment | pf4j-Upgrade + Jakarta betrifft alle Plugins |
| rdfbeans | Git-Submodul, zeigt auf inaktives Upstream `cyberborean/rdfbeans` | für RDF4J 5 muss der Code gepatcht werden; auf Upstream nicht pushbar |
| Sonstiges | TestNG 6.9, Swagger 1.5 (OpenAPI 2), libthrift 0.15, gdx-ai 1.8.2, findbugs-Maven-Plugin, maven-jar-plugin 2.6 u.a. | veraltete Build-Plugins und Dependencies |

Unkritisch auf modernen JDKs: gdx-ai (Behavior-Tree-Kern, pure Java, 1.8.2 ist die letzte Release-Version) und JEP (4.2.2 unterstützt Java 21).

## 2. Getroffene Entscheidungen

1. **Java-Ziel:** Migration auf **Java 21** als stabiler Meilenstein; anschließend Bump auf **Java 25** als letzte, risikoarme Etappe.
2. **Spring Boot:** direkt auf **4.x** (konkret 4.1.0). Kein Fallback auf 3.5: Falls der JAX-RS-Port mit Boot 4 nicht funktioniert, wird stattdessen auf Spring MVC umgebaut. *(Nach Spike B: JAX-RS-Port ist der geplante Weg; MVC bleibt nur noch als Plan B für den Fall, dass der volle REST-Layer scheitert.)*
3. **REST-Layer:** **JAX-RS-Port** über den offiziellen RESTEasy-Spring-Boot-Starter (`org.jboss.resteasy:resteasy-servlet-spring-boot-starter:6.3.0.Final` **plus** `spring-boot-starter-web` — der Starter bringt keine transitiven Abhängigkeiten mit). *Entschieden durch Spike B (grün auf Boot 4.1.0 + JDK 21).* Der Port ist **kein reiner Import-Rename**: er umfasst (a) Ablösung des toten PayPal-Starters `com.paypal.springboot:resteasy-spring-boot-starter:2.0.0-RELEASE` und von `org.jboss.resteasy:resteasy-spring:3.0.9.Final`, (b) Umbau von `ResteasyServletContext.java`, weil `@org.jboss.resteasy.annotations.interception.ServerInterceptor` in den geprüften RESTEasy-6-Jars (`resteasy-core`/`resteasy-core-spi:6.2.11.Final`) nicht mehr enthalten ist — für die entschiedene Starter-Linie `6.3.0.Final` nicht gegengeprüft — und `ResteasyProviderFactory` nur noch unter `jakarta.*` existiert; welche JAX-RS-2.1-Registrierung an die Stelle tritt, ist offen, (c) javax→jakarta über die ~26 REST-Klassen. Plan B bleibt der Umbau dieser Klassen inkl. Custom-Provider auf `@RestController`. Die öffentliche HTTP-API bleibt in beiden Fällen identisch (AJAN-editor-Kompatibilität).
4. **Triplestore:** eigenes **Embedded-Tomcat-Launcher-Modul** (Jakarta-Linie), das die offiziellen RDF4J-5.x-Server- und Workbench-WARs unter `/rdf4j` und `/workbench` deployed; gleiche CLI (`java -jar triplestore.jar --httpPort=8090`). Der Kontextpfad `/rdf4j` ist **normativ gesetzt** und bleibt der heutige (Kompatibilität mit bestehenden Konfigurationen und Start-Skripten); dass das offizielle `eclipse/rdf4j-workbench`-Image stattdessen `/rdf4j-server` verwendet (s. Spike A), betrifft nur das Image, nicht dieses Launcher-Modul — der Client bindet an eine konfigurierbare Basis-URL.
5. **RDF4J:** überall einheitlich **5.x**, Version zentral im Parent-POM (`org.eclipse.rdf4j.version`). Der Client-Umstieg 3.6.3 → 5.x im Produktcode gehört zu Etappe 3, nicht zum Kern-Sprung (Spike A; Regel-Override s. Etappe 3). Weil die Version zentral verwaltet ist, umfasst der Bump **nicht nur** rdfbeans/common/behaviour/functions, sondern zwingend auch das Pluginsystem: 9 POMs mit `rdf4j-model`/`rdf4j-repository-api` und 75 Java-Dateien in allen 10 Plugins.
6. **Verifikation:** vor jeder Änderung wird eine **E2E-Smoke-Suite** gebaut (abgeleitet aus der Postman-Collection) und in CI verankert; sie definiert „läuft wie bisher".
7. **Plugin-Scope:** **alle 10 Plugins** werden vollständig migriert.
8. **rdfbeans:** Submodul auflösen, Quellcode als normales Maven-Modul **in das Repo vendoren** (LGPL-Lizenzheader bleiben erhalten), dort für RDF4J 5 patchen. `.gitmodules` entfällt.
9. **Branch-Isolation:** gesamte Arbeit auf einem Epic-Branch; `master`, öffentliche Releases und das Docker-Hub-Image bleiben bis zur bewussten finalen Freigabe unberührt (Details Abschnitt 5).

## 3. Zielbild (Endzustand)

| Baustein | Ziel |
|---|---|
| Java | Alle Module einheitlich Java 21 (target nur im Parent-POM, keine lokalen Overrides); Abschluss-Etappe: 25. Docker-Basis `eclipse-temurin:21-jre-noble` (später 25). Die glibc-Constraints wegen embedded Python/libjep bleiben bestehen (kein Alpine, Noble-Basis beibehalten). |
| executionservice | Spring Boot 4.x als Parent, Jakarta-Namespaces. REST per offiziellem RESTEasy-Starter (`resteasy-servlet-spring-boot-starter:6.3.0.Final` + `spring-boot-starter-web`); *entschieden durch Spike B*, MVC nur noch als Plan B. Servlet-Container: **Tomcat** (`spring-boot-starter-tomcat`); *entschieden durch Spike C — Undertow wird von `spring-boot-dependencies:4.1.0` nicht mehr verwaltet*. |
| triplestore | Neues Launcher-Modul: kleiner Main + Embedded Tomcat (Jakarta), deployed RDF4J-5.x-Server- und Workbench-WARs; Ports/Pfade/CLI identisch. `tomcat8-maven-plugin` und tote Plugin-Repos entfernt. |
| RDF4J | 5.x überall, eine Version im Parent verwaltet. |
| Pluginsystem | pf4j **3.15.0** (*Spike D*); alle 10 Plugins auf Jakarta/RDF4J 5/Java 21; Fat-JAR-Deployment-Mechanismus (JarPluginLoader, `deployments/`-Assembly) bleibt unverändert — *durch Spike D bestätigt, aber nur für den getesteten Pfad*: `loadPlugins()` unter JDK 21 mit dem Stock-`DefaultPluginManager` (`state=RESOLVED`). **Ungetestet** bleiben Plugin-Start (`startPlugins()`), Extension-Auflösung über den in `AJANPluginLoader` überschriebenen `createExtensionFinder()`/`LegacyExtensionFinder` und das `provided`-Scope-Handling (s. Risiko 4). Bekannte Loader-Anpassung: `LegacyExtensionFinder` → `IndexedExtensionFinder` in `AJANPluginLoader.java`. |
| rdfbeans | Gevendortes Maven-Modul im Repo, gepatcht für RDF4J 5. |
| Swagger | `swagger-jaxrs` 1.5 → swagger-core 2.x (jakarta, OpenAPI 3) + aktueller swagger-ui-Webjar. Docs-Endpunkt bleibt erreichbar; Formatwechsel auf OpenAPI 3 ist akzeptiert. |
| Sonstige Deps | Lombok aktuell, TestNG 7.x, Hamcrest 3, opencsv/zip4j/libthrift aktuell (libthrift ggf. gepinnt, s. Risiko 7); gdx-ai bleibt 1.8.2. Maven-Plugins: findbugs entfernt, compiler/surefire/jar/checkstyle/pmd aktuell. |
| Nicht-Ziele | Keine Änderungen der öffentlichen HTTP-API; kein MVC-Umbau ohne Not; kein Ersatz von rdfbeans; keine neuen Features; keine Änderung des Plugin-Deployment-Formats. |

## 4. Vorgehen: Etappen mit stets grünem Build

Gewählter Ansatz: **inkrementelle Etappen** (statt Big Bang oder Strangler-Neuaufbau). Jede Etappe endet mit grünem Build **und** grüner E2E-Suite und ist einzeln revertierbar.

### Etappe 0 — Sicherheitsnetz
E2E-Smoke-Suite als eigenes Testmodul oder Skript + CI-Job gegen den **Ist-Zustand**:
- Projekt bauen, Triplestore + executionservice wie in `startAll` starten, Healthcheck abwarten.
- Kernflüsse aus der Postman-Collection: Repositories abfragbar; Use-Case-Agenten geladen; Agent per REST instanziieren; Event/Endpoint ansprechen; Agentenausführung beobachten; Ergebnis-RDF im Triplestore prüfen; Agent löschen.
- Plugin-Ladecheck: alle im jeweiligen Deployment enthaltenen Plugins erscheinen beim Start (Log/API). Achtung: lokal/Entwicklung sind es 10; der CI-Workflow paketiert heute nur 8 ins dist/Docker-Image (OPCUAPlugin und ScriptExecutorPlugin fehlen dort). Der Check erwartet je Umgebung die passende Menge; ob OPCUA/ScriptExecutor bewusst fehlen, wird in Etappe 5 geklärt.
Die Suite läuft ab dann in CI gegen jeden Etappen-Stand.

### Etappe 1 — Build-Hygiene (keine Framework-Sprünge)
- rdfbeans vendoren (Submodul raus, Quellen als Modul rein, `.gitmodules` löschen).
- findbugs- und pegdown-javadoc-Plugin entfernen; jar-Plugin aktualisieren; Surefire pinnen. (Checkstyle/PMD-Modernisierung erst in Etappe 4, wenn der JDK-Wechsel sie erzwingt; die toten Plugin-Repositories im triplestore-POM können erst in Etappe 3 raus — das retired tomcat8-maven-plugin wird daraus aufgelöst.)
- RDF4J-Versions-Mix dokumentieren; Vereinheitlichung erfolgt vollständig mit dem Triplestore-Neubau (Etappe 3) — nach Spike A gehört auch der Client-Umstieg dorthin und nicht mehr in den Kern-Sprung.
- Alle Module einheitlich auf Java 11 target (Ist-Docker-Runtime).
- Harmlose Dependency-Bumps (TestNG, Hamcrest; ungenutztes jcommander entfernen). opencsv 4→5 ist ein API-Bruch und wandert in Etappe 4; zip4j ist bereits aktuell.
- Riskante Bausteine (Boot, RDF4J-Major, pf4j) bleiben unangetastet.

### Etappe 2 — Spikes (je ~1 Tag timeboxed) — **abgeschlossen**
Ergebnisse und Belege: `docs/superpowers/specs/2026-07-11-spike-results.md`, Abschnitt „Konsequenzen fuer die Folgeplaene".
- **Spike A:** RDF4J-3.6-Client gegen RDF4J-5-Server (REST-Protokoll-Kompatibilität) → **rot** (10/16 E2E, Bruch in `Repositories.graphQuery`). Die hier vorregistrierte Regel lautete „falls inkompatibel → Triplestore wandert in Etappe 4"; sie wurde **bewusst überschrieben** (Auftraggeber-Bestätigung 2026-07-31): Etappe 3 bleibt eigenständig und bekommt den Client-Umstieg dazu. Begründung s. Etappe 3.
- **Spike B:** offizieller RESTEasy-Spring-Boot-Starter unter Boot 4 lauffähig? → **grün für den Minimalpfad** → JAX-RS-Port.
- **Spike C:** Undertow unter Boot 4 verfügbar? → **rot** → Tomcat-Starter.
- **Spike D:** pf4j-Zielversion; Fat-JAR-Classloading unter Java 21 im Deployment-Modus (wie im Docker-Image) → **grün** mit pf4j 3.15.0.

### Etappe 3 — Triplestore-Neubau **inkl. RDF4J-Client-Umstieg** (eigenständig; vorregistrierte Regel bewusst überschrieben)

**Regel-Override, ausdrücklich bestätigt.** Für den Fall „Spike A rot" war oben und in Risiko 3 vorregistriert: *der Triplestore-Neubau wandert in den Kern-Sprung (Etappe 4)*. Spike A ist rot — und diese Regel wird trotzdem **nicht** angewendet, sondern bewusst überschrieben: Etappe 3 bleibt eigenständig und wird stattdessen um den Client-Umstieg erweitert. **Der Auftraggeber hat den Override am 2026-07-31 ausdrücklich bestätigt** (Option „Etappe 3 erweitern").

**Begründung des Overrides:** Der RDF4J-Bruch soll vom Boot-4-/Java-21-Sprung **isoliert** bleiben. Fielen beide in Etappe 4 zusammen, kämen bei einer roten E2E-Suite gleichzeitig RDF4J-Major, Boot-Major, Jakarta-Namespace, Java-21-Bump und Container-Wechsel als Ursache in Frage; als eigene Etappe ist ein Fehlschlag eindeutig dem RDF4J-Umstieg zuzuordnen und einzeln revertierbar. (Die Etappen-Invariante „jede Etappe endet grün" allein trägt diese Wahl **nicht** — sie schließt nur aus, dass Etappe 3 ein bloßer Server-WAR-Tausch bleibt, weil Spike A das Abnahmekriterium „alter Service gegen neuen Triplestore" widerlegt hat. Zwischen „Etappe 3 erweitern" und „alles nach Etappe 4" entscheidet die Isolierbarkeit der Fehlerursache.)

**Umfang:**
1. Neues Launcher-Modul mit Embedded Tomcat + RDF4J-5-WARs; Ports/CLI identisch, Kontextpfad bleibt normativ `/rdf4j`; `tomcat8-maven-plugin` und tote Plugin-Repos raus; Start-Skripte und Dockerfile angepasst.
2. RDF4J-Client 3.6.3 → 5.x im Produktcode inkl. der API-Anpassungen an `Repositories`/`RepositoryConnection`. **Reichweite:** Die Version ist zentral im Parent-POM verwaltet (`org.eclipse.rdf4j.version`), der Bump zieht deshalb zwingend mit: rdfbeans (gevendort), common, behaviour, functions **und das gesamte Pluginsystem** — 9 POMs deklarieren `rdf4j-model`/`rdf4j-repository-api` (`pluginsystem/pom.xml` + 8 Plugin-POMs), 75 Java-Dateien in **allen 10** Plugins importieren `org.eclipse.rdf4j.*` (OPCUA und ScriptExecutor erben die Dependency, ohne sie zu deklarieren). Etappe 3 ist damit keine Zwei-Modul-Etappe.
3. Gezielte Testabdeckung für die von Spike A gefundene Bruchstelle (SPARQL-GraphQuery über `common/.../SPARQLUtil.java`), zusätzlich zur E2E-Suite; Plugin-Compile und `PluginLoadIT` gehören zum Abnahmekriterium.
4. **Bewusste Entscheidung zum RDF4J-Pin der E2E-Suite:** `e2e/pom.xml:30-45` pinnt `rdf4j-rio-turtle`/`rdf4j-model` (test-scope) auf 3.6.3 mit der Begründung „gleiche RDF4J-Linie wie das Projekt" — nach dem Client-Umstieg stimmt das nicht mehr. Etappe 3 muss explizit entscheiden: mitziehen auf 5.x oder bewusst auf 3.6.3 lassen (die Suite testet nur von außen und soll alle Etappen unverändert überleben; ein alter Rio-Parser bleibt dafür ggf. das robustere Werkzeug). In jedem Fall ist der veraltete Kommentar zu korrigieren.

Nicht in Etappe 3: alles Nicht-RDF4J am Pluginsystem (pf4j-Bump, Jakarta, Java 21) — das bleibt Etappe 4.

Die Etappe bleibt auf **Java 11** und Boot 1.3. Die Java-Version **steht dem nicht entgegen** (RDF4J 5.x verlangt minimal Java 11) — das ist aber nur die notwendige, nicht die hinreichende Bedingung. Zwei Punkte sind **nicht durch einen Spike belegt** und von Plan 2 zuerst zu klären: (a) Verträglichkeit der RDF4J-5-Transitiven (Jackson, httpclient, slf4j) mit dem Boot-1.3-Dependency-Management; (b) die konkrete Embedded-Tomcat-Version, die unter Java 11 läuft (Tomcat 11 verlangt Java 17). **Scheitert (a), wird Etappe 3 doch mit Etappe 4 zusammengelegt** — diese Rückfallebene ist vom Auftraggeber mitbeschlossen.

### Etappe 4 — Kern-Sprung (ein Branch, modulweise sequenziert)
Reihenfolge entlang des Dependency-Graphen. RDF4J ist zu diesem Zeitpunkt bereits auf 5.x (s. Etappe 3) — die betroffenen Module bleiben aber in der Sequenz, weil ihr **restlicher** Umfang (Java 21, Jakarta, opencsv 4→5) hier anfällt:
1. Parent-POM: Java 21, zentrale Versions-Properties.
2. rdfbeans → common → behaviour + functions: **Restumfang ohne RDF4J** — Java-21-Kompatibilität und javax→jakarta. Konkrete Belegstellen für den Jakarta-Teil: `common/src/main/java/de/dfki/asr/ajan/common/AgentUtil.java:40` und `common/src/main/java/de/dfki/asr/ajan/common/MimeUtil.java:32` (`javax.ws.rs.core.MultivaluedMap`), `behaviour/src/main/java/de/dfki/asr/ajan/behaviour/service/impl/HttpConnection.java:34-35` (`javax.ws.rs.core.MultivaluedHashMap`/`MultivaluedMap`).
3. executionservice: Boot 4.1.0, Jakarta, Servlet-Container Tomcat statt Undertow, JAX-RS-Port auf `resteasy-servlet-spring-boot-starter:6.3.0.Final` + `spring-boot-starter-web` (PayPal-Starter und `resteasy-spring:3.0.9.Final` raus, `ResteasyServletContext.java` umbauen — `@ServerInterceptor` ist in den geprüften RESTEasy-6-Jars `resteasy-core`/`resteasy-core-spi:6.2.11.Final` nicht mehr enthalten, **für die entschiedene Starter-Linie `6.3.0.Final` aber nicht gegengeprüft**; dass die Annotation dort wieder existiert, ist unwahrscheinlich, aber unbelegt — die Ersatzmechanik ist offen), Swagger 2.x. Plan B bei Scheitern: MVC-Umbau.
4. pluginsystem/loader: pf4j 3.15.0, `LegacyExtensionFinder` → `IndexedExtensionFinder`.
5. Alle 10 Plugins: Jakarta + Java 21 (der RDF4J-Anteil ist in Etappe 3 erledigt).

javax→jakarta mechanisch (OpenRewrite-Rezepte als Werkzeug, kein Muss). Merge-Kriterium: alles kompiliert, Unit-Tests grün, E2E-Suite grün.

### Etappe 5 — Deployment & Feinschliff
Dockerfile auf Temurin 21; CI-Workflow angepasst; README/Start-Skripte aktualisiert; ausgeschlossenen `BeliefUpdateStallTest` neu bewerten (fixen oder dokumentiert draußen lassen); restliche Dependency-Updates.

### Etappe 6 — Java 25
Target-Bump im Parent, Docker-Basis auf 25, E2E-Suite entscheidet.

## 5. Branching- und Release-Sicherheit

Der bestehende Workflow `docker-build-and-publish` triggert **nur auf Push nach `master`** und published dann Docker-Hub-Image (`aantakli/ajan-service:latest` + sha-Tag) und GitHub-Release. Daraus folgt:

- **Epic-Branch** (`modernization/java21`): sämtliche Arbeit passiert dort; Etappen als sequenzielle PRs/Sub-Branches gegen den Epic-Branch. `master` wird nicht angefasst.
- **Branch-CI ohne Publish:** neuer, separater Workflow (z.B. `ci-branch.yml`) für den Epic-Branch: nur bauen + E2E-Suite; kein Docker-Login, kein Push, kein Release-Job. Versehentliches Überschreiben von `latest` ist damit strukturell ausgeschlossen. Der bestehende Publish-Workflow bleibt unverändert.
- **Merge nach `master` = bewusster Release-Akt** am Ende des Epics. Vor dem Merge wird der letzte alte Stand getaggt (z.B. `pre-java21`), damit das letzte Java-11-Release dauerhaft auffindbar bleibt.
- Lokale Docker-Tests nutzen ausschließlich lokale Tags (z.B. `ajan-service:dev`), nie `aantakli/ajan-service`.

## 6. Risiken

1. **RESTEasy-Starter × Boot 4** — größtes Einzelrisiko; durch Spike B **teilweise entschärft**: der Minimalpfad (ein `@Path`-GET) läuft grün auf Boot 4.1.0 + JDK 21. **Restrisiko bleibt** für den vollen REST-Layer (Provider, ExceptionMapper, Filter/Interceptor, Content-Negotiation) — der Starter ist offiziell gegen Boot 3.4 gebaut und `ResteasyAutoConfiguration` ist unter Boot 4 nur teilweise auswertbar (WARN `TypeNotPresentException: ...WebMvcAutoConfiguration not present`). Plan B (MVC-Umbau) bleibt beschlossen; Plan 3 braucht einen expliziten Umschaltpunkt.
2. **RDF4J 3.6 → 5 API-Bruch** in rdfbeans/common/behaviour/functions **und im gesamten Pluginsystem** (entfernte deprecated APIs, geändertes Iteration-/Transaction-Handling) — mechanisch, aber breit: 9 POMs und 75 Java-Dateien in allen 10 Plugins hängen über das zentrale `org.eclipse.rdf4j.version` daran. Pro Modul mit Unit-Tests absichern, Plugin-Seite zusätzlich über `PluginLoadIT`. Das Risiko fällt nach dem Override in **Etappe 3**, nicht in Etappe 4.
3. **RDF4J-REST-Protokoll** alt-Client ↔ 5er-Server — **eingetreten** (Spike A: 10/16 E2E rot, Bruch bei SPARQL-GraphQuery, reines Repository-Listing funktioniert). Die hier vorregistrierte Konsequenz („wandert in den Kern-Sprung") wurde **bewusst überschrieben und vom Auftraggeber bestätigt** (2026-07-31): stattdessen wird Etappe 3 um den Client-Umstieg erweitert, um den RDF4J-Bruch vom Boot-4-/Java-21-Sprung isoliert diagnostizierbar zu halten (s. Etappe 3). Damit wird Risiko 2 zeitlich vorgezogen. Rückfallebene, falls RDF4J 5 unter Boot 1.3 transitiv nicht trägt: Etappe 3 und 4 doch zusammenlegen. (Heute läuft bereits gemischt: 3.6.3-Client gegen 4.0.0-Server-WAR.)
4. **pf4j-Classloading** — durch Spike D **teilweise entschärft**: pf4j 3.15.0 löst das reale Fat-JAR-Deployment (`StandardBTNodes-0.1-all.jar`, `pf4j.mode=deployment`) unter JDK 21 unverändert auf (`state=RESOLVED`). **Geltungsbereich des Belegs:** Der Spike (`spikes/pf4j-java21/src/main/java/spike/PluginLoadCheck.java`) ruft ausschließlich `loadPlugins()` und liest `getPluginState()` — mit dem Stock-`DefaultPluginManager`. **Restrisiko bleibt** für: (a) `startPlugins()`, (b) die Extension-Auflösung (`getExtensions(...)`) über den in `pluginsystem/loader/.../AJANPluginLoader.java` **überschriebenen** `createExtensionFinder()` mit `LegacyExtensionFinder` — genau das Bauteil, das die Umbenennung nach `IndexedExtensionFinder` betrifft und das der Spike **nicht** benutzt hat, (c) das `provided`-Scope-Handling der Plugin-Builds (in der Vorfassung dieses Risikos ausdrücklich genannt, von Spike D nicht getestet; offene Frage 6 in den Spike-Ergebnissen). Der pf4j-Schritt in Etappe 4 ist deshalb **nicht** als reiner Versions-Bump zu budgetieren; der Ladecheck über den echten `AJANPluginLoader` (`PluginLoadIT`) ist der eigentliche Beleg.
5. **PythonPlugin/JEP** — natives libjep + embedded Python 3.13; Docker-Basiswechsel wird per E2E-Python-Check verifiziert; glibc-Anforderungen (Noble) bleiben.
6. **Implizites Verhalten von Boot 1.3** (Property-Binding-Regeln, Undertow-Defaults, Servlet-Init-Reihenfolge) — Netz ist die E2E-Suite; zusätzlich beim Kern-Sprung die effektive Property-Auswertung diffen. Durch die Spike-C-Entscheidung ist der **Container-Wechsel Undertow → Tomcat jetzt sicher** und nicht mehr optional: heutige Undertow-spezifische Defaults und `server.undertow.*`-Properties müssen in Etappe 4 gezielt auf Tomcat-Äquivalente geprüft werden.
7. **MOSIM/libthrift** — Thrift-Bump kann Wire-Kompatibilität zu externen MOSIM-Gegenstellen betreffen; falls MOSIM aktiv gegen externe Systeme läuft, wird libthrift bewusst gepinnt.

## 7. Verifikation

- **Pro Etappe:** `mvn install` grün + E2E-Smoke-Suite grün (lokal und im Branch-CI).
- **Kern-Sprung zusätzlich:** Unit-Tests pro migriertem Modul; Plugin-Ladecheck (alle 10 im Entwicklungsmodus); manueller Gegentest mit dem AJAN-editor gegen die laufende Instanz.
- **Abschluss:** Docker-Image lokal bauen (`ajan-service:dev`), Container-Healthcheck + E2E-Suite gegen den Container.
