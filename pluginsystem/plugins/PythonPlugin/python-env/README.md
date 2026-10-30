# python-env

Reproducible generator for the portable CPython environments embedded in the PythonPlugin
(`nix_env.zip`, `win_env.zip`). Output goes to `dist/` (gitignored); zip root dir is `python-portable/`.

Pinned inputs: `python-env.properties` (python-build-standalone tag, CPython version, SHA-256 per platform),
`requirements.in` (direct deps) and `requirements.lock` (hash-pinned, installed with `pip --require-hashes`).

## Build the Linux env

    ./build-linux.sh [outdir]      # needs Docker; Git Bash on Windows works

The script re-runs itself in `eclipse-temurin:25-jdk-noble` (jep is compiled from sdist against the JDK there)
and writes `dist/nix_env.zip`.

## Add a package

1. Add it to `requirements.in`.
2. Re-lock:
   `uv pip compile requirements.in --universal --generate-hashes --python-version 3.13 -o requirements.lock`
3. Rebuild (`./build-linux.sh`) and commit `requirements.in` + `requirements.lock`.

`jep` must match the Java dependency `black.ninia:jep` (currently 4.3.2).

## Getting prebuilt zips from CI

The Windows env needs MSVC and is built in CI. Once the CI workflow (`python-env.yml`) and `fetch-ci.sh` exist,
`./fetch-ci.sh` downloads `nix_env.zip` and `win_env.zip` from the latest successful run into `dist/`.
