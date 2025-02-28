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

package de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ASPVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI STABLE_MODEL = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#StableModel");
	public final static IRI RULE_SET = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#RuleSet");

	public final static IRI RULE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Rule");
	public final static IRI HEAD = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Head");
	public final static IRI BODY = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Body");
	public final static IRI FACT = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Fact");
	public final static IRI CONSTANT = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Constant");
	public final static IRI CONSTRAINT = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Constraint");
	public final static IRI ATOM = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Atom");
	public final static IRI TERM = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Term");
	public final static IRI VARIABLE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Variable");

	public final static IRI HAS_HEAD = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#head");
	public final static IRI HAS_BODY = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#body");
	public final static IRI HAS_OPPOSITE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#opposite");
	public final static IRI HAS_PREDICATE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#predicate");
	public final static IRI HAS_TERMS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#terms");
	public final static IRI HAS_ATOMS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#atoms");
	public final static IRI HAS_RULES = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#rules");
	public final static IRI HAS_CONSTRAINTS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#constraints");	
	public final static IRI HAS_FACTS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#facts");
	public final static IRI HAS_NAF = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#naf");
	
	public final static IRI HAS_VALUE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#value");
	public final static IRI HAS_INT_VALUE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#intValue");
	public final static IRI HAS_STRING_VALUE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#stringValue");
	public final static IRI AS_RULES = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#asRules");
}
