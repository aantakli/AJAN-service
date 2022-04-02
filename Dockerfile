FROM maven:3.3.9-jdk-8-alpine

RUN apk update && apk add supervisor && apk add tree && apk add wget && apk add ca-certificates

WORKDIR ajan
VOLUME ajan

RUN mkdir app
COPY / /app

RUN cd /app/ && mvn clean install
RUN cd / && mkdir buildtmp
RUN cd /buildtmp && cp /app/triplestore/target/triplestore-0.1-war-exec.jar .
RUN cd /buildtmp && cp /app/executionservice/target/executionservice-0.1.jar .
RUN cd /buildtmp && cp /app/docker/* .
RUN cd /buildtmp && cp -R /app/executionservice/use-case .
RUN cd /buildtmp && cp -R /app/pluginsystem/plugins .
RUN rm -r /app/* && cd /app && cp -R /buildtmp/* . && rm -r /buildtmp

RUN chmod +x /app/startup.sh
RUN chmod +x /app/create.sh

RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/editor_data.trig
RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/node_definitions.ttl

EXPOSE 8080/tcp
EXPOSE 8090/tcp

ENV url="http://localhost:8090/rdf4j"
ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV Dpf4j_mode="development"
ENV port="8080"
ENV DloadTTLFiles="true"

ENTRYPOINT ["/bin/bash", "/app/startup.sh"]
