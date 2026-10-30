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

package de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions;

import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPlugin;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import jep.JepException;
import jep.SharedInterpreter;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.pf4j.Extension;

@Extension
@RDFBean("http://www.ajan.de/behavior/python-ns#LeafNode")
public class PythonLeafNode extends AbstractTDBLeafTask implements NodeExtension {
  @RDFSubject @Getter @Setter private String url;

  @RDF("rdfs:label")
  @Getter
  @Setter
  private String label;

  @RDF("python:input")
  @Getter
  @Setter
  private BehaviorConstructQuery query;

  @RDF("python:script")
  @Getter
  @Setter
  private String script = "";

  @RDF("python:subject")
  @Getter
  @Setter
  private String subject = "";

  @RDF("bt:targetBase")
  @Getter
  @Setter
  private URI targetBase;

  private AJANLogger LOG;

  @Override
  public String toString() {
    return "PythonLeafNode (" + label + ")";
  }

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/python-ns#LeafNode");
  }

  @Override
  public NodeStatus executeLeaf() {
    LOG = this.getObject().getLogger();
    if (!PythonPlugin.isAvailable()) {
      return new NodeStatus(
          Status.FAILED,
          this.getObject().getLogger(),
          this.getClass(),
          this + " FAILED: Python not available: " + PythonPlugin.unavailableReason());
    }
    try {
      LOG.info(this.getClass(), "Executing PythonLeafNode: " + this);
      return runPythonScript();
    } catch (PythonException ex) {
      return new NodeStatus(
          Status.FAILED, this.getObject().getLogger(), this.getClass(), this + " FAILED", ex);
    }
  }

  private NodeStatus runPythonScript() throws PythonException {
    String inputRDF = readInputRDF();
    LOG.info(this.getClass(), "Python script: " + getScript());
    LOG.info(this.getClass(), "Input RDF: " + inputRDF);
    LeafResult result;
    try {
      result = runLeaf(getScript(), inputRDF);
    } catch (JepException ex) {
      throw new PythonException("JEP execution failed!", ex);
    }
    if (!result.stdout().isEmpty()) {
      LOG.info(this.getClass(), "Python stdout: " + result.stdout());
    }
    if (result.status() == Status.FAILED) {
      return new NodeStatus(
          Status.FAILED,
          this.getObject().getLogger(),
          this.getClass(),
          this + " " + result.statusName() + ": " + result.label());
    }
    try {
      writeSolution(result.turtle());
    } catch (IOException | RDFParseException ex) {
      throw new PythonException("Python output is not valid Turtle!", ex);
    }
    return new NodeStatus(
        result.status(),
        this.getObject().getLogger(),
        this.getClass(),
        this + " " + result.statusName() + (result.label().isEmpty() ? "" : ": " + result.label()));
  }

  /**
   * Outcome of {@code AJANlib.execute_leaf_script}.
   *
   * @param status mapped node status (unknown names map to FAILED)
   * @param statusName status name as returned by Python
   * @param label NodeResult label, or the traceback on a Python exception
   * @param turtle output graph as Turtle ("" for none)
   * @param stdout what the script printed on this thread
   */
  public record LeafResult(
      Status status, String statusName, String label, String turtle, String stdout) {}

  /**
   * Runs a leaf script (LeafNode class contract, or legacy globals) in a fresh interpreter on the
   * calling thread. Python exceptions are reported as FAILED results, not thrown.
   */
  public static LeafResult runLeaf(String script, String inputTurtle) throws JepException {
    try (SharedInterpreter jep = new SharedInterpreter()) {
      jep.set("_ajan_script", script == null ? "" : script);
      jep.set("_ajan_input", inputTurtle == null ? "" : inputTurtle);
      jep.exec(
          "import AJANlib\n"
              + "_ajan_result = [str(v) for v in"
              + " AJANlib.execute_leaf_script(_ajan_script, _ajan_input)]");
      String statusName = jep.getValue("_ajan_result[0]", String.class);
      return new LeafResult(
          toStatus(statusName),
          statusName,
          jep.getValue("_ajan_result[1]", String.class),
          jep.getValue("_ajan_result[2]", String.class),
          jep.getValue("_ajan_result[3]", String.class));
    }
  }

  /** Maps a Python status name; anything but SUCCEEDED/RUNNING is FAILED. */
  public static Status toStatus(String name) {
    if ("SUCCEEDED".equals(name)) {
      return Status.SUCCEEDED;
    }
    if ("RUNNING".equals(name)) {
      return Status.RUNNING;
    }
    return Status.FAILED;
  }

  /** Parses the Turtle a script returned; empty input gives an empty model. */
  public static Model parseTurtle(String turtle) throws IOException {
    if (turtle == null || turtle.isEmpty()) {
      return new LinkedHashModel();
    }
    return Rio.parse(
        new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)),
        "http://www.ajan.de",
        RDFFormat.TURTLE);
  }

  private String readInputRDF() throws PythonException {
    return stripTabs(loadBeliefs());
  }

  /** Removes tab characters from the Turtle input (legacy behaviour; no charset conversion). */
  static String stripTabs(String input) {
    return input.replace("\t", "");
  }

  private String loadBeliefs() throws PythonException {
    try {
      if (!getQuery().getSparql().isEmpty()) {
        Repository origin =
            BTUtil.getInitializedRepository(getObject(), getQuery().getOriginBase());
        Model model = getQuery().getResult(origin);
        return ACTNUtil.getModelPayload(model, "text/turtle");
      }
    } catch (UnsupportedEncodingException | QueryEvaluationException | URISyntaxException ex) {
      throw new PythonException("Problems with loading RDF input!", ex);
    }
    return "";
  }

  private void writeSolution(final String input) throws IOException {
    if (input.isEmpty()) {
      LOG.info(this.getClass(), "Empty PythonNode solution!\n");
      return;
    }
    Model model = parseTurtle(input);
    if (getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
      this.getObject().getExecutionBeliefs().update(model);
    } else if (getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
      this.getObject().getAgentBeliefs().update(model);
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
