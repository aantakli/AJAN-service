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

package de.dfki.asr.ajan.pluginsystem.mlplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.common.AJANVocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public final class MLUtil {

    private MLUtil() {

	}

	public static void performWrite(final AgentTaskInformation info, final URI targetBase, final Model model) throws ConditionEvaluationException {
		try {
			if (targetBase.toString().equals(AJANVocabulary.DOMAIN_KNOWLEDGE.toString())
							|| targetBase.toString().equals(AJANVocabulary.SERVICE_KNOWLEDGE.toString())
							|| targetBase.toString().equals(AJANVocabulary.BEHAVIOR_KNOWLEDGE.toString())) {
				return;
			}
			if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
				info.getExecutionBeliefs().update(model);
			} else if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
				info.getAgentBeliefs().update(model);
			} else {
				updateExternalRepo(new SPARQLRepository(targetBase.toString()), model);
			}
		} catch (QueryEvaluationException ex) {
			throw new ConditionEvaluationException(ex);
		}
	}

	private static void updateExternalRepo(final Repository repo, final Model model) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.begin(IsolationLevels.SERIALIZABLE);
			removeStatementsSupersededBy(model, conn, false);
			addStatementsWith(model,conn);
			conn.commit();
		}
	}

	private static void removeStatementsSupersededBy(final Model model, final RepositoryConnection connection, final boolean mode) {
		model.stream().filter((stmt) -> (!(stmt.getSubject() instanceof BNode) || (stmt.getSubject() instanceof BNode) && mode)).forEachOrdered((stmt) -> {
			connection.remove(stmt.getSubject(), stmt.getPredicate(), null, (Resource) stmt.getContext());
		});
	}

	private static void addStatementsWith(final Model model, final RepositoryConnection connection) {
		model.forEach((stmt) -> {
			connection.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), (Resource) stmt.getContext());
		});
	}
}
