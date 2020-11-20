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

package de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class StandardBTVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

	// Composit Nodes
	public final static IRI SEQUENCE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Sequence");
	public final static IRI PRIORITY = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Priority");
	public final static IRI PARALLEL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Parallel");
	public final static IRI RANDOM = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Random");

	// Decorator Nodes
	public final static IRI INVERT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Invert");
	public final static IRI UNTIL_FAIL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#UntilFail");
	public final static IRI UNTIL_SUCCESS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#UntilSuccess");
	public final static IRI IS_SUCCEEDING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#IsSucceeding");
	public final static IRI IS_FAILING = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#IsFailing");

	// Leaf Nodes
	public final static IRI SUCCESS = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Success");
	public final static IRI FAIL = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Fail");
	public final static IRI TIMESTAMP = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#Timestamp");

	// Other IRIs
	public final static IRI BT_SUBJECT = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#subject");
	public final static IRI BT_PREDICATE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#predicate");
	public final static IRI BT_VALUETYPE = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#valueType");
}
