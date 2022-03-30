FROM maven:3.3.9-jdk-8-alpine

RUN apk update && apk add supervisor && apk add tree && apk add wget && apk add ca-certificates

WORKDIR ajan

RUN mkdir app
COPY / /app

RUN cd /app/ && mvn clean install
RUN chmod +x /app/startup.sh
RUN chmod +x /app/create.sh

RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/editor_data.trig
RUN cd /app && wget https://raw.githubusercontent.com/aantakli/AJAN-editor/master/Triplestore%20Repos/node_definitions.ttl

EXPOSE 8080/tcp
EXPOSE 8090/tcp

ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV agentFolderPath="-Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents"
ENV domainFolderPath="-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains"
ENV serviceFolderPath="-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services"
ENV behaviorsFolderPath="-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors"
ENV Dpf4j_mode="-Dpf4j.mode=development"
ENV port="-Dserver.port=8080"
ENV DloadTTLFiles="-DloadTTLFiles=true"
ENV Dpf4j_pluginsDir="-Dpf4j.pluginsDir=pluginsystem/plugins"
ENV url="-Dtriplestore.url=http://localhost:8090/rdf4j"

RUN export JAVA_OPS="$agentFolderPath $domainFolderPath $serviceFolderPath $behaviorsFolderPath $Dpf4j_mode $port $DloadTTLFiles $Dpf4j_pluginsDir $url"

ENTRYPOINT ["/bin/bash", "/app/startup.sh"]
