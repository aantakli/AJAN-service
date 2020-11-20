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

import graphplan.Graphplan;
import graphplan.PlanResult;
import graphplan.PlanSolution;
import graphplan.domain.Operator;
import graphplan.domain.Proposition;
import graphplan.graph.*;
import graphplan.graph.memo.MemoizationTable;
import graphplan.graph.planning.PlanningGraph;

import java.util.*;
import java.util.logging.Logger;

/**
 * A visitor class that implements the Graphplan solution extraction
 * algorithm.
 *
 * @author Felipe Meneguzzi
 */
public class SolutionExtractionVisitor implements GraphElementVisitor {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SolutionExtractionVisitor.class.getName());
	protected final List<Proposition> goals;
	protected final MemoizationTable memoizationTable;
	protected PlanningGraph planningGraph;
	protected Stack<Set<Proposition>> subGoalStack;

	// Reference Graphplan problem
	private Planner planner;

	// Represent plan solution
	protected PlanSolution planSolution;

	// List that contains all support actions stacks. Each one contains steps of a given plan found.
	protected List<Stack<Set<Operator>>> supportActionStacks;

	// Temporary stack that will hold steps of plans with intersection steps
	protected Stack<Set<Operator>> globalSupportActionStack;

	// Temporary list that will hold steps indexes of plans with intersection steps
	protected List<Integer> actionLevels;

	public SolutionExtractionVisitor(List<Proposition> goals, Planner planner) {
		this.goals = goals;
		this.planner = planner;
		planSolution = new PlanSolution();
		subGoalStack = new Stack<>();
		supportActionStacks = new ArrayList<>();
		globalSupportActionStack = new Stack<>();
		actionLevels = new ArrayList<>();
		memoizationTable = new MemoizationTable();
	}

	public boolean visitElement(GraphElement element) {
		if (element instanceof PlanningGraph) {
			this.planningGraph = (PlanningGraph) element;
			if (this.planningGraph.getLastGraphLevel().isPropositionLevel()) {
				this.subGoalStack.clear();
				this.supportActionStacks.clear();
				this.planSolution.clear();
				this.globalSupportActionStack.clear();
				this.actionLevels.clear();
				this.subGoalStack.push(new TreeSet<>(this.goals));

				/*TextDrawVisitor visitor = new TextDrawVisitor();
				planningGraph.accept(visitor);
				logger.info("Planning Graph is:");
				logger.info(visitor.toString());*/

				//Whenever we try to iterate in the graph, we need to expand
				//the no goods table to match the size of the graph
				memoizationTable.ensureCapacity(planningGraph.size() / 2);

				if (this.planningGraph.getLastGraphLevel().accept(this)) {
					for (Stack<Set<Operator>> supportActionStack : supportActionStacks) {
						planSolution.add(new PlanResult(supportActionStack));
					}
				}

				//logger.info("Table size is: "+noGoodTableSize());
				//logger.info("Hits         : "+hits);
				//logger.info("Misses       : "+misses);

				return !supportActionStacks.isEmpty();
			} else {
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public boolean visitGraphLevel(GraphLevel graphLevel) {
		if (graphLevel.isActionLevel()) {
			//For every action level we visit, we add a list
			//of actions to the support action stack to be
			//used in the iteration of the proposition level
			return graphLevel.getPrevLevel().accept(this);
		} else {
			if (graphLevel.getPrevLevel() == null) {
				//We hit the first level, return
				return true;
			}
			PropositionLevel propositionLevel = (PropositionLevel) graphLevel;
			Set<Proposition> subGoals = new TreeSet<>(this.subGoalStack.peek());

			//First, check the no goods table
			if (this.memoizationTable.isNoGood(this.subGoalStack.peek(), propositionLevel.getIndex())) {
				//And not even creep if the goals are no good
				return false;
			}

			//Then check if the goals are conceptually possible
			//If the goals are possible in this level
			if (propositionLevel.goalsPossible(subGoals)) {
				//Then push a set of potential actions for them
				//And try to fill this up with operators
				//supportActionStack.push(new LinkedHashSet<Operator>());
				//logger.fine("At level "+propositionLevel.getIndex()+", trying to achieve "+subGoalStack.peek());
				
				/*if(!planFound) {
					this.supportActionStack.pop();
				}*/
				return this.visitPropositionLevel(propositionLevel, subGoals);
			} else {
				//When memoization is in, check this
				return false;
			}
		}

		//return false;
	}

	private boolean visitPropositionLevel(PropositionLevel propositionLevel, Set<Proposition> subGoals) {
		//If we have reached the first proposition level
		//We have found a plan
		//TODO check this for redundancy
		if (propositionLevel.getPrevLevel() == null) {
			return true;
		}

		if (this.memoizationTable.isNoGood(subGoals, propositionLevel.getIndex())) {
			return false;
		}

		final ActionLevel actionLevel = (ActionLevel) propositionLevel.getPrevLevel();

		ArrayList<Proposition> subGoalsSorted = new ArrayList<>(subGoals);
		
		/* Heuristic: sort goals by proposition that appears earliest in the planning graph */
		if (Graphplan.sortGoals) {
			Collections.sort(subGoalsSorted, (o1, o2) -> (o1.getIndex() > o2.getIndex() ? -1 : (o1.getIndex() == o2.getIndex() ? 0 : 1)));
		}

		/* Heuristic: select firstly propositions that leads to the smallest set of resolvers */
		if (Graphplan.propositionsSmallest) {
			Collections.sort(subGoalsSorted, (o1, o2) -> {
				int o1Size = actionLevel.getGeneratingActions(o1).size();
				int o2Size = actionLevel.getGeneratingActions(o2).size();
				return (o1Size < o2Size ? -1 : (o1Size == o2Size ? 0 : 1));
			});
		}

		boolean planFound = this.search(subGoalsSorted, new HashSet<>(), actionLevel, new HashSet<>());
		if (!planFound) {
			this.memoizationTable.addNoGood(subGoals, propositionLevel.getIndex());
			this.subGoalStack.pop();
			return false;
		} else {
			return true;
		}
	}

	public boolean search(List<Proposition> subGoals, Set<Operator> operators, ActionLevel actionLevel, Set<Operator> mutex) {
		boolean planFound = false;

		if (subGoals.isEmpty()) {
			Set<Proposition> newSubGoals = determineSubgoals(operators);
			this.subGoalStack.push(newSubGoals);
			planFound = this.visitPropositionLevel((PropositionLevel) actionLevel.getPrevLevel(), newSubGoals);
			if (planFound) {
				globalSupportActionStack.push(operators);
				actionLevels.add(actionLevel.getIndex());

				// If this is the last action level, use globalSupportActionStack and actionLevels to build the final support actions stacks
				if (actionLevel.getIndex() == (planningGraph.getLastGraphLevel().getIndex() - 1)) {
					buildPlanResults();
				}
			}
		} else {
			List<Operator> resolvers = actionLevel.getGeneratingActions(this.popGoal(subGoals));
			resolvers = this.andNotMutexes(resolvers, mutex);
			while (!resolvers.isEmpty()) {
				Operator resolver = this.popResolver(resolvers);
				Set<Operator> newOperators = new HashSet<>(operators);
				newOperators.add(resolver);
				List<Proposition> newSubGoals = this.getSubGoals(resolver, subGoals);
				Set<Operator> newMutex = new HashSet<>(mutex);
				if (actionLevel.getMutexes().get(resolver) != null)
					newMutex.addAll(actionLevel.getMutexes().get(resolver));

				if (this.search(newSubGoals, newOperators, actionLevel, newMutex)) {
					planFound = true;

					// If we have found a plan, and we not intend to find all of them, return
					if (!planner.isExtractAllPossibleSolutions()) {
						return true;
					}
				}
			}
		}
		return planFound;
	}

	private void buildPlanResults() {
		List<Set<Operator>> temp = new ArrayList<>();
		for (Set<Operator> set : globalSupportActionStack) {
			temp.add(set);
		}

		for (int i = 0; i < actionLevels.size(); i++) {
			int currentI = actionLevels.get(i);
			if (currentI == 1) {
				Stack<Set<Operator>> aux = new Stack<>();
				int current = 0;
				for (int j = i; j < temp.size(); j++) {
					if (actionLevels.get(j) > current) {
						aux.push(temp.get(j));
						current = actionLevels.get(j);
					}
				}
				supportActionStacks.add(aux);
			}
		}
		this.globalSupportActionStack.clear();
		actionLevels.clear();
	}

	/**
	 * andNot
	 *
	 * @param resolvers
	 * @param mutex
	 * @return
	 */
	private List<Operator> andNotMutexes(List<Operator> resolvers, Set<Operator> mutex) {
		List<Operator> andNot = new ArrayList<>();

		for (Operator op : resolvers) {
			if (!mutex.contains(op)) andNot.add(op);
		}

		return andNot;
	}

	/**
	 * andNot
	 *
	 * @param resolver
	 * @param subGoals
	 * @return
	 */
	private List<Proposition> getSubGoals(Operator resolver, List<Proposition> subGoals) {
		List<Proposition> andNot = new ArrayList<>();

		for (Proposition p : subGoals) {
			if (!resolver.getEffects().contains(p)) andNot.add(p);
		}
		return andNot;
	}

	private Proposition popGoal(List<Proposition> subGoals) {
		return subGoals.get(0);
	}

	private Operator popResolver(List<Operator> resolvers) {
		return resolvers.remove(0);
	}

	/**
	 * Tries to determine early one if a set of actions will not be minimal;
	 *
	 * @param proposition
	 * @param actions
	 * @return
	 */
	protected final boolean alreadySatisfied(Proposition proposition, Set<Operator> actions) {
		for (Operator operator : actions) {
			if (operator.getEffects().contains(proposition))
				return true;
		}
		return false;
	}

	/**
	 * Gets the preconditions for the operators
	 * given as parameter
	 *
	 * @param operators
	 * @return
	 */
	private Set<Proposition> determineSubgoals(Set<Operator> operators) {
		final TreeSet<Proposition> subGoals = new TreeSet<>();

		for (Operator operator : operators) {
			for (Proposition proposition : operator.getPreconds()) {
				subGoals.add(proposition);
			}
		}

		return subGoals;
	}

	/**
	 * Returns whether or not the memoization table has levelled off. This just
	 * forwards the call to the memoization table.
	 * <p>
	 * XXX Review this method, I suspect it may be wrong according to Blum and
	 * Furst's paper.
	 *
	 * @param graphLevel The last graph level
	 * @return
	 */
	public final boolean levelledOff(int graphLevel) {
		return memoizationTable.levelledOff(graphLevel);
	}

	/**
	 * Returns the plans resulting from this solution extraction cycle.
	 *
	 * @return
	 */
	public PlanSolution getPlanSolution() {
		return planSolution;
	}
}
