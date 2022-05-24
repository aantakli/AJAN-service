FROM openjdk:8-jdk-alpine

RUN apk update && apk add supervisor && apk add wget && apk add ca-certificates && apk add curl

WORKDIR app

COPY triplestore-0.1-war-exec.jar .
COPY executionservice-0.1.jar .
COPY docker .
COPY executionservice/use-case ./executionservice/use-case
COPY pluginsystem/plugins ./pluginsystem/plugins

RUN chmod +x /app/startup.sh
RUN chmod +x /app/create.sh

RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/editor_data.trig
RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/node_definitions.ttl

WORKDIR /logs
VOLUME logs
VOLUME /app/executionservice/use-case

EXPOSE 8080/tcp
EXPOSE 8090/tcp

ENV url="http://localhost:8090/rdf4j"
ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV DloadTTLFiles="true"

ENTRYPOINT ["/bin/sh", "/app/startup.sh"]
