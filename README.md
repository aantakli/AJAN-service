# AJAN-service

This is the Maven based JAVA service to create and run AJAN agents.

## Prerequisites

You will need the following things properly installed on your computer.

* [Java OpenJDK (Version 1.8)](https://adoptopenjdk.net/)
* [Maven (Version 3.3.9)](http://artfiles.org/apache.org/maven/maven-3/3.3.9/)
* [Git](https://git-scm.com/)
* (optional) [Netbeans (Version 11.3)](http://netbeans.apache.org/download/nb113/nb113.html)

## Installation

* cmd: `git clone <repository-url>` this repository
* cmd: `cd AJAN-service`
* cmd: `git submodule update --init --recursive`
* run `installAJAN.bat` or cmd: `mvn install`

## Setup Triplestore

Start AJAN-Triplestore (see below), visit `http://localhost:8090/workbench/` and change (without user and psw) the RDF4J Server URL to `http://localhost:8090/rdf4j`

## RUN

* run `startTriplestore.bat` or cmd: `java -jar triplestore/target/triplestore-0.1-war-exec.jar --httpPort=8090`
* run `startAJAN.bat` or cmd: `java -Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents 
									-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains 
									-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services 
									-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors 
									-Dpf4j.mode=development 
									-Dserver.port=8080 
									-DloadTTLFiles=true 
									-Dpf4j.pluginsDir=pluginsystem/plugins 
									-Dtriplestore.url=http://localhost:8090/rdf4j 
									-jar executionservice/target/executionservice-0.1.jar`
   
Pre-modelled agent templates, behaviors, service definitions and domain knowledge can be adapted without the AJAN-editor via the turtle-files under the `ajan-service/executionservice/use-case` folder.
   