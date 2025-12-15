FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine

# Install dependencies
RUN apk update && \
    apk add --no-cache supervisor wget ca-certificates curl libstdc++ python3 py3-pip py3-wheel && \
    apk add --no-cache libstdc++=11.2.1_git20220219-r2 --repository https://dl-cdn.alpinelinux.org/alpine/v3.16/main --allow-untrusted && \
    apk list -I | grep -E 'libstdc++'

WORKDIR /app

COPY executionservice/target/executionservice-0.1.jar /app/executionservice.jar
COPY triplestore/target/triplestore-0.1-war-exec.jar /app/triplestore.jar
COPY docker/supervisord.conf /app/supervisord.conf
COPY .env /app/.env
COPY executionservice/use-case ./executionservice/use-case
COPY pluginsystem/plugins ./pluginsystem/plugins

EXPOSE 8080
EXPOSE 8090

ENV url="http://localhost:8090/rdf4j"
ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV DloadTTLFiles="true"
ENV DpublicHostName="localhost"

ENTRYPOINT ["/usr/bin/supervisord", "-c", "/app/supervisord.conf"]
