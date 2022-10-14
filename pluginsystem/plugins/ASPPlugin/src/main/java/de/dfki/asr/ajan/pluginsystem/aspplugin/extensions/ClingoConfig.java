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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

	@RDF("clingo:envVariable")
    private String envVar;

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

	private final org.slf4j.Logger LOG = LoggerFactory.getLogger(Problem.class);
	
	@Override
	public boolean runSolver(Problem problem) {
		boolean solution = false;
		if (execution < 1) 
			return solution;
		Process p;
		try {
			p = Runtime.getRuntime().exec(getEnvVar());
			p.destroy();
			for(int i = 0; i <= execution; i++) {
				if(executeSolver(problem, i)) {
					solution = true;
					break;
				}
			}
		} catch (IOException ex) {
			LOG.info("Environment variable not accessible!");
			return executeSolver(problem);
		}
		return solution;
	}

	private boolean executeSolver(Problem problem) {
		ArrayList<String> facts = new ArrayList();
		boolean stat = false;
		try (Control control = new Control()) {
			control.add(problem.getRuleset());
			control.ground();
			try (SolveHandle handle = control.solve(Collections.emptyList(), null, SolveMode.YIELD)) {
				while (handle.hasNext()) {
					if (!stat) stat = true;
					facts.add(handle.next().toString());
				}
				problem.setFacts(facts);
            }
			control.cleanup();
		} catch (RuntimeException ex) {
			LOG.info(ex.getMessage());
			return false;
		}
		return stat;
	}

	private boolean executeSolver(Problem problem, final int i) {
		List<String> solverCommandLine = new ArrayList();
		solverCommandLine.add("clingo");
		solverCommandLine.add("--verbose=0");
		solverCommandLine.add("-c maxtime="+i);
		addCommandLines(solverCommandLine);
		boolean stat = false;
		try {
			Process p = Runtime.getRuntime().exec(solverCommandLine.stream().toArray(String[]::new));
			try (OutputStreamWriter out = new OutputStreamWriter(p.getOutputStream())) {
				out.write(problem.getRuleset());
			}
			try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				stat = extractFactsFormSolverResult(in, problem);
				p.waitFor();
				p.destroy(); // might not even be neccessary?
			}
		} catch (IOException | InterruptedException | ClingoException ex) {
			Logger.getLogger(ClingoConfig.class.getName()).log(Level.SEVERE, null, ex);
			return stat;
		}
		return stat;
	}

	private boolean extractFactsFormSolverResult(final BufferedReader in, Problem problem) throws IOException, ClingoException{
 		ArrayList<String> factsFromSolver = new ArrayList();
 		boolean stat = true;
		String line;
		/* example output of the solver:
		 * ... more stupid stuff, looking for the Answer line
		 * Answer: 1
		 * moveTo(schrank)
		 * SATISFIABLE
		 * ... ignore all the other output, we are satisfied
		 */
		while ( (line = in.readLine()) != null) {
			if (line.startsWith("SATISFIABLE")) {
				problem.setFacts(factsFromSolver);
			} else if (line.startsWith("UNSATISFIABLE")) {
				stat = false;
				break;
			} else if (line.toLowerCase().contains("error") || line.startsWith("UNKNOWN") ) {
				throw new ClingoException("Solver error:" + line, null);
			} else {
				// preemptively read all other lines.
				// they may be facts or error messages, but we won't know that
				// until we read either SATISFIABLE or ERROR.
				factsFromSolver.add(line);
			}
		}
 		return stat;	
 	}

	private void addCommandLines(List<String> solverCommandLine) {
		if(time != null && time > 0)
			solverCommandLine.add("--time-limit="+ time);
		if(models != null && models > 0)
			solverCommandLine.add("--models="+ models);
		if(threads != null && threads > 0)
			solverCommandLine.add("--parallel-mode="+ threads);
		if(constants != null && constants.get(0) instanceof ClingoConstant) {
			constants.stream().forEach((constant) -> {
				if (!constant.getName().isEmpty()) {
					StringBuilder line = new StringBuilder();
					line.append("-c ").append(constant.getName())
						.append("=").append(constant.getValue());
					solverCommandLine.add(line.toString());
				}
			});
		}
	}
}

