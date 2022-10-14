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

import de.dfki.asr.ajan.pluginsystem.aspplugin.util.ASPConfig;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	private final String solver = "clingo.exe";
	private final org.slf4j.Logger LOG = LoggerFactory.getLogger(Problem.class);
	
	@Override
	public boolean runSolver(Problem problem) {
		boolean solution = false;
		if (execution < 1) 
			return solution;
		for(int i = 0; i <= execution; i++) {
			if(executeSolver(problem, i)) {
				solution = true;
				break;
			}
		}
		return solution;
	}

	private boolean executeSolver(Problem problem, final int i) {
		List<String> solverCommandLine = new ArrayList();
		solverCommandLine.add("--verbose=0");
		solverCommandLine.add("-c maxtime="+i);
		addCommandLines(solverCommandLine);
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

