# Python-Laufzeit für AJAN — Design (Variante 2C)

Status: umgesetzt und abgeschlossen (2026-10-31, Plan 5, Head siehe Abschnitt „Ergebnis“). Freigegeben 2026-10-30 (Variante vom User gewählt: „2C, die Flexibilität möchte ich erhalten“).
Branch: `modernization/java21`. Vorgänger: Java-Modernisierungs-Epic (Etappen 0–6, abgeschlossen).

## Ziel

Python-Knoten (PythonLeafNode, CustomScriptNode, OPCUA-Knoten) funktionieren unter Linux/Docker und
Windows identisch, mit beliebigen Python-Paketen (inkl. nativer Erweiterungen wie numpy/orjson), aus
einer reproduzierbar gebauten Umgebung — weiterhin **in-process via JEP, Auslieferung als einzelne Plugin-JAR**.

## Harte Randbedingungen (User)

- Kein zusätzlicher Worker-Prozess/Container. Python läuft im Service-JVM-Prozess (JEP).
- Deployment bleibt: Plugin-JAR(s) + Service-JAR; Python-Umgebung steckt im PythonPlugin-JAR.
- Beliebige Pakete (auch mit nativem Code) müssen nutzbar bleiben → echtes CPython, kein GraalPy.
- Epic-Regeln gelten weiter: nur Branch `modernization/java21`, nie master, kein Docker-Push, öffentliche HTTP-API unverändert.

## Ist-Befund (gemessen 2026-10-30 in `ajan-service:dev25`)

1. `nix_env.zip` (CPython 3.13.5, jep 4.2.2, handgebaut) enthält weder `rdflib` noch `AJANlib`; `win_env.zip` enthält beide.
2. Das dokumentierte Skript-Muster (`class MyNode(AJANlib.LeafNode)` mit `executeLeafNode`) wird von Java nie aufgerufen;
   `PythonLeafNode` liest nur globale `status`/`rdf_output`. Folge: Beispiel/Vorlage schreiben auf keinem OS ein Ergebnis.
3. OPCUA auf keinem OS lauffähig: `asyncua`/`orjson`/`aiofiles`/`pytz` fehlen; Skript-Ordner nicht im `sys.path`;
   `main.py` erwartet `argparse`-Argumente, bekommt aber Variablen; Knoten melden immer SUCCEEDED.
4. Globales `synchronized` serialisiert alle Python-Aufrufe; Event-Modus von OPCUA (Endlosschleife) würde alles blockieren.
5. Ungenutzt: `PythonPackageManager`, `VirtualEnvironmentManager`, `ScriptWatcher`, `nix_venv/`, `win_venv/` (Py 3.9),
   `__pycache__/`. Reflection-Hacks (`setEnv`, `usr_paths`) sind auf Java 17+ wirkungslos.
6. Zips (111 MB) liegen als Binärdateien im Git; Entpacken bei jedem Start in ein neues Temp-Verzeichnis ohne Aufräumen.

## Design

### D1 — Reproduzierbare Python-Umgebung (Build-Zeit)
- Neues Verzeichnis `pluginsystem/plugins/PythonPlugin/python-env/`:
  - `python-env.properties`: python-build-standalone-Release-Tag `20261026`, CPython `3.13.18`, SHA-256 je Plattform
    (`x86_64-unknown-linux-gnu`, `x86_64-pc-windows-msvc`, jeweils `install_only.tar.gz`).
  - `requirements.in`: `jep==4.3.2`, `rdflib`, `asyncua`, `aiofiles`, `orjson`, `pytz` (Nutzer ergänzen hier Pakete).
  - `requirements.lock`: erzeugt mit `uv pip compile --universal --generate-hashes --python-version 3.13`; Installation
    ausschließlich mit `pip install --require-hashes`.
  - `build-linux.sh`: läuft in Docker (`eclipse-temurin:25-jdk-noble` + `build-essential`), lädt PBS-Tarball, prüft SHA-256,
    installiert Lock (jep wird aus dem sdist gegen das JDK kompiliert), Smoke-Import, packt `dist/nix_env.zip`.
  - `build-windows.ps1`: dasselbe für Windows (benötigt MSVC + JDK; im CI auf `windows-latest`), packt `dist/win_env.zip`.
  - `fetch-ci.sh`: holt beide Zips als Artefakte des letzten erfolgreichen CI-Laufs (`gh run download`), für Entwickler ohne MSVC.
  - `dist/` ist gitignored. Die alten Zips, `nix_venv/`, `win_venv/`, `__pycache__/` und die alten Build-Skripte werden entfernt.
