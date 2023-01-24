#!/bin/bash
java -Dtriplestore.initialData.agentFolderPath=use-case/agents -Dtriplestore.initialData.domainFolderPath=use-case/domains -Dtriplestore.initialData.serviceFolderPath=use-case/services -Dtriplestore.initialData.behaviorsFolderPath=use-case/behaviors -Dtriplestore.initialData.nodeDefinitionsFolderPath=use-case/editor/nodeDefinitions -Dtriplestore.initialData.editorDataFolderPath=use-case/editor/editorData -Dpf4j.mode=development -Dserver.port=8080 -DloadTTLFiles=true -Dpf4j.pluginsDir=plugins -Dtriplestore.url=http://localhost:8090/rdf4j -jar target/executionservice-0.1.jar

echo "Local IP: "
ip addr show | grep "inet " | awk '{print $2}'
echo "Public IP: "
curl -s checkip.dyndns.org | sed -e 's/.*Current IP Address: //' -e 's/<.*$//'
