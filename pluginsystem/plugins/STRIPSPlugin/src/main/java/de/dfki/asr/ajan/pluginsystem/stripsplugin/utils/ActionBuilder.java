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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.utils;

import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import graphplan.domain.Operator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;

public final class ActionBuilder {

	private String url;
	private URI actionUri;
	List<BehaviorQuery> inputs;

	private final URIManager uriManager;

	public ActionBuilder(URIManager uriManager) throws URISyntaxException {
		this.uriManager = uriManager;
		this.url = "http://localhost";
		this.actionUri = new URI("http://localhost");
		this.inputs = new ArrayList();
	}

	public ActionBuilder setServiceUrl(String url) {
		this.url = url;
		return this;
	}

	public ActionBuilder setServiceDescription(Operator operator) throws URISyntaxException {
		String uri = uriManager.getURIFromHash(operator.getFunctor());
		actionUri = new URI(uri);
		return this;
	}

	public ActionBuilder setActionInputs(Operator operator) throws URISyntaxException {
		ParsedGraphQuery parsedQuery = createQuery(operator);
		BehaviorQuery query = createBehaviourQuery(parsedQuery);
		inputs.add(query);
		return this;
	}

	private ParsedGraphQuery createQuery(Operator operator) {
		ValueFactory factory = SimpleValueFactory.getInstance();
		List terms = operator.getTerms();
		Set<Resource> resourceSet = new HashSet();
		for(Object term: terms) {
			//----------------
			//ToDo: NOT only URIs could be saved as Terms --> Integers, Strings ...
			//----------------
			String resourceURI = uriManager.getURIFromHash(term.toString());
			Resource resource = factory.createIRI(resourceURI);
			resourceSet.add(resource);
		}
		return SPARQLUtil.getDescribeQuery(resourceSet.iterator());
	}

	private BehaviorQuery createBehaviourQuery(ParsedGraphQuery parsedQuery) throws URISyntaxException {
		BehaviorQuery query = new BehaviorConstructQuery();
		URI agentBeliefbase = new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString());
		query.setOriginBase(agentBeliefbase);
		query.setSparql(SPARQLUtil.renderQuery(parsedQuery));
		return query;
	}

	public Action build() {
		Action action = new Action();
		action.setUrl(url);
		action.setDefinition(actionUri);
		action.setInputs(inputs);
		return action;
	}
}
