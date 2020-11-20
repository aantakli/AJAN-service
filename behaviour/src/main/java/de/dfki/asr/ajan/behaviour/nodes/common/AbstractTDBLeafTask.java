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
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public abstract class AbstractTDBLeafTask extends LeafTask<AgentTaskInformation> implements TreeNode {

	protected final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;

	@Override
	public Status execute() {
		LeafStatus leafStatus = this.executeLeaf();
		Status status = leafStatus.getStatus();
		BTUtil.sendReport(this.getObject(), leafStatus.getLabel());
		return status;
	}

	public abstract LeafStatus executeLeaf();

	@Override
	public abstract void end();

	@Override
	public void evaluate(final EvaluationResult result) {
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

	public abstract Result simulateNodeLogic(final EvaluationResult result, final Resource root);

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
}
