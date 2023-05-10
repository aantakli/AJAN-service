java -Dtriplestore.initialData.agentFolderPath=use-case/agents -Dtriplestore.initialData.domainFolderPath=use-case/domains -Dtriplestore.initialData.serviceFolderPath=use-case/services -Dtriplestore.initialData.behaviorsFolderPath=use-case/behaviors -Dtriplestore.initialData.nodeDefinitionsFolderPath=use-case/editor/nodeDefinitions -Dtriplestore.initialData.editorDataFolderPath=use-case/editor/editorData -Dpf4j.mode=development -Dserver.port=8080 -DloadTTLFiles=true -Dpf4j.pluginsDir=plugins -Dtriplestore.url=http://localhost:8090/rdf4j -jar executionservice-0.1.jar


pause
