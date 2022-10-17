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

import de.dfki.asr.ajan.behaviour.exception.ActionBindingException;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.*;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.common.*;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.*;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.util.UUID;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RDFBean("bt:Action")
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.TooManyFields"})
public class Action extends AbstractTDBLeafTask {

	public enum Communication {
		SYNCHRONOUS (ACTNVocabulary.SYNCHRONOUS),
		ASYNCHRONOUS (ACTNVocabulary.ASYNCHRONOUS);

		private final IRI value;

		Communication(final IRI resource) {
			value = resource;
		}

		public IRI getValue() {
			return value;
		}
	}

	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:evaluate")
	@Getter @Setter
	private boolean evaluate = true;

	@Getter @Setter
	private AbstractActionDefinition actionDefinition;

	@RDF("bt:inputs")
	@Getter @Setter
	private List<BehaviorQuery> inputs;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("bt:definition")
	@Getter @Setter
	private URI definition;

	@Getter
	private final UUID id;
	@Getter @Setter
	private boolean isRunning;
	@Getter @Setter
	private Communication communication;
	@Setter
	private Model actionResult;
	private final TaskContext context;
	public static final String ASYNCHRONOUS_FRAGMENT = "Asynchronous";

	public Action() {
		this.id = UUID.randomUUID();
		context = new TaskContext();
		evaluate = true;
	}

	@Override
	public Resource getType() {
		return BTVocabulary.ACTION;
	}

	@Override
	public NodeStatus executeLeaf() {
		if (!loadDescription(getObject().getServiceTDB().getInitializedRepository())) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED");
		}
		context.put(this);
		context.put(actionDefinition);
		Status result;
		try {
			TaskStep workflow = actionDefinition.getWorkflow();
			if (workflow == null) {
				return new NodeStatus(Status.RUNNING, this.getObject().getLogger(), this.getClass(), toString() + " RUNNING");
			}
			result = workflow.execute(context);
		} catch (ActionBindingException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED", ex);
		}
		return new NodeStatus(result, this.getObject().getLogger(), this.getClass(), toString() + " " + "Action (" + url + ") " + result);
	}

	@Override
	public void end() {
		if (getStatus() == Status.CANCELLED) {
			BTUtil.sendReport(this.getObject(), toString() + " CANCELLED");
			this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
			TaskStep abortAction = actionDefinition.getAbortWorkflow();
			abortAction.execute(context);
		}
	}

	@Override
	public void resetTask() {
		inputs.stream().forEach((query) -> {
			query.setReset(true);
		});
		if (actionResult != null) {
			actionResult.clear();
		}
		super.resetTask();
	}

	@Override
	public String toString() {
		return "Action (" + label + ")";
	}

	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	private boolean loadDescription(final Repository repo) {
		try {
			if (actionDefinition == null) {
				try (RepositoryConnection conn = repo.getConnection()) {
					RDFBeanManager manager = new BehaviorBeanManager(conn);
					String rootIRI = getDefinition().toString();
					actionDefinition = (AbstractActionDefinition)manager.get(repo.getValueFactory().createIRI(rootIRI));
					actionDefinition.setAction(this);
				}
			}
			return true;
		} catch (Exception ex) {
			this.getObject().getLogger().info(this.getClass(), "Problem when loading Action Description!", ex);
			return false;
		}
	}

	public boolean containesKey() {
		return this.getObject().getActionInformation().containsKey(id.toString());
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		if (!loadDescription(result.getObject().getServiceTDB().getInitializedRepository())) {
			return Result.FAIL;
		}
		EvaluationBase repo = result.getRepo();
		InputModel inputModel = getEvaluatedInputModel(repo);
		if (inputModel == null) {
			return Result.FAIL;
		} else {
			Model stmts = result.getResultModel();
			setActionInputs(inputModel,root,stmts);
			return updateEvaluationBase(repo,inputModel,root,stmts);
		}
	}

	private InputModel getEvaluatedInputModel(final EvaluationBase repo) {
		InputModel inputModel = ACTNUtil.getInputModel(this.getInputs(), repo.initialize());
		inputModel.add(ACTNVocabulary.DUMMY, AJANVocabulary.ASYNC_REQUEST_URI, ACTNVocabulary.DUMMY);
		if (!SPARQLUtil.askModel(inputModel, actionDefinition.getConsumable().getSparql())) {
			this.getObject().getLogger().info(this.getClass(), "Input model failed to conform to Action specification.");
			return null;
		}
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
	private Result updateEvaluationBase(final EvaluationBase repo, final InputModel inputModel,
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

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			Resource resource = getInstance(root.getInstance());
			inputs.forEach((query) -> {
				query.setResultModel(getInstance(root.getInstance()), BTVocabulary.INPUT_MODEL_RESULT, model);
			});
			if (actionResult != null && !actionResult.isEmpty()) {
				Resource resultNode = vf.createBNode();
				model.add(resource, BTVocabulary.HAS_ACTION_RESULT, resultNode);
				model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.RESPONSE_RESULT);
				Resource resultGraph = BTUtil.setGraphResultModel(model, actionResult);
				model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
			}
		}
		return super.getModel(model, root, mode);
	}
}
