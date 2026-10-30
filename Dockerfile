# syntax=docker/dockerfile:1.6

# glibc-based base required: PythonPlugin unpacks an embedded python-build-
# standalone CPython (python-portable/) plus libjep.so, all linked against
# glibc (PBS needs glibc >= 2.17). Alpine (musl libc) would fail to load them.
# Noble (24.04) is kept as the base; no system Python is needed, the plugin
# brings its own interpreter. The JVM runs with --enable-native-access=
# ALL-UNNAMED (see docker/supervisord.conf) for JEP's JNI loading.
FROM eclipse-temurin:25-jre-noble

# supervisor for running triplestore + executionservice side by side,
# curl for healthchecks/operator use.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        supervisor \
        ca-certificates \
        curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Build context = the distribution directory produced by
# docker/assemble-dist.sh (locally and in CI):
#   bash docker/assemble-dist.sh build/dist
#   docker build -f Dockerfile -t ajan-service:dev build/dist
COPY executionservice-0.1.jar /app/executionservice.jar
COPY triplestore-0.1.jar  /app/triplestore.jar

# Initial RDF data and supervisor config.
COPY use-case ./executionservice/use-case
COPY docker/supervisord.conf /app/supervisord.conf
COPY .env /app/.env

# Self-contained plugin fat JARs (every pluginsystem/deployments/*-0.1-all.jar,
# renamed to pluginsystem/plugins/<plugin>-0.1.jar by assemble-dist.sh) plus
# loader-0.1.jar. pf4j's JarPluginLoader only puts the loaded JAR on the plugin
# classpath, so each plugin must already bundle its non-provided deps. The dist
# directory contains no plugin sources, so pf4j (deployment mode) sees JARs only.
COPY pluginsystem/ /app/pluginsystem

# Pre-create the folder NodeDefinitionsExtension copies into at startup so the
# initial RDF graph walk always finds a real directory even when no plugin
# ships node_definitions.ttl.
RUN mkdir -p /app/executionservice/use-case/editor/nodeDefinitions

EXPOSE 8080 8090

ENV url="http://localhost:8090/rdf4j" \
    repoURL="http://localhost:8090/rdf4j/repositories/" \
    DloadTTLFiles="true" \
    DpublicHostName="localhost"

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -fsS "http://localhost:8090/rdf4j/repositories" >/dev/null || exit 1

ENTRYPOINT ["/usr/bin/supervisord", "-c", "/app/supervisord.conf"]
