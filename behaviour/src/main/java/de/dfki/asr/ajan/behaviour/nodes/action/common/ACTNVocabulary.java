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

package de.dfki.asr.ajan.behaviour.nodes.action.common;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ACTNVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

	// ACTN-Core Resources
	public final static IRI RESOURCE = FACTORY.createIRI("http://www.ajan.de/actn#Resource");
	public final static IRI ACTION = FACTORY.createIRI("http://www.ajan.de/actn#Action");
	public final static IRI FAULT = FACTORY.createIRI("http://www.ajan.de/actn#FAULT");
	public final static IRI PLUGIN_ACTION = FACTORY.createIRI("http://www.ajan.de/actn#PluginAction");
	public final static IRI SERVICE_ACTION = FACTORY.createIRI("http://www.ajan.de/actn#ServiceAction");
	public final static IRI SAFE = FACTORY.createIRI("http://www.ajan.de/actn#Safe");
	public final static IRI UNSAFE = FACTORY.createIRI("http://www.ajan.de/actn#Unsafe");
	public final static IRI IDEMPOTENT = FACTORY.createIRI("http://www.ajan.de/actn#Idenpotent");
	public final static IRI SYNCHRONOUS = FACTORY.createIRI("http://www.ajan.de/actn#Synchronous");
	public final static IRI ASYNCHRONOUS = FACTORY.createIRI("http://www.ajan.de/actn#Asynchronous");
	public final static IRI BINDING = FACTORY.createIRI("http://www.ajan.de/actn#Binding");
	public final static IRI PAYLOAD = FACTORY.createIRI("http://www.ajan.de/actn#Payload");
	public final static IRI ACTION_VARIABLE = FACTORY.createIRI("http://www.ajan.de/actn#ActionVariable");
	public final static IRI CONSUMABLE = FACTORY.createIRI("http://www.ajan.de/actn#Consumable");
	public final static IRI PRODUCIBLE = FACTORY.createIRI("http://www.ajan.de/actn#Producible");

	public final static IRI DUMMY = FACTORY.createIRI("http://www.ajan.de/actn#Dummy");

	// ACTN-Core Predicates
	public final static IRI RUN_BINDING = FACTORY.createIRI("http://www.ajan.de/actn#runBinding");
	public final static IRI ABORT_BINDING = FACTORY.createIRI("http://www.ajan.de/actn#abortBinding");
	public final static IRI VARIABLES = FACTORY.createIRI("http://www.ajan.de/actn#variables");
	public final static IRI COMMUNICATION = FACTORY.createIRI("http://www.ajan.de/actn#communication");
	public final static IRI CATEGORY = FACTORY.createIRI("http://www.ajan.de/actn#category");
	public final static IRI CONSUMES = FACTORY.createIRI("http://www.ajan.de/actn#consumes");
	public final static IRI PRODUCES = FACTORY.createIRI("http://www.ajan.de/actn#produces");
	public final static IRI SPARQL = FACTORY.createIRI("http://www.ajan.de/actn#sparql");

	// SPIN Predicates
	public final static IRI SPIN_VAR_NAME = FACTORY.createIRI("http://spinrdf.org/sp#varName");

	// DCT Predicates
	public final static IRI DCT_DESCRIPTION = FACTORY.createIRI("http://purl.org/dc/terms/description");
}
