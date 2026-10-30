# Modernisierung Plan 4: Deployment & Feinschliff (Etappe 5) und Java 25 (Etappe 6)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Den Epic-Branch release-fähig machen: lauffähiges Docker-Image mit allen 10 Plugins im Deployment-Modus, alle Unit-Tests wieder aktiv, statische Analyse auf PMD 7, Lizenzdateien generiert, offene Restpunkte erledigt — und abschließend der Sprung auf Java 25, verifiziert gegen das gebaute Docker-Image.

**Architecture:** Erst werden die Bausteine des Deployments repariert (Plugin-Manifest, Fat-JAR-Inhalte, Tests, Analyse, Lizenzen) — parallelisierbar, weil disjunkt. Dann wird die Dist-Zusammenstellung aus dem Publish-Workflow in ein Skript gezogen, das lokal und in CI identisch läuft, und die E2E-Suite bekommt einen Modus gegen einen laufenden Container. Damit wird das Image zum ersten Mal Ende-zu-Ende geprüft (Deployment-Modus, Python-Plugin). Java 25 kommt zuletzt und wird mit genau demselben Container-Gate abgenommen.

**Tech Stack:** Java 21 → 25, Spring Boot 4.1.2, pf4j 3.16.0, maven-pmd-plugin 3.28.0 (PMD 7), Mockito 5, license-maven-plugin, Docker (`eclipse-temurin:21-jre-noble` → `25-jre-noble`, Build-Container `maven:3.9-eclipse-temurin-25`).

**Spec:** `docs/superpowers/specs/2026-07-11-java-modernization-design.md` (Etappe 5 inkl. „Aus Etappe 4 übernommen“, Etappe 6, Abschnitt 5 Branching/Release, Abschnitt 7 Verifikation)
**Vorgänger-Ledger:** `.superpowers/sdd/2026-10-30-modernization-plan-3-etappe-4/progress.md` (Rulings R1–R12)

---

## Vorbefunde (verifiziert 2026-10-30, Branch-Head `230b898d`)

