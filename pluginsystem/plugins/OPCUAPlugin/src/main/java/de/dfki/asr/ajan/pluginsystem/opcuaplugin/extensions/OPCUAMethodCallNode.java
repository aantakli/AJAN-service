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
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import java.util.List;

/**
 * An OPCUA node that calls one method through {@code call.py} and waits for it: SUCCEEDED only
 * when the method succeeded, otherwise FAILED with the reason.
 */
public abstract class OPCUAMethodCallNode extends OPCUANode {

  public abstract String getEndpointUrl();

  /** The {@code main.py --call} value, e.g. {@code enable_asset}. */
  protected abstract String method();

  /** The method's {@code main.py} options as (option, value) pairs. */
  protected abstract String[] options();

  @Override
  protected NodeStatus run(AJANLogger log) throws PythonException {
    List<String> argv = OPCUAMainScript.methodArgv(getEndpointUrl(), method(), options());
    log.info(this.getClass(), "Calling OPC UA method: " + argv);
    PythonExecutionResult result =
        OPCUAMainScript.callMethod(
            OPCUAMainScript.service(), OPCUAMainScript.script(OPCUAMainScript.CALL), argv);
    Status status = OPCUAMainScript.statusOf(result);
    return new NodeStatus(
        status,
        log,
        this.getClass(),
        this + " " + status + " (" + OPCUAMainScript.describe(result) + ")");
  }
}
