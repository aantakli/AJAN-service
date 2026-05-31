# syntax=docker/dockerfile:1.6

# --- Stage 1: clean up the plugins layout -------------------------------------
# The dist artifact overlays the prebuilt plugin JARs on top of the source tree,
# leaving behind source directories plus duplicate jars (original-*, *-shaded.jar)
# that confuse pf4j. This stage keeps only the canonical plugin JARs.
FROM alpine:3.19 AS plugins
WORKDIR /staging
COPY pluginsystem/plugins/ ./
RUN set -eux; \
    mkdir -p /out; \
    find . -maxdepth 1 -type f -name "*.jar" \
        ! -name "original-*.jar" \
        ! -name "*-shaded.jar" \
        -exec cp {} /out/ \;; \
    ls -la /out

# --- Stage 2: runtime ---------------------------------------------------------
FROM eclipse-temurin:11-jre-alpine

# supervisor for running triplestore + executionservice side by side,
# curl for healthchecks/operator use, python3 because PythonPlugin needs an
# interpreter present at runtime (the plugin unpacks an embedded venv from
# its own JAR resources).
RUN apk add --no-cache \
        supervisor \
        ca-certificates \
        curl \
        libstdc++ \
        python3 \
    && rm -rf /var/cache/apk/*

WORKDIR /app

# Application JARs. The CI/CD workflow places these at the build context root
# via the dist artifact download, *not* under */target/*.
COPY executionservice-0.1.jar /app/executionservice.jar
COPY triplestore-0.1-war-exec.jar  /app/triplestore.jar

# Initial RDF data and supervisor config.
COPY executionservice/use-case ./executionservice/use-case
COPY docker/supervisord.conf /app/supervisord.conf
COPY .env /app/.env

# Deduplicated plugin JARs from the prep stage.
COPY --from=plugins /out/ /app/pluginsystem/plugins/

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
