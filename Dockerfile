FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine

RUN apk update && apk add supervisor wget ca-certificates curl gcompat libstdc++ ncurses-libs python3 py3-pip
RUN pip install clingo

WORKDIR app

COPY triplestore-0.1-war-exec.jar .
COPY executionservice-0.1.jar .
COPY docker .
COPY executionservice/use-case ./executionservice/use-case
COPY pluginsystem/plugins ./pluginsystem/plugins

RUN chmod +x /app/startup.sh
RUN chmod +x /app/create.sh

WORKDIR /logs
VOLUME logs
VOLUME /app/executionservice/use-case

EXPOSE 8080/tcp
EXPOSE 8090/tcp

ENV url="http://localhost:8090/rdf4j"
ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV DloadTTLFiles="true"
ENV DpublicHostName="localhost"

ENTRYPOINT ["/bin/sh", "/app/startup.sh"]