- Wiederverwendbarer Workflow `.github/workflows/python-env.yml` (`workflow_call`): Matrix Linux/Windows, Cache-Key =
  Hash von `python-env/**`, lädt `python-env-linux`/`python-env-windows` als Artefakte hoch.

### D2 — Maven/Auslieferung
- PythonPlugin nimmt `python-env/dist/*.zip` als Ressourcen ins JAR (Pfade `/nix_env.zip`, `/win_env.zip` wie bisher).
- Fehlt ein Zip, baut Maven trotzdem (Warnung); die Laufzeit meldet das klar (D3). `docker/assemble-dist.sh` verlangt
  `nix_env.zip` im PythonPlugin-JAR; `win_env.zip` zusätzlich, wenn `REQUIRE_WIN_ENV=1` (Release/Dry-Run setzen es).
- `ci-branch.yml` und `docker-image.yml` rufen `python-env.yml` auf und legen die Zips vor `mvn install` nach `python-env/dist/`.
- Java-Seite `black.ninia:jep` auf `4.3.2` (muss zur nativen Version passen).
- Dockerfile: apt-Paket `python3` entfällt (wird nicht benutzt).

### D3 — Laufzeit (PythonPlugin)
- Umgebungsauswahl: System-Property/Env `ajan.python.home` (externes CPython mit installiertem jep) hat Vorrang;
  sonst eingebettetes Zip für das aktuelle OS.
- Entpacken in einen Cache `${ajan.python.cacheDir:-<Benutzer-Cache>/ajan-python}/<sha256-12>/` (`~/.cache/ajan-python`, Windows `%LOCALAPPDATA%\ajan-python`; nie ein geteiltes Temp-Verzeichnis, POSIX 0700, fremde/symlinkte/gruppenschreibbare Verzeichnisse werden abgelehnt) mit Marker-Datei
  `.complete`; vorhandener vollständiger Cache wird wiederverwendet, ältere Cache-Verzeichnisse werden gelöscht, sofern keine andere JVM sie nutzt (geteilter Lock auf `<sha256-12>.inuse`; vor dem Löschen Umbenennen nach `.trash-*`).
- JEP-Init explizit: `MainInterpreter.setJepLibraryPath(<abs. libjep.so|jep.dll>)`, `PyConfig.setPythonHome`; Reflection-Hacks entfallen.
- `AJANlib.py` ist Ressource des PythonPlugin-JARs und wird beim Start in `<cache>/<sha256-12>/ajan-lib/` gelegt (eingebettete Umgebung; mit `ajan.python.home` in `<cache>/ajan-lib/`), das auf den `sys.path` kommt.
- Fehlt jede Umgebung oder schlägt die Init fehl: Plugin startet trotzdem (pf4j STARTED), `isPythonAvailable()=false`,
  Python-Knoten enden FAILED mit klarer Meldung. Kein Absturz des Service.
- Globales `synchronized` entfällt (jeder Aufruf eigener `SharedInterpreter` auf dem aufrufenden Thread; GIL sorgt für Sicherheit).

### D4 — Skript-Vertrag PythonLeafNode
- Neu (dokumentiertes Muster): Skript definiert genau eine Unterklasse von `AJANlib.LeafNode`. AJAN instanziiert sie,
  übergibt den Input als `rdflib.Graph`, ruft `executeLeafNode`, mappt `NodeResult.status`, schreibt `rdf_output`
  (Graph → Turtle) in die Ziel-Wissensbasis. Logik in `AJANlib.run_leaf(...)`, Java bleibt dünn.
- Alt (Kompatibilität): ohne LeafNode-Unterklasse gelten weiter die globalen `status`/`rdf_output`/`input_rdf`.
- Python-Exceptions → FAILED mit Traceback im Agent-Log.

### D5 — Skript-Dateien und OPCUA
- `executeScriptFile` setzt `__file__`, `__name__ == "__main__"`, stellt den Skript-Ordner vorn in `sys.path` und
  unterstützt optional `argv` (Input-Schlüssel `argv`, `List<String>` → `sys.argv`). `SystemExit` ≠ 0 → Fehlschlag.
