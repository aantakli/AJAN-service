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
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
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
public class OPCUASetupNode extends AbstractTDBLeafTask implements NodeExtension {
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

  private AJANLogger LOG;

  @Override
  public String toString() {
    return "OPCUASetupNode (" + label + ")";
  }

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/opcua-ns#SetupNode");
  }

  @Override
  public NodeStatus executeLeaf() {
    LOG = this.getObject().getLogger();
    LOG.info(this.getClass(), "================= Test" + getEndpointUrl() + "=================");
    try {
      LOG.info(this.getClass(), "Executing OPCUASetupNode: " + this);
      return runPythonScript();
    } catch (PythonException ex) {
      return new NodeStatus(
          Status.FAILED, this.getObject().getLogger(), this.getClass(), this + " FAILED", ex);
    }
  }

  private NodeStatus runPythonScript() throws PythonException {
    LOG.info(this.getClass(), "Executing Python script for OPCUASetupNode: " + getEndpointUrl());

    PythonExecutionService pythonService = OPCUAMainScript.service();
    Path scriptPath = OPCUAMainScript.script(OPCUAMainScript.EVENTS);
    // venvPath is not used any more (embedded Python environment); kept for old behavior trees.

    // The event client loops until the process ends: run it on its own daemon thread (at most one
    // per endpoint) so the behavior tree is not blocked.
    AJANLogger logger = LOG;
    String endpoint = getEndpointUrl();
    boolean started =
        OPCUAMainScript.startEventClient(
            pythonService,
            scriptPath,
            endpoint,
            result ->
                logger.info(
                    OPCUASetupNode.class,
                    "OPC UA event client for " + endpoint + " ended: "
                        + OPCUAMainScript.describe(result)));
    LOG.info(
        this.getClass(),
        started
            ? "Started OPC UA event client for " + endpoint
            : "OPC UA event client for " + endpoint + " is already running");

    return new NodeStatus(
        Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this + " SUCCEEDED");
  }

  @Override
  public void end() {
    this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
  }

  @Override
  public SimulationResult.Result simulateNodeLogic(SimulationResult result, Resource root) {
    return SimulationResult.Result.SUCCESS;
  }
}
