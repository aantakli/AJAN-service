// Prefixes of the @RDF annotations of the OPCUA nodes (vocabulary of node_definitions.ttl).
// Without them RDFBeans takes "opcua:endpointUrl" etc. as IRIs of their own, so nodes from a
// behavior tree get no label, endpoint or parameters.
@RDFNamespaces({
  "ajan = http://www.ajan.de/ajan-ns#",
  "bt = http://www.ajan.de/behavior/bt-ns#",
  "opcua = http://www.ajan.de/behavior/opcua-ns#",
  "rdfs = http://www.w3.org/2000/01/rdf-schema#"
})
package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
