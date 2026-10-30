# AJAN-service

This is the Maven based JAVA service to create and run AJAN agents.

## Prerequisites

You will need the following things properly installed on your computer. So please make sure, that mvn and java (also JAVA_HOME) are correctly set in the environmental variables.

* [Java OpenJDK (Version 25)](https://adoptium.net/temurin/releases/?version=25) (needed to build and to run; all reactor modules are compiled with release 25, so an older JDK cannot build or run them — only the external E2E test client in `e2e/` intentionally stays on release 11). Without a local JDK 25 you can build inside Docker (see "Build without a local JDK 25" below)
* [Maven (Version 3.9.x)](https://maven.apache.org/download.cgi)
* [Git](https://git-scm.com/)
* (optional) any Java IDE with Maven support (e.g. IntelliJ IDEA, Eclipse, NetBeans)
* (optional) [Docker](https://docs.docker.com/get-docker/) to build and run the container image

## Installation

### Prebuild Installation
* [Download latest release ZIP](https://github.com/aantakli/AJAN-service/releases)
* Run `startALL.bat` if you are on Windows and `startALL.sh` if you are on Mac/Linux

### Clean Installation
* cmd: `git clone <repository-url>` this repository
* cmd: `cd AJAN-service`
* run `installAJAN.bat` or cmd: `mvn install`

#### Build without a local JDK 25

Build (and test) inside the official Maven/Temurin 25 image; the named volume `ajan-m2` caches the Maven repository between runs:

```
docker run --rm -v "$PWD:/w" -v ajan-m2:/root/.m2 -w /w maven:3.9-eclipse-temurin-25 mvn -B clean install
docker run --rm -v "$PWD:/w" -v ajan-m2:/root/.m2 -w /w maven:3.9-eclipse-temurin-25 mvn -B -f e2e/pom.xml verify
```

### Docker
* [Download latest Docker-Image](https://hub.docker.com/r/aantakli/ajan-service)
* Read Docker [Wiki](https://github.com/aantakli/AJAN-service/wiki/Docker)

Build and run the image locally (Java 25 runtime, triplestore and service in one container, plugins loaded in pf4j `deployment` mode from `/app/pluginsystem/plugins`):

```
mvn install
bash docker/assemble-dist.sh build/dist
docker build -f Dockerfile -t ajan-service:dev build/dist
docker run -d --name ajan -p 8080:8080 -p 8090:8090 ajan-service:dev
```

`docker/assemble-dist.sh` assembles the same distribution directory the CI release ZIP is made from (both JARs, every plugin fat JAR, use-case data, start scripts, `.env`); it is also the Docker build context. The container is healthy once `http://localhost:8090/rdf4j/repositories` answers; the service is at `http://localhost:8080/ajan/agents`.

The E2E suite can run against such a container instead of starting local processes:

```
docker logs -f ajan > build/container.log &
mvn -f e2e/pom.xml verify -Dajan.e2e.serviceUrl=http://localhost:8080/ajan -Dajan.e2e.triplestoreUrl=http://localhost:8090/rdf4j -Dajan.e2e.serviceLogFile="$PWD/build/container.log"
```

## Setup Triplestore

Start AJAN-Triplestore (see below), visit `http://localhost:8090/workbench/` and change (without user and psw) the RDF4J Server URL to `http://localhost:8090/rdf4j`

## RUN

* run `startTriplestore.bat` or cmd: `java -jar triplestore/target/triplestore-0.1.jar --httpPort=8090`
* run `startAJAN.bat` or cmd: `java -Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents 
									-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains 
									-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services 
									-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors 
									-Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/nodeDefinitions
									-Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/editorData
									-Dpf4j.mode=development 
									-Dserver.port=8080 
									-DloadTTLFiles=true 
									-Dpf4j.pluginsDir=pluginsystem/plugins 
									-Dtriplestore.url=http://localhost:8090/rdf4j 
									-jar executionservice/target/executionservice-0.1.jar`

Once AJAN is running, the Swagger UI is available at `http://localhost:8080/` (the OpenAPI 3 description is served at `http://localhost:8080/ajan/swagger.json`).

### VM Variables

* `-Dserver.port=8080`<br>-> Set the port to 8080
* `-DpublicHostName=127.0.0.1`<br>-> Set the host to 127.0.0.1
* `-DusePort=true` <br>-> Use prot in AJAN base URI (Example AJAN base URI would be `http://127.0.0.1:8080/ajan/agents`)
* `-Dtriplestore.url=http://localhost:8090/rdf4j` <br>-> Points to the triplestore 
* `-Dpf4j.pluginsDir=plugins` <br>-> Place where the AJAN plugins are stored
* `-Dpf4j.mode=development` <br>-> If set to 'development', then the plugins must be available as jars to be loaded, otherwise PF4J will try to load the plugins as ZIP files
* `-DloadTTLFiles=true` <br>-> Loading of predefined models with start of AJAN service, this overwrites the existing models in the Triplestore
* `-Dtriplestore.initialData.agentFolderPath=use-case/agents` <br>-> Folder to predefined agents (RDF/Trig)
* `-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains` <br>-> Folder to predefined domain (RDF/Turtle)
* `-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services` <br>-> Folder to predefined services (RDF/Turtle)
* `-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors` <br>-> Folder to predefined behaviors (RDF/Turtle)
   
Pre-modelled agent templates, behaviors, service definitions and domain knowledge can be adapted via the turtle-files under the `ajan-service/executionservice/use-case` folder. If you want to model them graphically, we refer you to the [AJAN-editor](https://github.com/aantakli/AJAN-editor). This editor needs some additional data in additional Triplestores which can be included using the following VM variables:

* `-Dtriplestore.initialData.nodeDefinitionsFolderPath=executionservice/use-case/editor/nodeDefinitions` <br>-> Folder to predefined node definitions (RDF/Turtle)
* `-Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/editorData` <br>-> Folder to predefined editor data (RDF/Turtle)
   
## Python runtime (PythonPlugin)

The PythonPlugin runs Python scripts in-process via JEP. The plugin JAR embeds a generated CPython environment (`nix_env.zip` for Linux, `win_env.zip` for Windows) which is extracted on first use; no system Python is required.

* **Local setup:** the zips are not in Git. Put them into `pluginsystem/plugins/PythonPlugin/python-env/dist/` before `mvn install`, either with `python-env/fetch-ci.sh` (downloads both from the last green `ci-branch` run of the current branch, needs an authenticated `gh`) or `python-env/build-linux.sh` (builds the Linux env with Docker). Without them the plugin builds but reports "Python not available"; `-Dajan.python.requireEnv=true` makes the JEP tests fail instead of skip.
* **External CPython:** set `-Dajan.python.home=<dir>` or `AJAN_PYTHON_HOME` to use an existing CPython that has `jep` installed instead of the embedded env (PythonLeafNode scripts also need `rdflib`; script files such as the OPCUA scripts need only their own packages).
* **Cache location:** the embedded env is extracted to `~/.cache/ajan-python` (Linux) or `%LOCALAPPDATA%\ajan-python` (Windows); override with `-Dajan.python.cacheDir=<dir>`.
* **Adding packages:** add them to `python-env/requirements.in`, run `uv pip compile requirements.in --universal --generate-hashes --python-version 3.13 -o requirements.lock` in `python-env/`, commit both files and rebuild the envs (CI does this automatically).
* **OPCUA logs:** the OPCUA scripts write `logs/` and `result_logs/` under `AJAN_OPCUA_LOG_DIR`, by default `~/.cache/ajan-opcua` (Linux) or `%LOCALAPPDATA%jan-opcua` (Windows).
* **Known limits:** scripts have no timeout; a native crash in a Python extension takes down the whole service; Python is initialised once per JVM.

## Regenerating third-party license files

The `THIRDPARTYLICENSE_<module>.txt` files (one per Maven module, next to the module's `pom.xml`) and the index file `THIRDPARTYLICENSE` are generated with the `license-maven-plugin` (`add-third-party`; `-Dlicense.force=true` makes it rewrite files it considers up to date). The plugin is configured in the root `pom.xml` (pluginManagement) but is intentionally **not** bound to the default build. Regenerate after dependency changes with a single command from the repository root:

```
mvn org.codehaus.mojo:license-maven-plugin:2.7.1:add-third-party -Dlicense.force=true
```

The reports list the compile and runtime dependencies with their licenses; scopes `provided` and `test` (compile-time-only and test-only libraries, which are not part of the distribution) are excluded.

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