| # | Befund |
|---|---|
| V1 | `Dockerfile:8` = `eclipse-temurin:11-jre-noble` (kann Java-21-Bytecode nicht laden). Kopiert `executionservice-0.1.jar`, `triplestore-0.1.jar`, `executionservice/use-case`, `docker/supervisord.conf`, `.env`, `pluginsystem/` aus einem Build-Kontext = von CI zusammengestellter `dist/`. Healthcheck `curl …:8090/rdf4j/repositories`. supervisord startet den Service mit `-Dpf4j.mode=deployment -Dpf4j.pluginsDir=/app/pluginsystem/plugins`, ohne JVM-Optionen. glibc/noble ist Pflicht (embedded Python 3.13 braucht GLIBC_2.38). |
| V2 | `.github/workflows/docker-image.yml`: JDK 11 (:26-30); Dist-Schritt (:40-63) kopiert `triplestore-0.1-war-exec.jar` (existiert nicht mehr → Schritt bricht ab) und Plugins aus einer **hart codierten Liste** von 8 (:51) — OPCUA/ScriptExecutor fehlen ohne technischen Grund; Release-Assets referenzieren `war-exec` (:173-174). Läuft nur auf `master`. |
| V3 | MQTTPlugin: `pluginsystem/plugins/pom.xml:35-41` schreibt Manifest-Einträge aus `${plugin.*}`; MQTTPlugin/pom.xml setzt diese Properties nie (Werte stehen nur in seiner `plugin.properties`: Klasse `de.dfki.asr.ajan.pluginsystem.mqttplugin.MQTTPlugin`, Id `MQTTPlugin`, Version `0.0.1`, Provider `Bhuvaneshwaran Ilanthirayan`) → leeres `Plugin-Id` → fehlt im Deployment-Modus. Vorbestehend seit dem Initial-Commit. |
| V4 | Docker Desktop lokal nutzbar (29.8.1); kein `eclipse-temurin`-Image im Cache. Kein lokales JDK 25. |
| V5 | E2E-Harness kann den Triplestore extern nutzen (`-Dajan.e2e.triplestoreUrl`), startet den Service aber immer selbst; `PluginLoadIT` liest `serviceLog()`. `AjanSystem.resolveSingleArtifact` filtert noch `-war-exec` (:183). |
| V6 | Triplestore-Shaded-JAR erbt `addClasspath=true` aus Root-`maven-jar-plugin` (pom.xml:95-103) → `Class-Path:` listet die eingeschatteten Tomcat-JARs → `TldScanner`-Warnungen. |
| V7 | behaviour: `skipTests=true` (`behaviour/pom.xml:173`). Mit Tests an: 21 Tests, 6 Fehler — 5× Mockito **1.10.19** (cglib, `InaccessibleObjectException` ab JDK 17), 1× echter Produktfehler `ClassCastException BigInteger→Integer` in `behaviour/.../nodes/branch/Executor.java:112` (`executionOrder` aus `getIntValue` liefert `BigInteger`). |
| V8 | executionservice: Surefire wählt seit Boot 4 den JUnit-Platform-Provider (junit-jupiter aus `spring-boot-starter-test`), TestNG-Tests werden **gar nicht gefunden**. `BeliefUpdateStallTest` braucht einen laufenden Triplestore auf 8090 (Kontextladen scheitert sonst), ist seit dem Initial-Commit ohne Begründung ausgeschlossen. |
| V9 | PMD: maven-pmd-plugin 3.6 (PMD 5) in root `pluginManagement` und executionservice; aktiv in common, behaviour, executionservice. Ruleset `build-resources/.../build-configuration/pmd-java-ruleset.xml` referenziert PMD-5-Rulesets (`rulesets/java/*.xml`) und PMD-5-AST in `violationSuppressXPath` — PMD 7 (Plugin 3.28.0, pmd-core 7.17.0) lädt es nicht. Obergrenze bei „alle Kategorien“: ~1140 Verstöße, davon ~460 `CommentSize` (Lizenzheader). |
| V10 | Fat-JAR-Hygiene: `commons-logging` kommt über `common → commons-validator:1.3.1` (neben `jcl-over-slf4j`); MappingPlugin bündelt `spring-core:4.3.6` über `mariaDB4j:2.2.3` — kein Mapping-Code referenziert mariaDB4j/Spring (grep). `javax.inject:1` in Mapping/MQTT ungenutzt. |
| V11 | pf4j 3.16.0: Index aus allen JARs des Plugin-Klassenpfads, Dependency-/Application-Extensions nicht mehr als Plugin-Extensions gemeldet, Annotation-Processor indiziert keine abstrakten Klassen mehr → **Extension-Zählungen in `loadcheck` können sich legitim ändern**. |
| V12 | `THIRDPARTYLICENSE_*.txt`: 7 handgepflegte Dateien (2020), kein Generator im Build. |
| V13 | Java 25: Boot 4.1.2 offiziell bis Java 27; Lombok 1.18.48 ok (JDK 25 ab 1.18.40). Risiken: alte Build-Plugins (shade 3.4.1 in OPCUA/Python, assembly 3.1.0, dependency 3.0.0), Tomcat 9.0.109 (9.0.110+ ist dokumentiert für 25), JEP (JNI → `--enable-native-access=ALL-UNNAMED`, JEP 472), Netty/Unsafe-Warnungen (JEP 498). Grep: keine SecurityManager/finalize/`sun.*`-Nutzung. |

## Eigenständig getroffene Entscheidungen (selbst freigegeben 2026-10-30)

1. **Alle 10 Plugins ins Docker-Image** (Dist per Glob statt Liste). Kein technischer Grund für das Fehlen von OPCUA/ScriptExecutor gefunden; Spec-Zielbild „alle 10 Plugins“.
2. **MQTTPlugin wird repariert** (Manifest-Properties) — das Plugin war im Image nie lauffähig; Reparatur = beabsichtigtes Verhalten.
3. **`BeliefUpdateStallTest` bleibt ausgeschlossen, jetzt begründet** (braucht laufenden Triplestore; Integrationsverhalten deckt die E2E-Suite). Spec lässt „dokumentiert draußen lassen“ ausdrücklich zu.
4. **mariaDB4j wird aus MappingPlugin entfernt**, sofern Compile, `loadcheck` und E2E grün bleiben (ungenutzt laut grep; schleppt Spring 4.3 ins Plugin).
5. **PMD 7: Ruleset 1:1 portieren** (dieselben Regeln auf ihre PMD-7-Namen), nicht „alle Kategorien“. Regeln, die in PMD 7 ersatzlos entfallen sind, entfallen mit Begründung; neue Verstöße der portierten Regeln werden behoben, nicht unterdrückt.
6. **Lizenzdateien werden generiert** (license-maven-plugin `add-third-party` je Modul, gleiche Dateinamen, fehlende Module ergänzen). Format ändert sich — akzeptiert, Inhalt wird korrekt statt veraltet.
7. **Java 25 wird lokal in Docker gebaut und getestet** (`maven:3.9-eclipse-temurin-25`), kein JDK-Download auf den Host.
8. **`docker-image.yml` wird jetzt geändert** (Spec Etappe 5 fordert es; der Workflow triggert weiterhin nur auf `master` und wird im Epic nie ausgeführt).
9. **Offen beim Auftraggeber (nicht Teil dieses Plans):** Härtung der Fehlerkörper (R9 — derzeit Boot-1.3-Parität mit Message/Exception), AJAN-editor-Gegentest, Merge nach `master`.

