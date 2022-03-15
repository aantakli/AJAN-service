FROM maven:3.3.9-jdk-8-alpine

RUN apk update && apk add supervisor

WORKDIR ajan

COPY . ./

RUN mvn clean install -e
RUN chmod +x ./startup.sh

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

ENTRYPOINT ["/bin/bash", "./startup.sh"]