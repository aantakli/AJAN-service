#!/usr/bin/env bash
# Assembles the AJAN distribution directory from a finished `mvn install`.
#
# The result is both the release ZIP content and the Docker build context
# (see Dockerfile, built with `docker build -f Dockerfile <dist-dir>`). CI
# (.github/workflows/docker-image.yml) and local builds call this same script.
#
# Usage: docker/assemble-dist.sh <dist-dir>
#   VERSION (env, optional): written to <dist-dir>/.env, defaults to "LOCAL".
#
# Layout produced:
#   executionservice-0.1.jar, triplestore-0.1.jar
#   pluginsystem/loader-0.1.jar
#   pluginsystem/plugins/<Plugin>-0.1.jar   (every pluginsystem/deployments/*-0.1-all.jar)
#   use-case/                                (executionservice/use-case + empty editor/nodeDefinitions)
#   docker/supervisord.conf                  (Docker image only)
#   start*.sh / start*.bat                   (from .github/, for the release ZIP)
#   .env                                     (VERSION=...)
set -euo pipefail

if [ "$#" -ne 1 ] || [ -z "$1" ]; then
    echo "usage: $0 <dist-dir>" >&2
    exit 2
fi

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
dist="$1"

rm -rf "$dist"
mkdir -p "$dist/pluginsystem/plugins" "$dist/docker"

cp "$root/triplestore/target/triplestore-0.1.jar" "$dist/"
cp "$root/executionservice/target/executionservice-0.1.jar" "$dist/"
cp "$root/pluginsystem/loader/target/loader-0.1.jar" "$dist/pluginsystem/"

# Self-contained plugin fat JARs (jar-with-dependencies / shaded) collected by
# the plugin builds in pluginsystem/deployments/. pf4j's JarPluginLoader only
# puts the loaded JAR on the plugin classpath, so each plugin must carry its
# non-provided runtime deps inside; the per-plugin target/<plugin>.jar is slim.
# Globbed (not listed) so every plugin module ends up in the image. The -all
# suffix is dropped so pf4j sees one canonical filename per plugin.
shopt -s nullglob
plugin_jars=("$root"/pluginsystem/deployments/*-0.1-all.jar)
shopt -u nullglob
if [ "${#plugin_jars[@]}" -eq 0 ]; then
    echo "ERROR: no plugin JARs in $root/pluginsystem/deployments - run 'mvn install' first." >&2
    exit 1
fi
for jar in "${plugin_jars[@]}"; do
    name="$(basename "$jar" -0.1-all.jar)"
    cp "$jar" "$dist/pluginsystem/plugins/${name}-0.1.jar"
done
echo "Copied ${#plugin_jars[@]} plugin JAR(s) to $dist/pluginsystem/plugins"

# Initial RDF data the start scripts / supervisord load. nodeDefinitions is
# populated at runtime by NodeDefinitionsExtension; pre-create it so the
# initial RDF graph walk finds a real directory.
cp -r "$root/executionservice/use-case" "$dist/use-case"
mkdir -p "$dist/use-case/editor/nodeDefinitions"

cp "$root/docker/supervisord.conf" "$dist/docker/"
cp "$root"/.github/*.sh "$root"/.github/*.bat "$dist/"
# A Windows checkout (core.autocrlf=true) has CRLF here; the files run on
# Linux (container / release ZIP), so normalise them to what CI produces.
sed -i 's/\r$//' "$dist"/*.sh "$dist/docker/supervisord.conf"
chmod +x "$dist"/*.sh

printf 'VERSION="%s"\n' "${VERSION:-LOCAL}" > "$dist/.env"
