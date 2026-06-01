# syntax=docker/dockerfile:1.6

# glibc-based base required: PythonPlugin unpacks an embedded portable Python
# (python-portable/) with binaries and libjep.so linked against glibc. Alpine
# (musl libc) would fail to load these even after pip/JEP paths resolve.
# Noble (24.04, glibc 2.39) is required: the embedded libpython3.13.so links
# against GLIBC_2.38, which jammy (22.04, glibc 2.35) does not provide.
FROM eclipse-temurin:11-jre-noble

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

# Application JARs. The CI/CD workflow places these at the build context root
# via the dist artifact download, *not* under */target/*.
COPY executionservice-0.1.jar /app/executionservice.jar
COPY triplestore-0.1-war-exec.jar  /app/triplestore.jar

# Initial RDF data and supervisor config.
COPY executionservice/use-case ./executionservice/use-case
COPY docker/supervisord.conf /app/supervisord.conf
COPY .env /app/.env

# Self-contained plugin fat JARs (from pluginsystem/deployments, renamed to
# <plugin>-0.1.jar under dist/pluginsystem/plugins/ by the CI workflow). pf4j's
# JarPluginLoader only puts the loaded JAR on the plugin classpath, so each
# plugin must already bundle its non-provided deps. The *.jar glob excludes the
# checked-out source subdirectories that still sit alongside the JARs in the
# build context; without it, COPY would flatten their contents (plugin.properties,
# lib/, examples/, ...) into the plugins folder and pf4j in deployment mode
# would refuse to load. Fails loudly if the dist overlay didn't produce JARs.
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
