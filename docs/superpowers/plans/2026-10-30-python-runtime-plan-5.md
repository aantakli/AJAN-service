# Python-Laufzeit (Variante 2C) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Python-Knoten laufen unter Linux/Docker und Windows gleich, aus reproduzierbar gebauter, eingebetteter CPython-Umgebung mit beliebigen Paketen, in-process via JEP.

**Architecture:** Build-Zeit-Generator (python-build-standalone + gehashte Lock-Datei) erzeugt `nix_env.zip`/`win_env.zip` außerhalb von Git; CI baut sie (Linux in Docker, Windows auf Windows-Runner) und legt sie vor Maven ab; das PythonPlugin-JAR bettet sie ein und entpackt sie in einen Hash-Cache. JEP-Init explizit, AJANlib als JAR-Ressource, Klassen-Skriptvertrag über `AJANlib.run_leaf`.

**Tech Stack:** CPython 3.13.18 (python-build-standalone 20261026), jep 4.3.2 (native + `black.ninia:jep`), rdflib, asyncua, uv (nur Locking), pip `--require-hashes`, GitHub Actions reusable workflow, Java 25, pf4j.

**Spec:** `docs/superpowers/specs/2026-10-30-python-runtime-design.md`

## Global Constraints

- Branch `modernization/java21` only; never touch/merge/push master; no `docker push`/`docker login`, no `aantakli/*` tags.
- Push of the epic branch to origin is allowed.
- No extra worker process/container; Python stays in-process (JEP); delivery stays plugin JAR + service JAR.
- Public HTTP API unchanged; full e2e suite stays green.
- Pins: python-build-standalone tag `20261026`, CPython `3.13.18`, jep `4.3.2` (native and Maven must be equal).
- No binary Python environments in Git (`python-env/dist/` is gitignored).
- Worktree agents: reset onto the epic head and verify `git merge-base` before committing; private Maven repo `-Dmaven.repo.local=<wt>\.m2-private -Dmaven.repo.local.tail=C:\Users\yanni\.m2\repository`; e2e only under the lock `C:\Users\yanni\AppData\Local\Temp\ajan-e2e.lock` (mkdir).
- Host has no JDK 25 and no MSVC: JDK-25 builds via `docker run --rm -v "<repo>:/w" -v ajan-m2:/root/.m2 -w /w maven:3.9-eclipse-temurin-25 mvn -B ...`; the Windows env is built only in CI.

## Review Focus

1. Native extension import inside JEP on Linux (orjson/asyncua C-ext needs libpython symbols globally visible) — must be proven in a JEP test, not with standalone `python`.
2. Cache reuse across restarts with a half-extracted directory (crash during unzip) — must not be treated as complete.
3. Plugin start without any env (dev checkout before fetching zips) — service must still start, Python nodes FAIL with a clear message.
4. Concurrent Python nodes from two agents after removing `synchronized` — no deadlock, no cross-talk of globals.
5. Legacy scripts (globals `status`/`rdf_output`) keep working after the class contract is added.

---

### Task 1: Linux env generator

**Files:**
- Create: `pluginsystem/plugins/PythonPlugin/python-env/{python-env.properties,requirements.in,requirements.lock,build-linux.sh,README.md,.gitignore}`
- Delete: `pluginsystem/plugins/PythonPlugin/{build_python_portable.sh,build-portable-python.ps1}` (replaced)

**Interfaces — Produces:** `python-env/dist/nix_env.zip` with one root dir `python-portable/` (PBS `install_only` layout: `bin/`, `lib/libpython3.13.so*`, `lib/python3.13/site-packages/{jep,rdflib,asyncua,orjson,aiofiles,pytz,...}`); properties keys `pbs.tag`, `python.version`, `sha256.linux`, `sha256.windows`.

