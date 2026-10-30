#!/usr/bin/env bash
# Assembles the AJAN distribution directory from a finished `mvn install`.
#
# The result is both the release ZIP content and the Docker build context
# (see Dockerfile, built with `docker build -f Dockerfile <dist-dir>`). CI
# (.github/workflows/docker-image.yml) and local builds call this same script.
#
# Usage: docker/assemble-dist.sh <dist-dir>
#   VERSION (env, optional): written to <dist-dir>/.env, defaults to "LOCAL".
# Requires bash and GNU coreutils/sed (sed -i without suffix argument, comm);
# fine on Linux CI and Git Bash, not on stock macOS/BSD sed.
#
# Layout produced:
#   executionservice-0.1.jar, triplestore-0.1.jar
#   pluginsystem/loader-0.1.jar
#   pluginsystem/plugins/<Plugin>-0.1.jar   (one per <module> of pluginsystem/plugins/pom.xml)
#   use-case/                                (executionservice/use-case + empty editor/nodeDefinitions)
#   docker/supervisord.conf                  (Docker image only)
#   start*.sh / start*.bat                   (from .github/, for the release ZIP)
#   .env                                     (VERSION=...)
#   .dockerignore                            (excludes the CI release *.zip)
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
# The expected set is the <module> list of pluginsystem/plugins/pom.xml (a new
# plugin needs no edit here). deployments/ is not emptied by `mvn clean`, so a
# missing JAR or a leftover from a removed plugin fails the build instead of
# silently changing the image. The -all suffix is dropped so pf4j sees one
# canonical filename per plugin.
expected=$(sed 's/<!--.*-->//g' "$root/pluginsystem/plugins/pom.xml" \
    | sed -n 's|.*<module>[[:space:]]*\([^<[:space:]]*\)[[:space:]]*</module>.*|\1|p' | sort)
if [ -z "$expected" ]; then
    echo "ERROR: no <module> entries found in pluginsystem/plugins/pom.xml" >&2
    exit 1
fi
actual=$(cd "$root/pluginsystem/deployments" && for f in *-0.1-all.jar; do
    [ -e "$f" ] && echo "${f%-0.1-all.jar}"; done | sort)
missing=$(comm -23 <(echo "$expected") <(echo "$actual"))
unexpected=$(comm -13 <(echo "$expected") <(echo "$actual"))
if [ -n "$missing" ] || [ -n "$unexpected" ]; then
    [ -n "$missing" ] && echo "ERROR: plugin JAR(s) missing in pluginsystem/deployments (run 'mvn install'): $(echo $missing)" >&2
    [ -n "$unexpected" ] && echo "ERROR: unexpected plugin JAR(s) in pluginsystem/deployments (no such module in pluginsystem/plugins/pom.xml; delete them): $(echo $unexpected)" >&2
    exit 1
fi
count=0
for name in $expected; do
    cp "$root/pluginsystem/deployments/${name}-0.1-all.jar" "$dist/pluginsystem/plugins/${name}-0.1.jar"
    count=$((count + 1))
done
echo "Copied $count plugin JAR(s) to $dist/pluginsystem/plugins"

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

# dist/ is also the Docker build context; CI zips the release into it, which
# must not be sent to the Docker daemon.
printf '*.zip\n' > "$dist/.dockerignore"