---

## Global Constraints

- Branch `modernization/java21`; `master` nie anfassen, nichts nach `master` mergen/pushen; Push des Epic-Branches erlaubt.
- Kein `docker push`, kein `docker login`, keine Tags `aantakli/*`. Lokale Images nur `ajan-service:dev` (Java 21) bzw. `ajan-service:dev25`.
- Öffentliche HTTP-API unverändert gegenüber Etappe 4 (inkl. R3/R4/R9/R10). E2E-Suite muss nach jedem Task grün sein (Ausnahme: Task 4 erweitert sie um den Container-Modus).
- Ports fix 8080/8090; E2E-Läufe und Container machine-weit exklusiv (Lock `C:\Users\yanni\AppData\Local\Temp\ajan-e2e.lock`, Protokoll wie in Plan 3 `wave-context.md`).
- Java: bis Task 7 **release 21** (Build-JDK `C:\Users\yanni\.jdks\ms-21.0.11`); ab Task 7 **release 25**. `e2e/` bleibt release 11 + RDF4J 3.6.3.
- Maven: `C:\Users\yanni\AppData\Local\Programs\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd` (3.9.11). Worktree-Agenten: privates Maven-Repo (`-Dmaven.repo.local=<wt>\.m2-private -Dmaven.repo.local.tail=C:\Users\yanni\.m2\repository`) und **Basis prüfen** (`git merge-base --is-ancestor <BASE> HEAD`; Worktrees zweigen sonst von `master` ab).
- Verifikation pro Task: `mvn clean install` (Modulzahl im Report) + `mvn -f e2e/pom.xml verify` mit Summenzeile.
- Fix-Richtungen nur mit verifizierter Annahme; Verstöße statischer Analyse werden behoben, nicht unterdrückt.

## Review Focus

1. **Deployment-Modus im echten Image** (`pf4j.mode=deployment`, Plugins aus `/app/pluginsystem/plugins`): bisher nie Ende-zu-Ende geprüft. → Task 4 Container-E2E inkl. `PluginLoadIT` (10 Plugins) gegen das Image.
2. **PythonPlugin im Image** (libjep, embedded Python, glibc): einziger nativer Pfad. → Task 4 Python-Smoke im Container; Task 7 erneut unter 25.
3. **Plugin-Extension-Zählungen nach pf4j 3.16** (V11): legitime vs. versehentliche Änderung. → Task 1 mit Begründung pro geänderter Zahl.
4. **Fehlerpfade nach Test-Reaktivierung** (Executor-Fix ändert Laufzeitverhalten eines Behavior-Tree-Knotens). → Task 2 Test, der vorher rot ist.
5. **Java-25-Laufzeitwarnungen/-fehler** (JNI, Unsafe, alte ASM in Build-Plugins). → Task 7 Startlog-Prüfung im Container.

---

### Task 1: Plugin-Deployment und Fat-JAR-Inhalte bereinigen

**Files:** `pluginsystem/plugins/MQTTPlugin/pom.xml`, `pluginsystem/pom.xml` (pf4j 3.16.0), `pluginsystem/loadcheck/src/test/java/.../DeploymentModeLoadTest.java`, `pluginsystem/plugins/MappingPlugin/pom.xml`, `common/pom.xml` (commons-logging-Exclude), `triplestore/pom.xml` (Class-Path)

**Interfaces:** Produces: alle 10 Deployment-JARs mit gesetztem `Plugin-Id`; `loadcheck` pinnt 10× `STARTED`.

