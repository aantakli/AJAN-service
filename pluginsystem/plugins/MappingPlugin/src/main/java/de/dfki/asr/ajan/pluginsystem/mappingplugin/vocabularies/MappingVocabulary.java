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

package de.dfki.asr.ajan.pluginsystem.mappingplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class MappingVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI MP_POSITION = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#Position");
	public final static IRI MP_3VECTOR = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#3Vector");
	public final static IRI MP_DISTANCE = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#Distance");
	public final static IRI DATETIME_PREDICATE = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#DateTimePredicate");
	
	public final static IRI MIME_TEXT_TUTRLE = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#TextTurtle");
	public final static IRI MIME_JSON = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#Json");
	public final static IRI MIME_JSON_LD = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#JsonLD");
	public final static IRI MIME_RDF_XML = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#RDFxml");

	public final static IRI MP_HAS_VALUE = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#value");
	public final static IRI MP_HAS_X = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#x");
	public final static IRI MP_HAS_Y = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#y");
	public final static IRI MP_HAS_Z = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#z");
	public final static IRI HAS_TIMEFORMAT = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#timeFormat");
	
	public final static IRI HAS_JSON_STRING = FACTORY.createIRI("http://www.ajan.de/ajan-mapping-ns#jsonString");

	// RML Vocabulary
	public final static IRI RML_REFERENCE_FORMULATION = FACTORY.createIRI("http://semweb.mmlab.be/ns/rml#referenceFormulation");
}
