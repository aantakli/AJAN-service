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

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.AJANBindingsException;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RDFBean("bt:MatchingAction")
@SuppressWarnings("PMD.ExcessiveImports")
public class MatchingAction extends AbstractTDBBranchTask {

	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:evaluate")
	@Getter @Setter
	private boolean eval;

	@RDF("ajan:variables")
	@Getter @Setter
	private List<Variable> variables;

	@RDF("bt:getOptions")
	@Getter @Setter
	private BehaviorSelectQuery optionsQ;

	@RDF("bt:getActions")
	@Getter @Setter
	private String actionsQ;

	@RDF("bt:inputs")
	@Getter @Setter
	private List<BehaviorQuery> inputs;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	private Action action;
	private static final Logger LOG = LoggerFactory.getLogger(MatchingAction.class);

	@Override
	public Resource getType() {
		return BTVocabulary.MATCHING_ACTION;
	}

	@Override
	public void run() {
		if (runningChild != null) {
			runningChild.run();
			return;
		}
		try {
			if (setMatchingAction()) {
				runChild(0);
			} else {
				fail();
			}
		} catch (RDFBeanException | URISyntaxException ex) {
			fail();
		}
	}

	@SuppressWarnings({"PMD.UnusedLocalVariable","PMD.NullAssignment"})
	private boolean setMatchingAction() throws RDFBeanException, URISyntaxException {
		if (children.size == 0) {
			List<BindingSet> bindings = getOptions();
			Repository repo = getObject().getServiceTDB().getInitializedRepository();
			List<BindingSet> result = getActionsResult(repo, bindings);
			List<IRI> actions = getActionsList(result);
			if (actions.isEmpty()) {
				return false;
			}
			InputModel inputModel = ACTNUtil.getInputModel(this.getInputs(), this.getObject());
			List<AbstractActionDefinition> fittingDesc = removeUnfittingActions(loadDescriptions(repo, actions), inputModel);
			if (fittingDesc.isEmpty()) {
				return false;
			} else {
				createAction(fittingDesc.get(0));
				addChild(action);
				return true;
			}
		}
		return true;
	}

	private List<BindingSet> getOptions() throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(this.getObject(), optionsQ.getOriginBase());
		List<BindingSet> sets = optionsQ.getResult(repo);
		if (sets.isEmpty()) {
			return null;
		} else {
			return optionsQ.getResult(repo);
		}
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
		List<BindingSet> list = new ArrayList();
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

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
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

	private void createAction(final AbstractActionDefinition description) {
		action = new Action();
		description.setAction(action);
		action.setLabel(getLabel());
		action.setActionDefinition(description);
		action.setInputs(inputs);
		action.setTargetBase(targetBase);
		action.setEvaluate(eval);
	}

	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}

	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	@Override
	public void end() {
		if (action != null) {
			action.end();
		}
	}

	@Override
	public void resetTask() {
		action.resetTask();
	}

	@Override
	public String toString() {
		return "FixedAction (" + url + ")";
	}

	@Override
	public void evaluate(final EvaluationResult result) {
		LOG.info("evaluate not implemented yet");
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return action.getModel(model, root, mode);
	}
}