- [ ] **Step 1:** MQTTPlugin-`<properties>`: `plugin.id=MQTTPlugin`, `plugin.class=de.dfki.asr.ajan.pluginsystem.mqttplugin.MQTTPlugin`, `plugin.version=0.0.1`, `plugin.provider=Bhuvaneshwaran Ilanthirayan`, `plugin.dependencies` leer. `loadcheck` zuerst anpassen (MQTT erwartet `STARTED`, Extension-Zahlen messen) → vor dem POM-Fix rot, danach grün.
- [ ] **Step 2:** pf4j `3.16.0`. `loadcheck` neu laufen lassen; **jede** geänderte Extension-Zahl im Report mit dem konkreten 3.16-Changelog-Punkt (V11) begründen — eine Änderung ohne Begründung stoppt den Task. `PluginLoadIT` (Dev-Modus, 10 Plugins) muss grün bleiben.
- [ ] **Step 3:** MappingPlugin: `mariaDB4j` und `javax.inject:1` entfernen; MQTTPlugin: `javax.inject:1` entfernen. Beleg: `jar tf MappingPlugin-0.1-all.jar | findstr org/springframework` → 0. `common`: `commons-logging` an `commons-validator` ausschließen; Beleg: executionservice-Fat-JAR enthält kein `commons-logging-*.jar` mehr, Startlog ohne JCL-Warnung.
- [ ] **Step 4:** Triplestore: `addClasspath=false` für das Launcher-JAR; Beleg: Manifest ohne `Class-Path`, Triplestore-Start ohne `TldScanner`-Warnung, `/rdf4j/home/overview.view` weiterhin 200.
- [ ] **Step 5:** Verifikation (install + E2E). Commits je Step.

### Task 2: Unit-Tests reaktivieren

**Files:** `behaviour/pom.xml` (skipTests weg, Mockito 5), `behaviour/src/test/java/**` (nur soweit Mockito-5-API es erfordert), `behaviour/src/main/java/de/dfki/asr/ajan/behaviour/nodes/branch/Executor.java`, `executionservice/pom.xml` (Surefire-TestNG-Provider), `executionservice/src/test/java/.../BeliefUpdateStallTest.java` (Begründungskommentar)

- [ ] **Step 1:** Mockito auf die aktuelle 5.x (Root-dependencyManagement, test scope). Die 5 Mockito-Fehler (V7) müssen verschwinden; Test-Anpassungen nur API-bedingt.
- [ ] **Step 2:** Executor-Bug TDD: `UnmarshalBehaviourTest.rootCanStep` ist der rote Test (`ClassCastException BigInteger→Integer`, `Executor.java:112`). Fix: `executionOrder` korrekt als Zahlen behandeln (Konvertierung über `Number#intValue`, keine Typannahme über den Literal-Datentyp). Zusätzlich ein gezielter Test, der eine `xsd:integer`-Reihenfolge mit ≥2 Kindern ausführt.
- [ ] **Step 3:** `behaviour` `skipTests` entfernen; alle behaviour-Tests grün.
- [ ] **Step 4:** executionservice: TestNG-Tests wieder auffindbar (Surefire-Provider `surefire-testng` als Plugin-Dependency **oder** gleichwertige Konfiguration); Beleg: `Tests run: N` > 0 für eine TestNG-Klasse. `BeliefUpdateStallTest` bleibt ausgeschlossen mit Kommentar am Exclude: braucht laufenden Triplestore (8090), Integrationsverhalten über E2E abgedeckt (Entscheidung 3).
- [ ] **Step 5:** Verifikation; Commits je Step.

### Task 3: PMD 7

**Files:** Root-`pom.xml` (pluginManagement), `executionservice/pom.xml`, `build-resources/src/main/resources/build-configuration/pmd-java-ruleset.xml`, betroffene Quelldateien in common/behaviour/executionservice

- [ ] **Step 1:** maven-pmd-plugin `3.28.0`; `targetJdk` entfernen (Release kommt aus dem Compiler); `sourceEncoding`-Warnung beheben. executionservice nutzt dieselbe Konfiguration (Insel: Version/Config dort ebenfalls).
- [ ] **Step 2:** Ruleset portieren: jede bisherige Regel bzw. jedes bisherige Ruleset auf die PMD-7-Kategorie/-Regel abbilden; Excludes übernehmen; `violationSuppressXPath` auf PMD-7-AST umschreiben. Mapping-Tabelle (alt → neu / entfallen + Grund) in den Report.
- [ ] **Step 3:** Verstöße der portierten Regeln beheben. Wird eine Regel durch geänderte PMD-7-Semantik zur Massenquelle (> 50 Treffer, z. B. `CommentSize` auf Lizenzheadern), darf sie im Ruleset mit Begründungskommentar konfiguriert/ausgeschlossen werden — **kein** `NOPMD`, kein `@SuppressWarnings("PMD…")` im Code. Positivkontrolle: absichtlicher Verstoß → Build rot.
- [ ] **Step 4:** Verifikation; Commit.

