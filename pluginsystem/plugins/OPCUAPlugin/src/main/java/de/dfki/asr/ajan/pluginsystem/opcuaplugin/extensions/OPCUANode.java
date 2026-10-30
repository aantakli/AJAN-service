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
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import org.eclipse.rdf4j.model.Resource;

/**
 * Shared behaviour of the OPCUA nodes. The RDF-mapped fields ({@code @RDF}, {@code @RDFSubject})
 * stay in the concrete classes: RDFBeans reads field annotations only from the bean class itself.
 */
public abstract class OPCUANode extends AbstractTDBLeafTask {

  /** Runs the node; a {@link PythonException} makes it FAILED. */
  protected abstract NodeStatus run(AJANLogger log) throws PythonException;

  @Override
  public String toString() {
    return getClass().getSimpleName() + " (" + getLabel() + ")";
  }

  @Override
  public NodeStatus executeLeaf() {
    AJANLogger log = this.getObject().getLogger();
    try {
      log.info(this.getClass(), "Executing " + this);
      return run(log);
    } catch (PythonException ex) {
      return new NodeStatus(Status.FAILED, log, this.getClass(), this + " FAILED", ex);
    }
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
