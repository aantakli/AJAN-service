/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes.common;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class BTVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

	// AJAN-Core Nodes
	public final static IRI BEHAVIOR_TREE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#BehaviorTree");
	public final static IRI ROOT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Root");
	public final static IRI ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Action");
	public final static IRI MATCHING_ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#MatchingAction");
	public final static IRI EVALUATE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Evaluate");
	public final static IRI LOAD_BEHAVIOR = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#LoadBehavior");
	public final static IRI CONDITION = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Condition");
	public final static IRI UPDATE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Update");
	public final static IRI WRITE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Write");
	public final static IRI EVENT_PRODUCER = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#EventProducer");
	public final static IRI GOAL_PRODUCER = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#GoalProducer");
	public final static IRI HANDLE_EVENT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#HandleEvent");
	public final static IRI HANDLE_QUEUE_EVENT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#HandleQueueEvent");
	public final static IRI HANDLE_GOAL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#HandleGoal");
	public final static IRI MESSAGE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Message");
	public final static IRI EXECUTOR = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Executor");
	public final static IRI REPEATER = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Repeater");
	public final static IRI DEBUG = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Debug");
	public final static IRI REPORT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Report");

	public final static IRI INPUT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Input");
	public final static IRI CONSTRAINT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Constraint");
	public final static IRI ASK_QUERY = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#AskQuery");
	public final static IRI CONSTRUCT_QUERY = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ConstructQuery");
	public final static IRI UPDATE_QUERY = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#UpdateQuery");
	public final static IRI EVALUATION_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#EvaluationResult");
	public final static IRI BT_EVALUATION_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#BTEvaluationResult");
	public final static IRI RESPONSE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ResponseResult");
	public final static IRI QUERY_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#QueryResult");
	public final static IRI UPDATE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#UpdateResult");
	public final static IRI WRITE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#WriteResult");
	public final static IRI CONSTRAINT_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ConditionResult");
	public final static IRI VALIDATE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ValidateResult");
	public final static IRI CONTENT_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ContentResult");
	public final static IRI GOAL_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#GoalResult");
	public final static IRI INITIAL_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#InitialResult");
	public final static IRI DOMAIN_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#DomainResult");
	public final static IRI STABLE_MODELS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#StableModels");
	public final static IRI INPUT_MODEL_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#InputModelResult");
	public final static IRI QUERY_URI_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#QueryURIResult");
	public final static IRI BEHAVIOR_URI_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#BehaviorURIResult");
	public final static IRI SPARQL_UPDATE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#SPARQLUpdateResult");
	public final static IRI BOOLEAN_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#BooleanResult");
	public final static IRI TUPLE_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#TupleResult");
	public final static IRI GRAPH_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#GraphResult");
	public final static IRI QUERY_BINDING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#QueryBinding");
	public final static IRI GRAPH = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Graph");

	public final static IRI HAS_CHILD = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#hasChild");
	public final static IRI HAS_CHILDREN = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#hasChildren");
	public final static IRI HAS_CONSTRAINTS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#constraints");
	public final static IRI HAS_INPUTS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#inputs");
	public final static IRI HAS_QUERIES = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#queries");
	public final static IRI HAS_ORIGIN_BASE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#originBase");
	public final static IRI HAS_TARGET_BASE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#targetBase");
	public final static IRI HAS_SPARQL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#sparql");
	public final static IRI HAS_SERVICE_ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#serviceAction");
	public final static IRI HAS_ACTION_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#actionResult");
	public final static IRI HAS_STABLE_MODELS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#stableModels");
	public final static IRI HAS_STATE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#state");
	public final static IRI HAS_STEPS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#steps");
	public final static IRI HAS_BINDING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#binding");
	public final static IRI HAS_VALIDATE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#validate");
	public final static IRI HAS_EVENT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#event");
	public final static IRI HAS_QUERY_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#queryResult");
	public final static IRI HAS_GRAPH_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#graphResult");
	public final static IRI HAS_DETAIL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#detail");
	public final static IRI HAS_RESULT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#result");
	public final static IRI HAS_QUERY_BINDING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#queryBinding");
	public final static IRI HAS_NAME = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#name");
	public final static IRI HAS_VALUE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#value");
	public final static IRI BT_NODE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#btNode");
	public final static IRI HAS_TIMESTAMP = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#timeStamp");
	public final static IRI HAS_DEBUGGING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#debugging");

	public final static IRI ST_FRESH = FACTORY.createIRI("http://www.ajan.de/behavior/state-ns#Fresh");
	public final static IRI ST_RUNNING = FACTORY.createIRI("http://www.ajan.de/behavior/state-ns#Running");
	public final static IRI ST_SUCCESS = FACTORY.createIRI("http://www.ajan.de/behavior/state-ns#Success");
	public final static IRI ST_FAIL = FACTORY.createIRI("http://www.ajan.de/behavior/state-ns#Fail");
	public final static IRI ST_UNCLEAR = FACTORY.createIRI("http://www.ajan.de/behavior/state-ns#Unclear");
}