### Task 4: Docker-Deployment auf Java 21 + E2E gegen den Container

**Files:** Create `docker/assemble-dist.sh` (bash, CI und lokal identisch); Modify `Dockerfile`, `.github/workflows/docker-image.yml`, `docker/supervisord.conf` (nur falls nötig), `e2e/src/test/java/.../AjanSystem.java` (Container-Modus; `-war-exec`-Filter entfernen), `startAJAN.sh`/`.bat`, `.github/startAJAN.*`, `README.md` (Maven/NetBeans-Zeilen, Docker-Anleitung)

**Interfaces:** Produces: `docker/assemble-dist.sh <dist-dir>` (erzeugt exakt den Build-Kontext des Dockerfiles: beide JARs, `pluginsystem/plugins/*-0.1.jar` per Glob über `pluginsystem/deployments/*-0.1-all.jar`, `pluginsystem/loader-0.1.jar`, `use-case`, Skripte, `.env`); E2E-Properties `ajan.e2e.serviceUrl` (Service nicht starten, gegen URL testen) und `ajan.e2e.serviceLogFile` (Log-Datei für `PluginLoadIT`).

- [ ] **Step 1:** `assemble-dist.sh` aus der Dist-Logik von `docker-image.yml` extrahieren (Glob statt Liste — Entscheidung 1); der Workflow ruft das Skript auf. Workflow: JDK 21, Triplestore-JAR `triplestore-0.1.jar` (auch Release-Asset), sonst unverändert (Login/Push/Release-Jobs bleiben, werden nicht ausgeführt).
- [ ] **Step 2:** `Dockerfile` → `eclipse-temurin:21-jre-noble`; veraltete Kommentare korrigieren. Start-Skripte: falscher JAR-Pfad in `startAJAN.sh`, `--add-opens`-Bedarf unter 21 prüfen (nur behalten, wenn belegt nötig).
- [ ] **Step 3:** E2E-Container-Modus in `AjanSystem` (V5): bei gesetztem `ajan.e2e.serviceUrl` keinen Service starten/stoppen, `serviceBase()` = Property, `serviceLog()` = `ajan.e2e.serviceLogFile`; Triplestore über bestehendes `ajan.e2e.triplestoreUrl`. `-war-exec`-Filter entfernen. Der Standardmodus (lokale Prozesse) bleibt unverändert grün.
- [ ] **Step 4:** Lokal: `bash docker/assemble-dist.sh build/dist` → `docker build -t ajan-service:dev build/dist` (bzw. mit dem Dockerfile im Kontext) → Container mit `-p 8080:8080 -p 8090:8090` starten, Healthcheck abwarten, `docker logs -f` in eine Datei, dann `mvn -f e2e/pom.xml verify -Dajan.e2e.serviceUrl=http://localhost:8080/ajan -Dajan.e2e.triplestoreUrl=http://localhost:8090/rdf4j -Dajan.e2e.serviceLogFile=<datei>`. **Erwartung:** alle Tests grün, `PluginLoadIT` sieht 10 Plugins **im Deployment-Modus**. Zusätzlich Python-Smoke: Startlog ohne JEP-/libjep-Fehler; ein Aufruf, der das PythonPlugin tatsächlich nutzt, falls ein Use-Case-Agent das hergibt (sonst mindestens erfolgreiche Plugin-Initialisierung belegen). Container danach stoppen und entfernen.
- [ ] **Step 5:** Verifikation (Standard-E2E + Container-E2E), Commits.

### Task 5: Lizenzdateien generieren

**Files:** Root-`pom.xml` (license-maven-plugin in pluginManagement + Profil oder expliziter Goal-Aufruf), `THIRDPARTYLICENSE`, `**/THIRDPARTYLICENSE_*.txt`

