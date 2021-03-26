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

### VM Variables

* `-Dserver.port=8080`<br>-> Set the port to 8080
* `-DpublicHostName=127.0.0.1`<br>-> Set the host to 127.0.0.1
* `-DusePort=true` <br>-> Use prot in AJAN base URI (Example AJAN base URI would be `http://127.0.0.1:8080/ajan/agents`)
* `-Dtriplestore.url=http://localhost:8090/rdf4j` <br>-> Points to the triplestore 
* `-DloadTTLFiles=true` <br>-> Loading of predefined models with start of AJAN service, this overwrites the existing models in the Triplestore
* `-Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents` <br>-> Folder to predefined agents (RDF/Trig)
* `-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains` <br>-> Folder to predefined domain (RDF/Turtle)
* `-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services` <br>-> Folder to predefined services (RDF/Turtle)
* `-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors` <br>-> Folder to predefined behaviors (RDF/Turtle)
   
Pre-modelled agent templates, behaviors, service definitions and domain knowledge can be adapted via the turtle-files under the `ajan-service/executionservice/use-case` folder. If you want to model them graphically, we refer you to the [AJAN-editor](https://github.com/aantakli/AJAN-editor).
   
## Examples

For examples, we refere to the [AJAN-service Wiki](https://github.com/aantakli/AJAN-service/wiki/1-AJAN-Overview).
