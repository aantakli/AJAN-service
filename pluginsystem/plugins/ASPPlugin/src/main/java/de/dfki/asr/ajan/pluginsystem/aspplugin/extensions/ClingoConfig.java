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

package de.dfki.asr.ajan.pluginsystem.aspplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.ClingoException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.ASPConfig;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.pf4j.Extension;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.solving.SolveHandle;
import org.potassco.clingo.solving.SolveMode;
import org.slf4j.LoggerFactory;

@Extension
@RDFBean("clingo:Config")
@Data
public class ClingoConfig implements NodeExtension, ASPConfig {

	@RDF("clingo:solver")
    private String solver;

	@RDF("clingo:time-limit")
	private Integer time;

	@RDF("clingo:execution-limit")
	private Integer execution = 1;

	@RDF("clingo:models")
	private Integer models;

	@RDF("clingo:parallel-mode")
	private Integer threads;

	@RDF("clingo:const")
	private List<ClingoConstant> constants;

	private final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClingoConfig.class);
	
	@Override
	public boolean runSolver(Problem problem) {
		boolean solution = false;
		if (execution < 1) 
			execution = 1;
		try {
			for(int i = 1; i <= execution; i++) {
				if(executeSolver(problem, i)) {
					solution = true;
					break;
				}
			}
		} catch (ClingoException ex) {
			LOG.info("Environment variable not accessible!");
			LOG.info("Executing built in Clingo instead!");
			LOG.info(ex.getMessage());
			for(int i = 1; i <= execution; i++) {
				if(executeInternalSolver(problem, i)) {
					solution = true;
					break;
				}
			}
		}
		return solution;
	}

	private boolean executeSolver(Problem problem, final int i) throws ClingoException {
		LOG.debug("Execute Solver");
		StringBuilder solverCommandLine = new StringBuilder();
		solverCommandLine.append(getCommandLineForSolver());
		solverCommandLine.append(" --verbose=0");
		//solverCommandLine.append(" --models=").append(getModels().toString());
		solverCommandLine.append(" --const maxtime=").append(i);
		addCommandLines(solverCommandLine);
		boolean stat = false;
		try {
			Process p = Runtime.getRuntime().exec(solverCommandLine.toString());
			try (OutputStreamWriter out = new OutputStreamWriter(p.getOutputStream())) {
				out.write(problem.getRuleset());
			}
			try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				stat = extractFactsFormSolverResult(in, problem);
			} finally {
				p.waitFor();
				p.descendants().forEach(ph -> {
					ph.destroy();
				});
				p.destroy();
			}
		} catch (IOException | InterruptedException | ClingoException ex) {
			throw new ClingoException("Problems with the Runtime environment!", ex);
		}
		return stat;
	}

	
	private boolean executeInternalSolver(Problem problem, final int i) {
		LOG.debug("Execute internal Solver");
		ArrayList<String> facts = new ArrayList();
		boolean stat = false;
		int models = getModels();
		try {
			try (Control control = new Control("--verbose", "0", "--const", "maxtime="+i)) {
				control.add(problem.getRuleset());
				control.ground();
				try (SolveHandle handle = control.solve(Collections.emptyList(), null, SolveMode.YIELD)) {
					while (handle.hasNext() && models > 0) {
						if (!stat) stat = true;
						facts.add(handle.next().toString());
						models--;
					}
					problem.setFacts(facts);
				}
				control.cleanup();
			} catch (RuntimeException ex) {
				LOG.debug(ex.getMessage());
				return false;
			}
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			return false;
		}
		return stat;
	}

	private boolean extractFactsFormSolverResult(final BufferedReader in, Problem problem) throws IOException, ClingoException {
 		ArrayList<String> factsFromSolver = new ArrayList();
		ArrayList<String> factsFromPyClingo = new ArrayList();
 		boolean stat = true;
		boolean pyclingo = false;
		boolean readPyclingo = false;
		String line;
		/* example output of the solver:
		 * ... more stupid stuff, looking for the Answer line
		 * Answer: 1
		 * moveTo(schrank)
		 * SATISFIABLE
		 * ... ignore all the other output, we are satisfied
		 */
		while ( (line = in.readLine()) != null) {
			LOG.info(line);
			if (line.startsWith("SATISFIABLE")) {
				if (pyclingo) {
					problem.setFacts(factsFromPyClingo);
					break;
				} else {
					problem.setFacts(factsFromSolver);
				}
			} else if (line.startsWith("UNSATISFIABLE")) {
				stat = false;
				break;
			} else if (line.toLowerCase().contains("error") || line.startsWith("UNKNOWN") ) {
				throw new ClingoException("Solver error:" + line, null);
			} else if (line.startsWith("pyclingo")) {
				pyclingo = true;
			} else if (pyclingo && line.startsWith("Answer")) {
				readPyclingo = true;
			} else if (readPyclingo) {
				factsFromPyClingo.add(line);
			} else {
				// preemptively read all other lines.
				// they may be facts or error messages, but we won't know that
				// until we read either SATISFIABLE or ERROR.
				factsFromSolver.add(line);
			}
		}
 		return stat;	
 	}

	private void addCommandLines(StringBuilder solverCommandLine) {
		if(time != null && time > 0)
			solverCommandLine.append(" --time-limit=").append(time);
		if(models != null && models > 0)
			solverCommandLine.append(" --models=").append(models);
		if(threads != null && threads > 0)
			solverCommandLine.append(" --parallel-mode=").append(threads);
		if(constants != null && constants.get(0) instanceof ClingoConstant) {
			constants.stream().forEach((constant) -> {
				if (!constant.getName().isEmpty()) {
					StringBuilder line = new StringBuilder();
					line.append("- -const ").append(constant.getName())
						.append("=").append(constant.getValue());
					solverCommandLine.append(line.toString());
				}
			});
		}
	}

	private String getCommandLineForSolver() throws ClingoException {
		Properties prop = new Properties();
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream input = null;
		try {
			input = classLoader.getResourceAsStream("envVariables.properties");
			prop.load(input);
			if (getSolver() == null) {
				throw new ClingoException("No solver specified!");
			}
			if (prop.containsKey(getSolver())) {
				return (String)prop.get(getSolver());
			} else {
				throw new ClingoException("Environmental variable not available!");
			}
		} catch (IOException ex) {
			throw new ClingoException("Can't load environmental variable!", ex);
		}
	}
}

