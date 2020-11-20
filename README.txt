----------------------

	!!! Please skip the AJAN-Editor related parts until a running version is available !!!

----------------------

1. Install:

   AJAN-Service:
   -> Java OpenJDK (1.8) [https://adoptopenjdk.net/]
   -> Maven 3.3.9 [http://artfiles.org/apache.org/maven/maven-3/3.3.9/]
   -> GIT [https://git-scm.com/book/en/v2/Getting-Started-Installing-Git/]
   (optional) -> Netbeans 11.3 (Java SE) [http://netbeans.apache.org/download/nb113/nb113.html]
 
   AJAN-Editor:
   -> Nodejs 8.6 [https://nodejs.org/download/release/v8.6.0/] >> npm install
   -> Ember [https://ember-cli.com/] >> ember install >> cmd: "npm install -g ember-cli"
   -> Bower [https://bower.io/] >> bower install >> cmd: "npm install -g bower"

----------------------

2. Submodules:

   AJAN-Service:
   -> ajan-service >> cmd: "git submodule update --init --recursive"

----------------------

3. Setup
   
   AJAN-Service:
   (mvn only) -> install via mvn: run "installAJAN.bat"
   (with IDE) -> Open: Netbeans -> File -> Open Project -> "ajan-service"
			  -> Make sure that the needed Java and Maven versions are set in Netbeans: Netbeans >> Tools >> Options >> Java  
			  -> Build ALL modules >> Build: "AJAN Parent Project". If that doesn't work, try the following order: 
				"build-resources" >> "RDFBeans" >> "common" >> "behaviour" >> "triplestore" >> "executionservice" >> "Plugin System"
			  -> Add Variables: executionservice >> properties >> Run >> VM Options: "-Dpf4j.mode=development -Dpf4j.pluginsDir=../pluginsystem/plugins -DLoadTTLFiles=true -Dserver.port=8080"
   
   AJAN-Triplestore:
   -> Start AJAN-Triplestore with the startTriplestore.bat or with the cmd: "java -jar triplestore/target/triplestore-0.1-war-exec.jar --httpPort=8090"
   ->-> Visit "http://localhost:8090/workbench/" and change (without user and psw) the RDF4J Server URL to "http://localhost:8090/rdf4j"
   
   AJAN-Editor:
   -> install npm >> cmd: "npm install"
   -> Start AJAN-Triplestore to add editor related repositories (if not already happened)
   ->-> Create new RDF Repository "editor_data": http://localhost:8090/workbench/repositories/ >> New repositories >> Type: Native Java Store, ID/Title: editor_data
   ->-> Create new RDF Repository "node_definitions": http://localhost:8090/workbench/repositories/ >> New repositories >> Type: Native Java Store, ID/Title: node_definitions
   # Add Node Definitions
   ->-> Copy RDF: ajan-editor >> Triplestore Repos >> node_definitions.ttl
   ->-> http://localhost:8090/workbench/repositories/node_definitions/summary -> ADD -> paste RDF
   # Add Templates
   ->-> Copy RDF: ajan-editor >> Triplestore Repos >> editor_data.trig
   ->-> http://localhost:8090/workbench/repositories/editor_data/summary -> ADD -> -> paste RDF

----------------------

4. RUN

   AJAN-Service:
   -> Run Triplestore: ajan-service -> run: "startTriplestore.bat"
   (mvn only) -> run: "startAJAN.bat"
   (with IDE) -> Netbeans >> executionservice >> Run
   
   (-> the pre-modelled agent templates, behaviors, service definitions and domain knowledge can also be adapted without the editor via the turtle-files under the "ajan-service/executionservice/use-case" folder)
   
   AJAN-Editor:
   -> Run Editor >> cmd: "ember serve" (in editor installation folder)
   -> http://localhost:4200 >> Choose new Triplestore >> type name and URI: http://localhost:8090/rdf4j/repositories/