- [ ] Write `python-env.properties` with the real SHA-256 of both `cpython-3.13.18+20261026-<triple>-install_only.tar.gz` (from the release's `SHA256SUMS`).
- [ ] `requirements.in`: `jep==4.3.2`, `rdflib`, `asyncua`, `aiofiles`, `orjson`, `pytz`; generate `requirements.lock` with `uv pip compile requirements.in --universal --generate-hashes --python-version 3.13 -o requirements.lock`.
- [ ] `build-linux.sh [outdir]`: runs itself inside `docker run eclipse-temurin:25-jdk-noble` (apt `build-essential curl zip`), downloads + sha-verifies PBS, `JAVA_HOME=/opt/java/openjdk bin/python3 -m pip install --no-cache-dir --require-hashes -r requirements.lock`, smoke `bin/python3 -c "import rdflib, asyncua, orjson, aiofiles, pytz"` plus existence of `site-packages/jep/libjep.so`, then `zip -r -X` into `dist/nix_env.zip` (strip `__pycache__`, `*.a` static libs).
- [ ] Run it; record zip size and smoke output in the report.
- [ ] README: how to add a package (edit `.in`, re-lock, rebuild), how to fetch CI artifacts (Task 2).
- [ ] Commit `build(python): reproducible linux python env generator`.

### Task 2: Windows env generator + reusable CI workflow

**Files:**
- Create: `pluginsystem/plugins/PythonPlugin/python-env/{build-windows.ps1,fetch-ci.sh}`, `.github/workflows/python-env.yml`
- Modify: `.github/workflows/ci-branch.yml` (add a `python-env` job using the reusable workflow; no consumer yet)

**Interfaces — Consumes:** Task 1 files. **Produces:** artifacts `python-env-linux` (`nix_env.zip`), `python-env-windows` (`win_env.zip`); `python-env.yml` with `on: workflow_call` whose outputs nothing, artifacts only; cache key `python-env-${{ runner.os }}-${{ hashFiles('pluginsystem/plugins/PythonPlugin/python-env/**') }}` excluding `dist/`.

- [ ] `build-windows.ps1`: PBS windows tarball (sha-verified), `python.exe -m pip install --require-hashes -r requirements.lock` (needs JAVA_HOME + MSVC; on CI use `actions/setup-java` 25 and `ilammy/msvc-dev-cmd@v1`), smoke import, `jep\jep.dll` exists, zip root `python-portable\` into `dist\win_env.zip`.
- [ ] `python-env.yml`: matrix `ubuntu-latest` (build-linux.sh) / `windows-latest` (build-windows.ps1), actions/cache on `dist/`, upload artifacts.
- [ ] `fetch-ci.sh`: `gh run download` of both artifacts from the latest successful `ci-branch` run on the current branch into `python-env/dist/`.
- [ ] Push branch, wait for CI; both artifacts present and the windows smoke step green. Report run id.
- [ ] Commit `ci(python): build python envs for linux and windows in CI`.

### Task 3: PythonPlugin runtime rewrite

**Files:**
- Modify: `PythonPlugin.java`, `extensions/PythonExecutionServiceImpl.java`, `pluginsystem/plugins/PythonPlugin/pom.xml` (resources from `python-env/dist`, include `ajan-lib/AJANlib.py`), `pluginsystem/plugins/pom.xml` + `pluginsystem/loader/pom.xml` (jep `4.3.2`)
- Move: `pluginsystem/plugins/PythonPlugin/AJANlib.py` → `src/main/resources/ajan-lib/AJANlib.py`
- Delete: `src/main/resources/{nix_env.zip,win_env.zip,nix_venv/,win_venv/,__pycache__/}`, `PythonPackageManager.java`, `venv/VirtualEnvironmentManager.java`, `hotreload/ScriptWatcher.java` (verify unused first); `PythonExecutionService.installPythonPackage` and `executePowerShellCommand` stay in the interface (extension point API) but return `failed("not supported")`.
- Create: `src/main/java/.../pythonplugin/PythonEnvironment.java`, tests `src/test/java/.../PythonEnvironmentTest.java`, `PythonRuntimeIT`-style JUnit test `PythonJepTest.java`

**Interfaces — Produces:**
- `final class PythonEnvironment { static PythonEnvironment resolve(); Path home(); Path sitePackages(); Path jepLibrary(); Path ajanLib(); }` — `resolve()` order: `ajan.python.home` (system property, then env var `AJAN_PYTHON_HOME`) → embedded `/nix_env.zip` or `/win_env.zip`; throws `PythonUnavailableException` (checked, message names what is missing).
- Cache dir: `${ajan.python.cacheDir:-${java.io.tmpdir}/ajan-python}/<first 12 hex of sha256(zip)>/`, marker `.complete` written last; incomplete dir is deleted and re-extracted; sibling cache dirs other than the current one are deleted.
- `PythonPlugin.isAvailable()`; `PythonExecutionServiceImpl.isPythonAvailable()` delegates; when unavailable every execute returns `failed("Python not available: <reason>")`.
- JEP init: `MainInterpreter.setJepLibraryPath(jepLibrary)`, `PyConfig.setPythonHome(home)`, `JepConfig.addIncludePaths(ajanLib, sitePackages)`; on Linux preload `libpython3.13.so.1.0` so C extensions resolve symbols (prove with orjson import).
- `executeScript` no longer `synchronized`.

- [ ] Tests first: `PythonEnvironmentTest` (pure Java, no JEP): cache reuse with marker, incomplete dir re-extracted, stale sibling removed, `ajan.python.home` precedence, missing everything → `PythonUnavailableException`. Build fixture zips in the test.
- [ ] `PythonJepTest` (runs when the env zip for this OS is on the classpath; `-Dajan.python.requireEnv=true` turns absence into failure): `import rdflib, asyncua, orjson, AJANlib` inside JEP; two threads run `executeScript` concurrently with different globals and see only their own values.
- [ ] Implement; surefire `argLine` gets `--enable-native-access=ALL-UNNAMED`.
- [ ] Plugin starts with no env (loadcheck/e2e still 10 STARTED); Python nodes report FAILED with reason.
- [ ] Verify in JDK-25 Docker build with `nix_env.zip` from Task 1 copied to `python-env/dist/`.
- [ ] Commit(s) `refactor(python): deterministic env resolution and explicit JEP init` etc.

### Task 4: PythonLeafNode class contract

**Files:** Modify `src/main/resources/ajan-lib/AJANlib.py`, `extensions/PythonLeafNode.java`; Test `src/test/java/.../PythonLeafContractTest.java` (JEP, gated like Task 3).

**Interfaces — Produces:** `AJANlib.run_leaf(namespace: dict, input_turtle: str) -> tuple[str, str, str]` returning `(status, label, output_turtle)`; picks the single `LeafNode` subclass defined in `namespace` (not `LeafNode` itself), instantiates with no args, parses input into `rdflib.Graph` (`format="turtle"`, empty → empty graph), calls `executeLeafNode`, status from `NodeResult.status` (`Status` enum or string), output serialized as Turtle (`None`/empty graph → `""`). No subclass → legacy: return globals `status` (default `"SUCCEEDED"`), `""`, `rdf_output`. More than one subclass → `ValueError`.

- [ ] Tests: Python_Example script (copy from `executionservice/use-case/Examples/Python_Example/behaviors.ttl`) with an input graph containing one `ajan:Agent` yields one `python-ns#label` triple and SUCCEEDED; legacy script setting `status='FAILED'` → FAILED; script raising → FAILED with traceback text; two subclasses → FAILED.
- [ ] PythonLeafNode calls `run_leaf` via JEP, keeps stdout capture, maps status, writes Turtle as today.
- [ ] Commit `fix(python): execute the documented LeafNode class contract`.

### Task 5: Script files and OPCUA

**Files:** Modify `PythonExecutionServiceImpl.executeScriptFile`, OPCUA nodes `OPCUA{Setup,EnableAsset,SelectJoint,StartJoining}Node.java`, `OPCUAPlugin/src/main/resources/scripts/opcua/main.py`; Test in OPCUAPlugin or PythonPlugin (JEP-gated).

**Interfaces — Produces:** `executeScriptFile(path, inputs)`: sets `__file__`, `__name__="__main__"`, prepends `path.getParent()` to `sys.path` (removed afterwards), if `inputs.get("argv") instanceof List` sets `sys.argv=[str(path)]+argv`; `SystemExit` with code 0/None → success, otherwise failed. Method nodes build argv `--url <u> --call <c> [--joint-id|--enable|--deselect ...]` and return FAILED when result not success. `main.py` exits non-zero on connection/method error. Setup node: starts event mode (`--url <u>`) on a daemon thread named `opcua-events-<endpoint>`, at most one per endpoint (static registry), returns SUCCEEDED after start.

- [ ] Tests: script in a temp dir importing a sibling module; argv passed through; `sys.exit(3)` → failed; EnableAsset against `opc.tcp://127.0.0.1:1` → FAILED.
- [ ] Commit `fix(opcua): run main.py with argv, sibling imports and real status`.

### Task 6: CI, Docker and dist consume the envs

**Files:** Modify `.github/workflows/ci-branch.yml`, `.github/workflows/docker-image.yml`, `docker/assemble-dist.sh`, `Dockerfile`, `README.md`.

- [ ] Both workflows: job needs `python-env`, download both artifacts into `pluginsystem/plugins/PythonPlugin/python-env/dist/` before `mvn`; Maven runs with `-Dajan.python.requireEnv=true`.
- [ ] `assemble-dist.sh`: fail unless the PythonPlugin jar contains `nix_env.zip`; with `REQUIRE_WIN_ENV=1` also `win_env.zip`; publish-dryrun and docker-image set it.
- [ ] Dockerfile: drop apt `python3`, update the glibc comment (PBS needs glibc ≥ 2.17).
- [ ] README: local setup (`python-env/fetch-ci.sh` or `build-linux.sh`), `ajan.python.home`, adding packages, known limits (no timeout, native crash kills service).
- [ ] Push; CI green incl. publish-dryrun. Commit `ci: ship generated python envs in the plugin jar`.

### Task 7: E2E Python test and full verification

**Files:** Create `e2e/src/test/java/de/dfki/asr/ajan/e2e/PythonNodeIT.java` (+ fixture TTL if needed).

- [ ] Test: create an agent whose behavior runs the Python_Example LeafNode (reuse existing e2e agent-creation helpers), wait until agent knowledge contains `<agent> <http://www.ajan.de/python-ns#label> "is an Python Agent"`.
- [ ] Run full e2e locally (Windows host, `win_env.zip` via `fetch-ci.sh`) and in container mode against `ajan-service:dev25` rebuilt from this branch. Both green.
- [ ] Commit `test(e2e): python leaf node writes its result`.

### Task 8: Close
- [ ] Spec status „abgeschlossen“ with CI run ids; memory updated. (Controller.)
