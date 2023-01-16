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
	public final static IRI RULE_SET = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#RuleSet");
	public final static IRI AS_RULES = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#asRules");
	
	public final static IRI IS_FACT = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#Fact");
	public final static IRI HAS_OPPOSITE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#opposite");
	public final static IRI HAS_PREDICATE = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#predicate");
	public final static IRI HAS_FACTS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#facts");
	public final static IRI HAS_TERMS = FACTORY.createIRI("http://www.ajan.de/behavior/asp-ns#terms");
}
