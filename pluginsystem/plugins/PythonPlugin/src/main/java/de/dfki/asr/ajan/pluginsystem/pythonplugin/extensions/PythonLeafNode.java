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
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import jep.JepException;
import jep.SubInterpreter;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.pf4j.Extension;

/*
m JEP mit portablen Python-Umgebungen (nix_env.zip, win_env.zip) in deinem Java-Projekt korrekt zu nutzen, beachte Folgendes:


Entpacken und Setzen der Umgebungsvariablen
Stelle sicher, dass die jeweilige Umgebung beim Start entpackt und die Umgebungsvariablen korrekt gesetzt werden:


PYTHONHOME auf das Root-Verzeichnis der entpackten Umgebung.
PATH (Windows) bzw. LD_LIBRARY_PATH (Linux) so erweitern, dass die Python-Binaries und -Libraries gefunden werden.
jep.python.home und jep.library.path als System-Properties setzen.
JEP native Bibliotheken
Die JEP-JAR benötigt die nativen Bibliotheken (jep.dll, libjep.so) im Library-Pfad. Diese sollten in deiner portablen Umgebung im richtigen Verzeichnis (Lib, DLLs, lib etc.) liegen.


Java-Startparameter
Falls nötig, gib beim Starten der JVM zusätzliche Parameter an, z. B.:


-Djep.python.home=<Pfad_zur_Python-Umgebung>
-Djep.library.path=<Pfad_zur_JEP-Bibliothek>
-Djava.library.path=<Pfad_zur_JEP-Bibliothek>
Ressourcen laden
Stelle sicher, dass das Entpacken der ZIP-Dateien und das Setzen der Variablen vor der ersten JEP-Nutzung erfolgt (wie in deiner setupEmbeddedPythonEnv()-Methode).


Kompatibilität
Die Python-Umgebung und JEP müssen zur verwendeten Java-Version und zum Betriebssystem passen (32/64 Bit, Versionen).


Fehlerbehandlung
Prüfe, ob beim Starten von JEP Exceptions auftreten, die auf fehlende oder falsch gesetzte Umgebungsvariablen hindeuten.


Zusammengefasst:


Portable Umgebung entpacken
Umgebungsvariablen und System-Properties korrekt setzen
JEP-Bibliotheken im Pfad
Vor JEP-Nutzung alles initialisieren
Damit sollte JEP mit deiner portablen Python-Umgebung funktionieren.
 */

@Extension
@RDFBean("python:LeafNode")
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
    try {
      return runPythonScript();
    } catch (PythonException ex) {
      return new NodeStatus(
          Status.FAILED, this.getObject().getLogger(), this.getClass(), this + " FAILED", ex);
    }
  }

  private NodeStatus runPythonScript() throws PythonException {
    try (SubInterpreter jep = new SubInterpreter()) {
      String script = getScript();
      String inputRDF = readInputRDF();

      jep.set("input_rdf", inputRDF);
      jep.eval(script);

      String status = (String) jep.getValue("status");
      String rdfOutput = (String) jep.getValue("rdf_output");

      Status pyStatus = Status.FAILED;
      if ("SUCCEEDED".equals(status)) {
        pyStatus = Status.SUCCEEDED;
      } else if ("RUNNING".equals(status)) {
        pyStatus = Status.RUNNING;
      }

      writeSolution(rdfOutput);
      return new NodeStatus(
          pyStatus, this.getObject().getLogger(), this.getClass(), this + " " + status);
    } catch (JepException | IOException ex) {
      throw new PythonException("JEP execution failed!", ex);
    }
  }

  private String readInputRDF() throws PythonException {
    String input = loadBeliefs();
    String removedControls =
        input.replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ");
    return handleQuotes(removedControls);
  }

  private String handleQuotes(String input) throws PythonException {
    input = input.replaceAll("\"", "_AJAN_DQ_");
    return input.replaceAll("'", "_AJAN_SQ_");
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
    Model model;
    if (input.isEmpty()) {
      LOG.info(this.getClass(), "Empty PythonNode solution!\n");
      return;
    }
    model =
        Rio.parse(
            new ByteArrayInputStream(input.getBytes()), "http://www.ajan.de", RDFFormat.TURTLE);
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
