/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.AJANBindingsException;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.common.Variable;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Finds the action definitions that match the options of a {@link MatchingAction}.
 */
final class ActionMatcher {

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private final String actionsQ;
	private final List<Variable> variables;

	public ActionMatcher(final String actionsQ, final List<Variable> variables) {
		this.actionsQ = actionsQ;
		this.variables = variables;
	}

	/**
	 * @return the action definitions matching the given options whose consumable fits the input model;
	 * empty if there is no candidate action at all (the input model is not read in that case)
	 */
	public List<AbstractActionDefinition> findFittingActions(final List<BindingSet> options, final Repository repo,
					final List<BehaviorQuery> inputs, final AgentTaskInformation info) throws RDFBeanException {
		List<BindingSet> result = getActionsResult(repo, options);
		List<IRI> actions = getActionsList(result);
		if (actions.isEmpty()) {
			return new ArrayList<>();
		}
		InputModel inputModel = ACTNUtil.getInputModel(inputs, info);
		return removeUnfittingActions(loadDescriptions(repo, actions), inputModel);
	}

	private List<IRI> getActionsList(final List<BindingSet> result) {
		List<IRI> actions = new ArrayList<>();
		for (BindingSet bindings: result) {
			Value value = bindings.getValue("option");
			if (value instanceof SimpleIRI) {
				actions.add((IRI)value);
			}
		}
		return actions;
	}

	public List<BindingSet> getActionsResult(final Repository repo, final List<BindingSet> bindings) {
		List<BindingSet> list = new ArrayList<>();
		for (BindingSet bindingSet: bindings) {
			try (RepositoryConnection conn = repo.getConnection()) {
				TupleQuery query = conn.prepareTupleQuery(actionsQ);
				assignVariables(query, bindingSet);
				TupleQueryResult result = query.evaluate();
				while (result.hasNext()) {
					list.add(result.next());
				}

			} catch (AJANBindingsException | URISyntaxException ex) {
				return list;
			}
		}
		return list;
	}

	private void assignVariables(final TupleQuery query, final BindingSet bindings) throws AJANBindingsException, URISyntaxException {
		if (variables == null) {
			return;
		}
		if (!variables.isEmpty()) {
			Iterator<Binding> itr = bindings.iterator();
			while (itr.hasNext()) {
				Binding bound = itr.next();
				for (Variable var: variables) {
					if (bound.getName().equals(var.getVarName())) {
						query.setBinding(bound.getName(), getValue(var,bound));
					}
				}
			}
		}
	}

	private Value getValue(final Variable var, final Binding bound) {
		String value = bound.getValue().stringValue();
		String valueType = var.getDataType().toString();
		if (valueType.equals(RDFS.RESOURCE.toString())) {
			return vf.createIRI(value);
		}
		return vf.createLiteral(value, vf.createIRI(valueType));
	}

	private List<AbstractActionDefinition> loadDescriptions(final Repository repo, final List<IRI> options) throws RDFBeanException {
		List<AbstractActionDefinition> descriptions =  new ArrayList<>();
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn);
			for (IRI option: options) {
				AbstractActionDefinition actn = (AbstractActionDefinition)manager.get(option);
				descriptions.add(actn);
			}
		}
		return descriptions;
	}

	private List<AbstractActionDefinition> removeUnfittingActions(final List<AbstractActionDefinition> descriptions, final InputModel inputModel) {
		List<AbstractActionDefinition> fittingDesc = new ArrayList<>();
		for (AbstractActionDefinition desc: descriptions) {
			if (SPARQLUtil.askModel(inputModel, desc.getConsumable().getSparql())) {
				fittingDesc.add(desc);
			}
		}
		return fittingDesc;
	}
}
