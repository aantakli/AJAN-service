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
import de.dfki.asr.ajan.behaviour.events.AJANGoal;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.PluginActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;
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
import org.slf4j.LoggerFactory;

public class OperatorBuilder {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OperatorBuilder.class);

	private final URIManager uriManager;
	private final URI actionURI;
	private final AgentTaskInformation taskInfos;
	private final Map<String, String> variables = new HashMap();
	private final Map<String, AJANOperator> operators;

	public OperatorBuilder(final URIManager uriManager, final Map<String, AJANOperator> operators, final URI actionURI, final AgentTaskInformation taskInfos) {
		this.uriManager = uriManager;
		this.actionURI = actionURI;
		this.taskInfos = taskInfos;
		this.operators = operators;
	}

	public Operator build()
			throws RDFBeanException, VariableEvaluationException, TermEvaluationException {
		Repository serviceRepo = taskInfos.getServiceTDB().getInitializedRepository();
		Repository agentRepo = taskInfos.getAgentTemplatesTDB().getInitializedRepository();
		AJANOperator action = loadActionDescription(serviceRepo);
		if (action.getClazz() == null) {
			action = loadGoalDescription(agentRepo);
		}
		action.setUri(actionURI);
		operators.put(action.getOperatorId(), action);
		return createOperator(action);
	}

	private AJANOperator loadActionDescription(final Repository repo) throws RDFBeanException {
		AJANOperator action = new AJANOperator();
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn);
			AbstractActionDefinition actn = manager.get(repo.getValueFactory().createIRI(actionURI.toString()), ServiceActionDefinition.class);
			if (actn == null) {
				actn = manager.get(repo.getValueFactory().createIRI(actionURI.toString()), PluginActionDefinition.class);
			}
			if (actn != null) {
				action.setClazz(AbstractActionDefinition.class);
				action.setVariables(actn.getVariables());
				action.setConsumable(actn.getConsumable());
				action.setProducible(actn.getProducible());
				action.setOperatorId(uriManager.setActSignatureHash(actionURI.toString()));
			}
		}
		return action;
	}

	private AJANOperator loadGoalDescription(final Repository repo) throws RDFBeanException {
		AJANOperator action = new AJANOperator();
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn);
			AJANGoal goal = manager.get(repo.getValueFactory().createIRI(actionURI.toString()), AJANGoal.class);
			if (goal != null) {
				action.setClazz(AJANGoal.class);
				action.setVariables(goal.getVariables());
				action.setConsumable(goal.getConsumable());
				action.setProducible(goal.getProducible());
				action.setOperatorId(uriManager.setGolSignatureHash(actionURI.toString()));
			}
		}
		return action;
	}

	private Operator createOperator(final AJANOperator action)
			throws VariableEvaluationException, RDFBeanException, TermEvaluationException {
		List<Proposition> preconds = getPropositions(action.getConsumable().getSparql());
		List<Proposition> effects = getPropositions(action.getProducible().getSparql());
		Structure struct = createOperatorStruct(action);
		return new OperatorImpl(struct, preconds, effects);
	}

	private Structure createOperatorStruct(final AJANOperator action)
			throws VariableEvaluationException {
		Structure struct = new Structure(action.getOperatorId());
		variables.keySet().stream().map((var) -> new VarTerm(var)).forEach((term) -> {
			struct.addTerm(term);
		});
		return struct;
	}

	private List<Proposition> getPropositions(final String query)
			throws RDFBeanException, VariableEvaluationException, TermEvaluationException {
		List<Proposition> props = new ArrayList();
		TupleExpr tuple = SPARQLUtil.getTupleExpr(query);
		QueryPropositions visitor = new QueryPropositions();
		visitor.setPropositions(props, tuple, uriManager, variables);
		return props;
	}
}
