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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import graphplan.domain.Proposition;
import graphplan.domain.jason.PropositionImpl;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Term;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public final class StateLoader {

	private StateLoader() {}

	public static List<Proposition> getState(List<BehaviorQuery> queries, AgentTaskInformation taskInfos, URIManager uriManager)
			throws ConditionEvaluationException, URISyntaxException {
		List<Proposition> state = new ArrayList();
		LinkedHashModel model = getModel(queries, taskInfos);
		model.stream().map((stat) -> {
			PropositionImpl prop = new PropositionImpl(true,uriManager.setPrdTermHash(stat.getPredicate()));
			Term subj = new LiteralImpl(uriManager.setObjTermHash(stat.getSubject()));
			Term obj = new LiteralImpl(uriManager.setObjTermHash(stat.getObject()));
			prop.addTerm(subj);
			prop.addTerm(obj);
			return prop;
		}).forEach((prop -> {
			state.add(prop);
		}));
		return state;
	}

	private static LinkedHashModel getModel(List<BehaviorQuery> queries, AgentTaskInformation taskInfos)
			throws ConditionEvaluationException, URISyntaxException {
		LinkedHashModel model = new LinkedHashModel();
		for (BehaviorQuery query: queries) {
			Repository repo = getInitializedRepository(query.getOriginBase(), taskInfos);
			try (RepositoryConnection conn = repo.getConnection()) {
				conn.begin();
				GraphQuery describeQuery = conn.prepareGraphQuery(query.getSparql());
				try (GraphQueryResult result = describeQuery.evaluate()) {
					while (result.hasNext()) {
						model.add(result.next());
					}
				}
				conn.commit();
			}
		}
		return model;
	}

	private static Repository getInitializedRepository(final URI url, AgentTaskInformation taskInfos)
			throws URISyntaxException, QueryEvaluationException {
		Repository repo;
		if (url.equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
			repo = taskInfos.getAgentBeliefs().getInitializedRepository();
		} else {
			repo = new SPARQLRepository(url.toString());
			repo.initialize();
		}
		return repo;
	}
}
