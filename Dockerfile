# syntax=docker/dockerfile:1.6

# glibc-based base required: PythonPlugin unpacks an embedded portable Python
# (python-portable/) with binaries and libjep.so linked against glibc. Alpine
# (musl libc) would fail to load these even after pip/JEP paths resolve.
# Noble (24.04, glibc 2.39) is required: the embedded libpython3.13.so links
# against GLIBC_2.38, which jammy (22.04, glibc 2.35) does not provide.
FROM eclipse-temurin:21-jre-noble

# supervisor for running triplestore + executionservice side by side,
# curl for healthchecks/operator use, python3 because PythonPlugin needs an
# interpreter present at runtime (the plugin unpacks an embedded venv from
# its own JAR resources).
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        supervisor \
        ca-certificates \
        curl \
        python3 \
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
