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
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import org.pf4j.Extension;

@Extension
@RDFBean("python:LeafNode")
public class PythonLeafNode extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("python:input")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@RDF("python:script")
	@Getter @Setter
	private String script = "";

	@RDF("bt:targetBase")
	@Getter @Setter
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
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED", ex);
		}
	}

	private NodeStatus runPythonScript() throws PythonException {
		try {
			File python = new File(getClass().getClassLoader().getResource("venv/Scripts/python.exe").getFile());
			File main = new File(getClass().getClassLoader().getResource("main.py").getFile());
			if (python.exists() && main.exists()) {
				List<String> cmdLine = new ArrayList();
				cmdLine.add(python.getPath());
				cmdLine.add(main.getPath());
				cmdLine.add("\"" + getScript()+ "\"");
				cmdLine.add("\"" + readInputRDF() + "\"");
				Process p = Runtime.getRuntime().exec(cmdLine.stream().toArray(String[]::new));
				try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					return extractFormResult(in);
				} finally {
					p.waitFor();
					p.descendants().forEach(ph -> {
						ph.destroy();
					});
					p.destroy();
				}
			}
			else {
				throw new PythonException("Files not existing!");
			}
		} catch (IOException | InterruptedException ex) {
			LOG.info(this.getClass(), ex.getMessage());
			throw new PythonException("Problems with the Runtime environment!", ex);
		}
	}

	private String readInputRDF() throws PythonException {
		String input = loadBeliefs();
		String removedControls = input.replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ");
		return removedControls.replaceAll("\"", "'");
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

	private NodeStatus extractFormResult(final BufferedReader in) throws IOException {
		Status pyStatus = Status.FAILED;
		String pyStringOutput = "";
		StringBuilder rdfOutput = new StringBuilder();
		boolean pyLabel = false;
		boolean pyRDF = false;
		String line;
		while ( (line = in.readLine()) != null) {
			LOG.info(this.getClass(), line);
			switch (line) {
				case "Status.SUCCEEDED":
					pyStatus = Status.SUCCEEDED;
					pyStringOutput = toString() + " SUCCEEDED ";
					pyLabel = true;
					continue;
				case "Status.RUNNING":
					pyStatus = Status.RUNNING;
					pyStringOutput = toString() + " RUNNING ";
					pyLabel = true;
					continue;
				case "ERROR":
				case "Status.FAILED":
					pyStatus = Status.FAILED;
					pyStringOutput = toString() + " FAILED ";
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
 		return new NodeStatus(pyStatus, this.getObject().getLogger(), this.getClass(), pyStringOutput);
 	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return EvaluationResult.Result.SUCCESS;
	}
}
