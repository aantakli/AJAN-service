#!/bin/bash
java -Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents -Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains -Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services -Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors -Dtriplestore.initialData.nodeDefinitionsFolderPath=executionservice/use-case/editor/nodeDefinitions -Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/editorData -Dpf4j.mode=development -Dserver.port=8080 -DloadTTLFiles=true -Dpf4j.pluginsDir=pluginsystem/plugins -Dtriplestore.url=http://localhost:8090/rdf4j -jar executionservice/target/executionservice-0.1.jar

echo "Local IP: "
ip addr show | grep "inet " | awk '{print $2}'
echo "Public IP: "
curl -s checkip.dyndns.org | sed -e 's/.*Current IP Address: //' -e 's/<.*$//'