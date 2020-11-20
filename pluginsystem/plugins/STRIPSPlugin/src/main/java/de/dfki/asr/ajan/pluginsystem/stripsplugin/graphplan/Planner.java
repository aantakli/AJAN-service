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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.graphplan;

import graphplan.PlanSolution;
import graphplan.domain.DomainDescription;
import graphplan.domain.Operator;
import graphplan.flyweight.OperatorFactory;
import graphplan.flyweight.OperatorFactoryException;
import graphplan.graph.PropositionLevel;
import graphplan.graph.memo.mutexes.StaticMutexesTable;
import graphplan.graph.planning.PlanningGraph;
import graphplan.graph.planning.PlanningGraphException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Planner {
	private boolean extractAllPossibleSolutions; // Extract all solutions with minimum length
	private int maxLength; // Extract all solutions with (minimum length + extractAllPossibleWithMaxLength, respective to graph level)
	private long timeout;
	private int maxLevels;

	private DomainDescription domain;
	private PlanningGraph planningGraph;
	private SolutionExtractionVisitor solutionExtraction;
	
	public Planner (int maxLevels, int maxLength, long timeout, boolean extractAllPossibleSolutions, DomainDescription domain) {
		if(maxLevels <= 0) {
			throw new IllegalStateException("maxLevels must be greater than 0");
		}
		if(maxLength < 0) {
			throw new IllegalStateException("extractAllPossibleSolutionsWithMaxLength must be greater than or equal to 0");
		}

		this.timeout = timeout;
		this.maxLevels = maxLevels;
		this.extractAllPossibleSolutions = extractAllPossibleSolutions;
		this.maxLength = maxLength;
		this.domain = domain;
	}

	public PlanSolution plan() throws PlanningGraphException, OperatorFactoryException, TimeoutException {
		createPlaningGraph();
		extractSolutions();
		prepareOperators();
		return calculatePlan();
	}

	private PlanSolution calculatePlan() throws TimeoutException, PlanningGraphException {
		boolean planFound = false;

		while ((!planFound && (this.planningGraph.size() <= maxLevels)) || maxLength >= 0) {
			try {
				this.planningGraph.expandGraph();
			} catch (PlanningGraphException e) {
				System.err.println(e.getMessage());
				return new PlanSolution();
			}
			if (this.planningGraph.goalsPossible(domain.getGoalState(), this.planningGraph.size() - 1)) {
				planFound = this.planningGraph.accept(this.solutionExtraction);
				if (planFound) {
					maxLength--;
				} else {
					if(timeout > 0 && ((TimeoutSolutionExtractionVisitor) solutionExtraction).timedOut()) {
						throw new TimeoutException("No plan possible in " + timeout + " milliseconds");
					}
					if (!planPossible()) {
						throw new PlanningGraphException("Graph has levelled off, plan is not possible.", this.planningGraph.levelOffIndex());
					}
				}
			} else {
				if (this.planningGraph.levelledOff()) {
					throw new PlanningGraphException("Goals are not possible and graph has levelled off, plan is not possible.", this.planningGraph.levelOffIndex());
				}
			}
		}
		return this.solutionExtraction.getPlanSolution();
	}

	private void createPlaningGraph() {
		PropositionLevel initialLevel = new PropositionLevel();
		initialLevel.addPropositions(domain.getInitialState());
		this.planningGraph = new PlanningGraph(initialLevel, new StaticMutexesTable(new ArrayList<>(domain.getOperators())));
	}

	private void extractSolutions() {
		if(timeout > 0) {
			this.solutionExtraction = new TimeoutSolutionExtractionVisitor(domain.getGoalState(), this);
			((TimeoutSolutionExtractionVisitor) solutionExtraction).setTimeout(timeout);
		} else {
			this.solutionExtraction = new SolutionExtractionVisitor(domain.getGoalState(), this);
		}
	}

	public boolean isExtractAllPossibleSolutions() {
		return extractAllPossibleSolutions;
	}

	public void setExtractAllPossibleSolutions(boolean extractAllPossibleSolutions) {
		this.extractAllPossibleSolutions = extractAllPossibleSolutions;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	private void prepareOperators() throws OperatorFactoryException {
		OperatorFactory.getInstance().resetOperatorTemplates();
		for (Operator operator : domain.getOperators()) {
			OperatorFactory.getInstance().addOperatorTemplate(operator);
		}
	}

	private boolean planPossible() {
		if (!this.planningGraph.levelledOff()) {
			return true;
		} else {
			return this.solutionExtraction.levelledOff(this.planningGraph.levelOffIndex());
		}
	}
}
