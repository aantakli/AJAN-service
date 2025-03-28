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

package de.dfki.asr.ajan.behaviour.nodes.common;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.DebugMode;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public abstract class AbstractTDBLeafTask extends LeafTask<AgentTaskInformation> implements TreeNode {

	protected final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;
	private Status lastStatus = Status.FRESH;
	private Status debugState = Status.FRESH;

	@Override
	public Status execute() {
		Debug debug = this.getObject().getDebug();
		if (debug.isDebugging()) {
			if (null == debug.getMode()) {
				return Status.RUNNING;
			} else {
				if (debug.getMode().equals(DebugMode.STEP) || debugState.equals(Status.RUNNING)) {
					debug.setMode(DebugMode.NONE);
					debugState = runNode();
					return debugState;
				}
				return Status.RUNNING;
			}
		} else {
			debugState = Status.FRESH;
			return runNode();
		}
	}

	private Status runNode() {
		NodeStatus leafStatus = this.executeLeaf();
		Status state = leafStatus.getStatus();
		leafStatus.getLogger().info(leafStatus.getClazz(), leafStatus.getLabel());
		if (leafStatus.getCause() != null) {
			leafStatus.getLogger().info(leafStatus.getClazz(), leafStatus.getCause().getMessage());
		}
		if (!this.getObject().getReportURI().equals("")) {
			BTUtil.reportState(getUrl(), getModel(new LinkedHashModel(), this.getObject().getBt(), BTUtil.ModelMode.DETAIL), this.getObject(), leafStatus);
		}
		return state;
	}

	public abstract NodeStatus executeLeaf();

	@Override
	public abstract void end();

	@Override
	public void simulate(final SimulationResult result) {
		Model stmts = new LinkedHashModel();
		Resource root = BTUtil.setBasicEvaluationResult(stmts, getUrl());
		Result childResult = simulateNodeLogic(result,root);
		result.setChildResult(childResult);
		switch (childResult) {
			case SUCCESS:
				stmts.add(root, BTVocabulary.HAS_STATE, BTVocabulary.ST_SUCCESS);
				result.addResult(root, stmts);
				break;
			case FAIL:
				stmts.add(root, BTVocabulary.HAS_STATE, BTVocabulary.ST_FAIL);
				result.addResult(root, stmts);
				break;
			case UNCLEAR:
				stmts.add(root, BTVocabulary.HAS_STATE, BTVocabulary.ST_UNCLEAR);
				result.addResult(root, stmts);
				break;
			default:
				break;
		}
	}

	public abstract Result simulateNodeLogic(final SimulationResult result, final Resource root);

	@Override
	protected Task<AgentTaskInformation> copyTo(final Task<AgentTaskInformation> task) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Resource getInstance(final Resource btInstance) {
		if (instance == null) {
			instance = BTUtil.getInstanceResource(getUrl(), btInstance);
		}
		return instance;
	}

	@Override
	public Resource getDefinition(final Resource btDefinition) {
		String url = getUrl();
		if (url == null) {
			return btDefinition;
		}
		return vf.createIRI(url);
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		model.add(getInstance(root.getInstance()), RDFS.LABEL, vf.createLiteral(getLabel()));
		return model;
	}

	@Override
	public void reset() {
		lastStatus = getStatus();
		if (!this.getObject().getReportURI().equals("") && lastStatus == Status.CANCELLED) {
			NodeStatus nodeStatus = new NodeStatus(lastStatus, this.getObject().getLogger(), this.getClass(), toString() + " CANCELLED");
			BTUtil.reportState(getUrl(), getModel(new LinkedHashModel(), this.getObject().getBt(), BTUtil.ModelMode.DETAIL), this.getObject(), nodeStatus);
		}
		super.reset();
	}

	public Status getLastStatus() {
		return lastStatus;
	}
}
