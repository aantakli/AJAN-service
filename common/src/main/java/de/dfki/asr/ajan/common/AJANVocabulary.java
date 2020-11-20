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

package de.dfki.asr.ajan.common;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class AJANVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI AGENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Agent");
	public final static IRI AGENT_THIS = FACTORY.createIRI("http://www.ajan.de/ajan-ns#This");
	public final static IRI AGENT_HAS_NAME = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentName");
	public final static IRI AGENT_HAS_ROOT = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentRoot");
	public final static IRI AGENT_HAS_URI = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentURI");
	public final static IRI AGENT_HAS_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentKnowledge");
	public final static IRI AGENT_HAS_TEMPLATE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentTemplate");

	public final static IRI GENERATED_BNODE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#BNode");

	public final static IRI AJAN_PROCESS_ID = FACTORY.createIRI("http://www.ajan.de/ajan-ns#processId");

	public final static IRI AGENT_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#AgentKnowledge");
	public final static IRI EXECUTION_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#ExecutionKnowledge");
	public final static IRI BEHAVIOR_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#BehaviorKnowledge");
	public final static IRI DOMAIN_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#DomainKnowledge");
	public final static IRI SERVICE_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#ServiceKnowledge");
	public final static IRI TEMPLATE_KNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#TemplateKnowledge");

	public final static IRI AGENT_HAS_REPORT_URI = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentReportURI");
	public final static IRI AGENT_HAS_INITKNOWLEDGE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#agentInitKnowledge");
	public final static IRI AGENT_HAS_INITIALBEHAVIOR = FACTORY.createIRI("http://www.ajan.de/ajan-ns#initialBehavior");
	public final static IRI AGENT_HAS_FINALBEHAVIOR = FACTORY.createIRI("http://www.ajan.de/ajan-ns#finalBehavior");
	public final static IRI AGENT_HAS_BEHAVIOR = FACTORY.createIRI("http://www.ajan.de/ajan-ns#behavior");
	public final static IRI AGENT_HAS_EVENT = FACTORY.createIRI("http://www.ajan.de/ajan-ns#event");
	public final static IRI AGENT_HAS_ENDPOINT = FACTORY.createIRI("http://www.ajan.de/ajan-ns#endpoint");
	public final static IRI AGENT_HAS_ACTION = FACTORY.createIRI("http://www.ajan.de/actn#action");
	public final static IRI AGENT_INITIALISATION = FACTORY.createIRI("http://www.ajan.de/ajan-ns#AgentInitialisation");

	public final static IRI INITIALBEHAVIOR_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#IntialBehavior");
	public final static IRI FINALBEHAVIOR_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#FinalBehavior");
	public final static IRI BEHAVIOR_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Behavior");
	public final static IRI DEFAULT_EVENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#DefaultEvent");
	public final static IRI STRING_EVENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#StringEvent");
	public final static IRI JSON_EVENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#JsonEvent");
	public final static IRI MODEL_EVENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#ModelEvent");
	public final static IRI MODEL_QUEUE_EVENT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#ModelQueueEvent");
	public final static IRI GOAL_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Goal");
	public final static IRI ENDPOINT_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Endpoint");
	public final static IRI GOAL_INFORMATION = FACTORY.createIRI("http://www.ajan.de/ajan-ns#GoalInformation");
	public final static IRI BOUND = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Bound");
	public final static IRI VARIABLE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Variable");

	public final static IRI HAS_GOAL = FACTORY.createIRI("http://www.ajan.de/ajan-ns#goal");
	public final static IRI HAS_CLAUSE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#clause");
	public final static IRI HAS_BINDINGS = FACTORY.createIRI("http://www.ajan.de/ajan-ns#bindings");
	public final static IRI HAS_VARIABLES = FACTORY.createIRI("http://www.ajan.de/ajan-ns#variables");
	public final static IRI HAS_DATA_TYPE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#dataType");
	public final static IRI HAS_CAPABILITY = FACTORY.createIRI("http://www.ajan.de/ajan-ns#capability");

	public final static IRI BEHAVIOR_HAS_BT = FACTORY.createIRI("http://www.ajan.de/ajan-ns#bt");
	public final static IRI BEHAVIOR_HAS_TRIGGER = FACTORY.createIRI("http://www.ajan.de/ajan-ns#trigger");

	public final static IRI EXC_EXCEPTION = FACTORY.createIRI("http://www.ajan.de/ajan-ns#Exception");
	public final static IRI EXC_HAS_CAUSE = FACTORY.createIRI("http://www.ajan.de/ajan-ns#hasCause");

	public final static IRI ASYNC_REQUEST_URI = FACTORY.createIRI("http://www.ajan.de/actn#asyncRequestURI");

	public final static IRI HTTP_PARAMETER_VALUE = FACTORY.createIRI("http://www.w3.org/2011/http#paramValue");
	public final static IRI HTTP_REQUEST_URI = FACTORY.createIRI("http://www.w3.org/2006/http#requestURI");
	public final static IRI HTTP_METHOD_GET = FACTORY.createIRI("http://www.w3.org/2008/http-methods#GET");
	public final static IRI HTTP_METHOD_POST = FACTORY.createIRI("http://www.w3.org/2008/http-methods#POST");
	public final static IRI HTTP_HEADER_ACCEPT = FACTORY.createIRI("http://www.w3.org/2008/http-headers#Accept");
}
