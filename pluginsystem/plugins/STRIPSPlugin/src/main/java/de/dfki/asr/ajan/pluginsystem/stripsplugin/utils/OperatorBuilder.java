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
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.TermEvaluationException;
import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.VariableEvaluationException;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import graphplan.domain.Operator;
import graphplan.domain.Proposition;
import graphplan.domain.jason.OperatorImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.VarTerm;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class OperatorBuilder {
	
	private final URIManager uriManager;
	private final URI actionURI;
	private final AgentTaskInformation taskInfos;
	private final Map<String, String> variables = new HashMap();

	public OperatorBuilder(URIManager uriManager, URI actionURI, AgentTaskInformation taskInfos) {
		this.uriManager = uriManager;
		this.actionURI = actionURI;
		this.taskInfos = taskInfos;
	}

	public Operator build()
			throws RDFBeanException, VariableEvaluationException, TermEvaluationException {
		Repository repo = taskInfos.getServiceTDB().getInitializedRepository();
		AbstractActionDefinition action = loadActionDescription(repo);
		return createOperator(action);
	}

	private AbstractActionDefinition loadActionDescription(Repository repo) throws RDFBeanException {
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn);
			return manager.get(repo.getValueFactory().createIRI(actionURI.toString()), AbstractActionDefinition.class);
		}
	}

	private Operator createOperator(AbstractActionDefinition action)
			throws VariableEvaluationException, RDFBeanException, TermEvaluationException {
		List<Proposition> preconds = getPropositions(action.getConsumable().getSparql());
		List<Proposition> effects = getPropositions(action.getProducible().getSparql());
		Structure struct = createOperatorStruct();
		return new OperatorImpl(struct, preconds, effects);
	}

	private Structure createOperatorStruct()
			throws VariableEvaluationException {
		Structure struct = new Structure(uriManager.setActSignatureHash(actionURI.toString()));
		variables.keySet().stream().map((var) -> new VarTerm(var)).forEach((term) -> {
			struct.addTerm(term);
		});
		return struct;
	}

	private List<Proposition> getPropositions(String query)
			throws RDFBeanException, VariableEvaluationException, TermEvaluationException {
		List<Proposition> props = new ArrayList();
		TupleExpr tuple = SPARQLUtil.getTupleExpr(query);
		QueryPropositions visitor = new QueryPropositions();
		visitor.setPropositions(props, tuple, uriManager, variables);
		return props;
	}
}
