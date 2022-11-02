# AJAN-service

This is the Maven based JAVA service to create and run AJAN agents.

## Prerequisites

You will need the following things properly installed on your computer.

* [Java OpenJDK (Version 11)](https://adoptium.net/temurin/releases/?version=11)
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
									-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/editor/nodeDefinitions
									-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/editor/editorData
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
* `-Dpf4j.pluginsDir=pluginsystem/plugins` <br>-> Place where the AJAN plugins are stored
* `-Dpf4j.mode=development` <br>-> If set to 'development', then the plugins must be available as jars to be loaded, otherwise PF4J will try to load the plugins as ZIP files
* `-DloadTTLFiles=true` <br>-> Loading of predefined models with start of AJAN service, this overwrites the existing models in the Triplestore
* `-Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents` <br>-> Folder to predefined agents (RDF/Trig)
* `-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains` <br>-> Folder to predefined domain (RDF/Turtle)
* `-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services` <br>-> Folder to predefined services (RDF/Turtle)
* `-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors` <br>-> Folder to predefined behaviors (RDF/Turtle)
   
Pre-modelled agent templates, behaviors, service definitions and domain knowledge can be adapted via the turtle-files under the `ajan-service/executionservice/use-case` folder. If you want to model them graphically, we refer you to the [AJAN-editor](https://github.com/aantakli/AJAN-editor). This editor needs some additional data in additional Triplestores which can be included using the following VM variables:

* `-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/editor/nodeDefinitions` <br>-> Folder to predefined node definitions (RDF/Turtle)
* `-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/editor/editorData` <br>-> Folder to predefined editor data (RDF/Turtle)
   
## Examples

For examples, we refere to the [AJAN-service Wiki](https://github.com/aantakli/AJAN-service/wiki/1-AJAN-Overview).

## Plugins
### MQTT Plugin

MQTT Plugin is the plugin used to communicate using the MQTT Protocol. For more details visit https://mqtt.org/

Following are the different types of Nodes that is part of MQTT Plugin:
- For MQTT Broker:
	- CreateMQTTBroker (Creates the MQTT Broker)
	- DeleteMQTTBroker (Deleted the created MQTT Broker)
- For Communication:
	- PublishMessage (Publish a message to a provided topic)
	- PublishMessageRDF (Publish a message in RDF string to a provided topic)
	- SubscribeTopic (Subscribe to a provided topic and wait until message received or timeout and unsubscribe. Store the message received in knowledge base)
	- SubscribeTopicAlwaysListen (Subscribe to a provided topic and receive message asynchronously. Store the message received in knowledge base)
	- SubscribeTopicProduceEvent (Subscribe to a provided topic and fire an event on receiving message)
	- UnsubscribeTopic (Unsubscribe the subscribed topic)

### Testing Environment:
Inorder to work with MQTT Publish and Subscribe, we need a working MQTT Broker setup. We could also use the CreateMQTTBroker provided here but it might be more convenient to set up our own MQTT Broker.
To do so:
- Install a MQTT Message Broker such as [Mosquitto](https://mosquitto.org/download/)
- Make sure you enter the path to mosquitto in envionmental variable for the following to work without any hassles.
- Run the mosquitto broker via `mosquitto -p <port-number>`.
- In Mosquitto publishing is done by
  `mosquitto_pub -t "<your-topic>" -m "<your-message>" -r`.
  The `-r` is used to retain message which is useful for testing. For further more info, please refer [mosquitto_pub](https://mosquitto.org/man/mosquitto_pub-1.html)
- In Mosquitto subscribing is done by
  `mosquitto_sub -t "<your-topic>"`.
  For further more info, please refer [mosquitto_sub](https://mosquitto.org/man/mosquitto_sub-1.html)
