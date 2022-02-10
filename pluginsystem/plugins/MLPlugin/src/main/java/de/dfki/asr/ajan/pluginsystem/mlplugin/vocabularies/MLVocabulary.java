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

package de.dfki.asr.ajan.pluginsystem.mlplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class MLVocabulary {

	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI CLUSTER = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#Cluster");
	public final static IRI ITEMSET = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#Itemset");
	public final static IRI ORDERED_ITEMSET = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#OrderedItemset");
	public final static IRI ASSOCIATION_RULE = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#AssociationRule");

	public final static IRI HAS_MEMBER = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#member");
	public final static IRI HAS_ITEM = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#item");
	public final static IRI HAS_STEP = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#step");
	public final static IRI HAS_CONFIDENCE = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#confidence");
	public final static IRI HAS_SUPPORT = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#support");
	public final static IRI HAS_LIFT = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#lift");
	public final static IRI HAS_ANTECENDENT = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#antecendent");
	public final static IRI HAS_CONSEQUENT = FACTORY.createIRI("http://www.ajan.de/behavior/ml-ns#consequent");
}