- OPCUA-Methodenknoten (EnableAsset, SelectJoint, StartJoining) rufen `main.py` mit `argv` auf und werten das Ergebnis aus.
  `main.py` beendet sich bei Fehlern mit Exit-Code ≠ 0.
- OPCUASetupNode (Event-Modus, Endlosschleife) startet den Client in einem eigenen Daemon-Thread je Endpoint
  (idempotent) und blockiert den Behavior Tree nicht.

## Bewusst nicht im Umfang
- Kein Timeout/Abbruch laufender Skripte (JEP kann CPython-Code nicht unterbrechen); kein Absturzschutz gegen native Crashes.
  Dokumentiert als bekannte Grenze von 2C.
- Keine weiteren Plattformen (aarch64, macOS) eingebettet; dafür `ajan.python.home`.
- Kein Laufzeit-`pip install` (Pakete kommen über `requirements.in`).

## Akzeptanz
- Container-Test: Python_Example-Agent schreibt `python-ns#label "is an Python Agent"` in seine Wissensbasis.
- In JEP (Linux-Container und Windows): `import rdflib, asyncua, orjson, AJANlib` gelingt (orjson = native Erweiterung).
- OPCUA-Methodenknoten gegen nicht erreichbaren Endpoint → FAILED (nicht SUCCEEDED).
- Volle E2E-Suite grün, lokal (Windows, mit `win_env.zip` aus CI) und im Container; CI grün inkl. publish-dryrun.
- Keine Python-Zips mehr im Git-Tree.

## Ergebnis (2026-10-31)
- Plan `docs/superpowers/plans/2026-10-30-python-runtime-plan-5.md`, Tasks 1–7 umgesetzt, Task-Reviews + Gesamt-Review + Code-Health-Review, eine Final-Fix-Welle.
- CI grün: Run 41142948174 (python-env Linux/Windows, build-and-e2e mit E2E 31/31 inkl. `PythonNodeIT`, publish-dryrun, python-windows mit PythonPlugin 46 Tests/7 POSIX-only übersprungen und OPCUA 13/13). Lokal: Container-E2E 31/31 gegen `ajan-service:dev25`.
- Akzeptanz erfüllt: Python_Example schreibt `python-ns#label "is an Python Agent"`; `rdflib`, `asyncua`, `orjson`, `AJANlib` importierbar in JEP unter Linux und Windows; OPCUA-Methodenknoten gegen unerreichbaren Endpoint → FAILED; keine Python-Zips mehr im Git-Tree.
- Bewusste Verhaltensänderungen: FAILED-Leaf-Knoten schreiben kein `rdf_output` mehr; `main.py`-Fehler → Exit 1 mit Grund; externes Python ohne ladbares libpython → Python nicht verfügbar (ERROR); Cache liegt pro Nutzer (`~/.cache/ajan-python`, `%LOCALAPPDATA%\ajan-python`) mit 0700 und In-Use-Locks.
- Offene manuelle Prüfungen (User): OPCUA gegen echten IJT-Server (Status 0 = OK angenommen); vollständiger Service-Lauf unter Windows-Host mit JDK 25.
- Geparkt (Wartbarkeit): Duplikate in CI-Workflows/Build-Skripten/Surefire-Konfig, gleichnamige Module verschiedener Skript-Ordner teilen `sys.modules`, Event-Client wird bei Plugin-Stop/Agent-Löschen nicht beendet.
- Container-Verifikation (2026-10-31, Image `ajan-service:dev25`): Voll-Build mit allen Tests grün, E2E 31/31 im Container, manuelle Checks (Python_Example, orjson/asyncua/rdflib in JEP, zwei parallele Python-Agenten, fehlschlagendes Skript, CustomScriptNode, OPCUA gegen unerreichbaren Endpoint, Container-Neustart mit Cache-Wiederverwendung, Swagger/Triplestore) bestanden. Dabei gefunden und behoben: OPCUA-Knoten bekamen ihre RDF-Parameter nie (fehlende `@RDFNamespaces`, seit Einführung des Plugins — die frühere Akzeptanz „OPCUA → FAILED“ traf nur zufällig über den Default-Endpoint zu), Skript-`print()` erschien im Container nicht im Log (Block-Pufferung), `Bound` nutzte ein nicht deklariertes `rdf:`-Präfix.
