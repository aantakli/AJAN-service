/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import java.net.URI;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;

@Extension
@RDFBean("http://www.ajan.de/behavior/opcua-ns#SetupNode")
public class OPCUASetupNode extends OPCUANode implements NodeExtension {
  @RDFSubject @Getter @Setter private String url;

  @RDF("rdfs:label")
  @Getter
  @Setter
  private String label;

  @RDF("opcua:endpointUrl")
  @Getter
  @Setter
  private String endpointUrl = "";

  @RDF("opcua:venvPath")
  @Getter
  @Setter
  private String venvPath = "";

  @RDF("bt:targetBase")
  @Getter
  @Setter
  private URI targetBase;

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/opcua-ns#SetupNode");
  }

  @Override
  protected NodeStatus run(AJANLogger log) throws PythonException {
    PythonExecutionService pythonService = OPCUAMainScript.service();
    Path scriptPath = OPCUAMainScript.script(OPCUAMainScript.EVENTS);
    // venvPath is not used any more (embedded Python environment); kept for old behavior trees.

    // The event client loops until the process ends: run it on its own daemon thread (at most one
    // per endpoint) so the behavior tree is not blocked.
    String endpoint = OPCUAMainScript.normalizeEndpoint(getEndpointUrl());
    boolean started =
        OPCUAMainScript.startEventClient(
            pythonService,
            scriptPath,
            endpoint,
            result ->
                log.info(
                    OPCUASetupNode.class,
                    "OPC UA event client for " + endpoint + " ended: "
                        + OPCUAMainScript.describe(result)));
    log.info(
        this.getClass(),
        started
            ? "Started OPC UA event client for " + endpoint
            : "OPC UA event client for " + endpoint + " is already running");
    return new NodeStatus(Status.SUCCEEDED, log, this.getClass(), this + " SUCCEEDED");
  }
}
