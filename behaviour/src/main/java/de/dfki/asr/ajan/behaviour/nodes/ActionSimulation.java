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

import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationBase;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

/**
 * Internal helper; the public facade is {@link Action}. It simulates the effects of an action on the simulation base.
 */
final class ActionSimulation {

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private final Action action;
	private final AbstractActionDefinition actionDefinition;

	public ActionSimulation(final Action action) {
		this.action = action;
		this.actionDefinition = action.getActionDefinition();
	}

	public Result simulate(final SimulationResult result, final Resource root) {
		SimulationBase repo = result.getRepo();
		InputModel inputModel = getSimulatedInputModel(repo);
		if (!SPARQLUtil.askModel(inputModel, actionDefinition.getConsumable().getSparql())) {
			action.getObject().getLogger().info(action.getClass(), "Input model failed to conform to Action specification.");
			return Result.FAIL;
		}
		Model stmts = result.getResultModel();
		setActionInputs(inputModel,root,stmts);
		return updateSimulationBase(repo,inputModel,root,stmts);
	}

	private InputModel getSimulatedInputModel(final SimulationBase repo) {
		InputModel inputModel = ACTNUtil.getInputModel(action.getInputs(), repo.initialize());
		inputModel.add(ACTNVocabulary.DUMMY, AJANVocabulary.ASYNC_REQUEST_URI, ACTNVocabulary.DUMMY);
		return inputModel;
	}

	private void setActionInputs(final InputModel inputModel, final Resource root, final Model stmts) {
		ParsedTupleQuery selectQuery = ACTNUtil.createSelectQuery(actionDefinition.getVariables(), actionDefinition.getConsumable().getSparql());
		List<BindingSet> bindings = SPARQLUtil.queryModel(inputModel, selectQuery);
		actionDefinition.getVariables().forEach((var) -> {
			bindings.forEach((bindingSet) -> {
				for (Binding binding: bindingSet) {
					if (var.getVarName().equals(binding.getName())) {
						Resource bindRsc = vf.createBNode();
						stmts.add(root, BTVocabulary.HAS_BINDING, bindRsc);
						stmts.add(bindRsc, ACTNVocabulary.SPIN_VAR_NAME, vf.createLiteral(binding.getName()));
						stmts.add(bindRsc, BTVocabulary.HAS_VALUE, binding.getValue());
					}
				}
			});
		});
	}

	@SuppressWarnings("PMD.ExcessiveParameterList")
	private Result updateSimulationBase(final SimulationBase repo, final InputModel inputModel,
					final Resource root, final Model stmts) {
		Model addModel = ACTNUtil.getAddModel(actionDefinition, inputModel);
		Model removeModel = ACTNUtil.getRemoveModel(actionDefinition, inputModel);
		repo.update(addModel, removeModel, false);
		if (ACTNUtil.dummyStatementsInModel(addModel)) {
			stmts.add(root, BTVocabulary.HAS_STATE, BTVocabulary.ST_UNCLEAR);
			return Result.UNCLEAR;
		} else {
			stmts.add(root, BTVocabulary.HAS_STATE, BTVocabulary.ST_SUCCESS);
			return Result.SUCCESS;
		}
	}
}