- [ ] **Step 1:** `org.codehaus.mojo:license-maven-plugin` (aktuelle Version) so konfigurieren, dass `add-third-party` je Modul die bisherigen Dateinamen erzeugt (`THIRDPARTYLICENSE_<modul>.txt` am bisherigen Ort) und für bisher fehlende Module (MQTT, OPCUA, Python, RMLMapping, ScriptExecutor, triplestore) neue Dateien entstehen. Nicht an den Default-Build binden (eigener Aufruf, im README dokumentiert).
- [ ] **Step 2:** Generieren, Root-`THIRDPARTYLICENSE` auf die vollständige Dateiliste aktualisieren. Stichprobe: `rdf4j-sail-spin` taucht nicht mehr auf; RDF4J erscheint mit 5.3.1. Commit.

### Task 6: Restpunkte (Deferred Minors aus Plan 3)

Ein Batch-Task über die Liste in der Spec, Etappe 5, „Deferred Minors aus den Task-Reviews“ (T1–T7) plus README-Zeilen — **außer** den Punkten, die Tasks 1–5 bereits erledigen. Jeder Punkt: erledigt oder mit Begründung bewusst offen (Report-Tabelle). Insbesondere:
- T5: `SPARQLTextGuard`-`IllegalArgumentException` in `AgentModelManager` auf `InitializationRDFValidationException` (412) abbilden — **Verhaltensänderung** (500 → 412): nur mit neuem E2E-Test, der eine Agent-Anlage mit ungültiger IRI im Template pinnt.
- T6: `dev.resteasy.exception.mapper` so setzen, dass jeder Start (auch `@SpringBootTest`) ihn hat (statischer Initialisierer o. ä.).
- [ ] Verifikation; Commits gruppiert nach Modul.

### Task 7: Etappe 6 — Java 25

**Files:** Root-`pom.xml` (`maven.compiler.release=25`), `executionservice/pom.xml` (`java.version=25`), Build-Plugins (shade ≥ 3.6, assembly aktuell, dependency aktuell, failsafe in `e2e` auf 3.5.x), `triplestore/pom.xml` (Tomcat 9 aktuell, ≥ 9.0.110), `Dockerfile` (`eclipse-temurin:25-jre-noble`), `docker/supervisord.conf` + Start-Skripte (`--enable-native-access=ALL-UNNAMED`, nur falls die JNI-Warnung belegt auftritt), `.github/workflows/ci-branch.yml` + `docker-image.yml` (JDK 25)

- [ ] **Step 1:** Build im Container: `docker run --rm -v <repo>:/w -v ajan-m2:/root/.m2 -w /w maven:3.9-eclipse-temurin-25 mvn -B clean install` (Named Volume als Cache; nicht das Windows-`~/.m2` mounten). Rote Build-Plugins anheben, nur was rot ist.
- [ ] **Step 2:** E2E unter 25 im selben Container (`mvn -f e2e/pom.xml verify` — Services laufen im Container auf JDK 25). Grün.
- [ ] **Step 3:** Image `ajan-service:dev25` (Dist aus Step 1 via `assemble-dist.sh`), Container-E2E wie Task 4 Step 4. Startlog auf `WARNING: A restricted method`, `sun.misc.Unsafe`, `UnsupportedClassVersion` prüfen; Warnungen nur mit belegtem Flag beseitigen.
- [ ] **Step 4:** CI `ci-branch.yml` auf JDK 25; Push; CI grün (Run-ID).
- [ ] **Step 5:** Lokaler Default-JDK für Folgearbeit: Hinweis im README (Build über Docker oder lokales JDK 25).

### Task 8: Abschluss

- Spec: Etappe 5 und 6 „abgeschlossen“ mit Run-IDs; Restliste für den Auftraggeber (R9-Härtung, AJAN-editor-Test, `pre-java21`-Tag + Merge nach `master` als Release-Akt). Epic-Ledger + Memory.
- Whole-Branch-Review über Plan 4 (Muster wie Plan 3), danach eine Fix-Welle.

## Abhängigkeiten / Parallelisierung

Welle A parallel (Worktrees, je eigener Branch von Head nach Plan-Commit): **Task 1, 2, 3, 5** (disjunkt bis auf POM-Abschnitte; Merge durch Controller). Danach **Task 4** (braucht Task 1: MQTT, Class-Path), dann **Task 6**, dann **Task 7**, dann **Task 8**. Container und E2E sind exklusiv (Lock).
