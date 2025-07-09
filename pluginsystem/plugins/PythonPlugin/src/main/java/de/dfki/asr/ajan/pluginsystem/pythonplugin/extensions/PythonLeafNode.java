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
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

@Extension
@RDFBean("python:LeafNode")
public class PythonLeafNode extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter
    @Setter
    private String url;

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

    private Thread thread;
    private Status running = Status.FRESH;
    private AgentTaskInformation info;

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
        switch (running) {
            case FRESH:
            case CANCELLED: {
                runPythonScript();
                running = Status.RUNNING;
                String report = this + " RUNNING";
                return new NodeStatus(Status.RUNNING, this.getObject().getLogger(), this.getClass(), report);
            }
            case SUCCEEDED: {
                running = Status.FRESH;
                String report = this + " SUCCEEDED";
                return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
            }
            default: {
                String report = this + " RUNNING";
                return new NodeStatus(Status.RUNNING, this.getObject().getLogger(), this.getClass(), report);
            }
        }
    }

    private void runPythonScript() {
        info = this.getObject();
        Class<? extends PythonLeafNode> clazz = this.getClass();
        thread = new Thread(() -> {
            running = Status.RUNNING;
            Process p = null;
            try {
                File python = getPython();
                if (python == null || !python.exists()) {
                    LOG.info(clazz, "Python executable not found! Check the venv structure in resources.");
                    running = Status.FAILED;
                } else {
                    // Read main.py from resources
                    String mainScript;
                    try (InputStream in = clazz.getClassLoader().getResourceAsStream("main.py")) {
                        if (in == null) {
                            LOG.info(clazz, "main.py script not found in resources!");
                            running = Status.FAILED;
                            mainScript = null;
                        } else {
                            mainScript = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }

                    if (mainScript != null) {
                        List<String> cmdLine = new ArrayList<>();
                        cmdLine.add(python.getAbsolutePath());
                        cmdLine.add("-c");
                        cmdLine.add(mainScript); // Pass script content directly
                        cmdLine.add(handleQuotes(getScript()));
                        cmdLine.add(readInputRDF());

                        LOG.info(clazz, "Executing Python script with command: " + cmdLine.get(0) + " -c " + cmdLine.get(1) + " " + cmdLine.get(2) + " " + cmdLine.get(3));
                        ProcessBuilder pb = new ProcessBuilder(cmdLine);
                        pb.redirectErrorStream(true);

                        // The venv root is two levels up from the python.exe in win_venv/python/
                        File venvRoot = python.getParentFile().getParentFile();

                        // Construct a comprehensive PYTHONPATH
                        String sitePackages = new File(venvRoot, "Lib/site-packages").getAbsolutePath();
                        String pythonLib = new File(venvRoot, "Lib").getAbsolutePath();
                        String pythonDlls = new File(venvRoot, "DLLs").getAbsolutePath();
                        String pathSeparator = System.getProperty("path.separator");
                        String pythonPath = String.join(pathSeparator, sitePackages, pythonLib, pythonDlls);

                        pb.environment().put("PYTHONPATH", pythonPath);
                        // Force Python to use UTF-8 encoding to prevent filesystem encoding errors.
                        pb.environment().put("PYTHONUTF8", "1");
                        // Set the working directory to the venv root.
                        pb.directory(venvRoot);

                        p = pb.start();
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            extractFormResult(in);
                        }
                        p.waitFor();
                    }
                }
            } catch (IOException | InterruptedException ex) {
                LOG.info(clazz, "Problems with the Python script execution!", ex);
                running = Status.FAILED;
            } catch (PythonException e) {
                LOG.info(clazz, "PythonException occurred!", e);
                running = Status.FAILED;
            } catch (Exception e) {
                LOG.info(clazz, "Unexpected error during Python script execution!", e);
                running = Status.FAILED;
            } finally {
                if (p != null) {
                    p.descendants().forEach(ProcessHandle::destroy);
                    p.destroy();
                }
            }

            if (running == Status.FAILED) {
                BTUtil.sendReport(info, toString() + " FAILED");
            } else {
                BTUtil.sendReport(info, toString() + " SUCCEEDED");
                running = Status.SUCCEEDED;
            }
            info.getBt().run();
        });
        thread.start();
    }

    private File getPython() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        String resourcePath;
        if (isWindows(osName)) {
            // Point to the actual interpreter, not the launcher in Scripts.
            resourcePath = "win_venv/python/python.exe";
        } else if (isUnix(osName)) {
            resourcePath = "nix_venv/bin/python";
        } else {
            // Unsupported OS
            return null;
        }

        java.net.URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            // Resource not found
            return null;
        }

        try {
            // Using File constructor with URI is the correct way to handle paths with spaces etc.
            return new File(resourceUrl.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Failed to convert resource URL to file path", e);
        }
    }


    private boolean isWindows(final String OS) {
        return OS.contains("win");
    }

    private boolean isUnix(final String OS) {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    private String readInputRDF() throws PythonException {
        String input = loadBeliefs();
        String removedControls = input.replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ");
        return handleQuotes(removedControls);
    }

    private String handleQuotes(String input) throws PythonException {
        input = input.replaceAll("\"", "_AJAN_DQ_");
        return input.replaceAll("'", "_AJAN_SQ_");
    }

    private String loadBeliefs() throws PythonException {
        try {
            if (!getQuery().getSparql().isEmpty()) {
                Repository origin = BTUtil.getInitializedRepository(getObject(), getQuery().getOriginBase());
                Model model = getQuery().getResult(origin);
                return ACTNUtil.getModelPayload(model, "text/turtle");
            }
        } catch (UnsupportedEncodingException | QueryEvaluationException | URISyntaxException ex) {
            throw new PythonException("Problems with loading RDF input!", ex);
        }
        return "";
    }

    private void extractFormResult(final BufferedReader in) throws IOException {
        String pyStringOutput = "";
        StringBuilder rdfOutput = new StringBuilder();
        boolean pyLabel = false;
        boolean pyRDF = false;
        String line;
        LOG.info(this.getClass(), "Extracting PythonNode Output:");
        while ((line = in.readLine()) != null) {
            LOG.info(this.getClass(), line);
            switch (line) {
                case "Status.SUCCEEDED":
                    pyStringOutput = this + " SUCCEEDED ";
                    pyLabel = true;
                    continue;
                case "Status.RUNNING":
                    pyStringOutput = this + " RUNNING ";
                    pyLabel = true;
                    continue;
                case "ERROR":
                case "Status.FAILED":
                    pyStringOutput = this + " FAILED ";
                    pyLabel = true;
                    continue;
                default:
                    break;
            }
            if (pyLabel) {
                pyStringOutput = pyStringOutput + line;
                pyLabel = false;
                continue;
            }
            if (line.endsWith("RDF--------")) {
                pyRDF = true;
                continue;
            }
            if (line.endsWith("--------RDF")) {
                pyRDF = false;
                continue;
            }
            if (pyRDF) {
                rdfOutput.append("\n").append(line);
            }
        }
        writeSolution(rdfOutput.toString());
    }

    private void writeSolution(final String input) throws IOException {
        Model model;
        if (input.isEmpty()) {
            LOG.info(this.getClass(), "Empty PythonNode solution!\n");
            return;
        }
        model = Rio.parse(new ByteArrayInputStream(input.getBytes()), "http://www.ajan.de", RDFFormat.TURTLE);
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
