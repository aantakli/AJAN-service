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

import com.badlogic.gdx.ai.btree.SingleRunningChildBranch;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public abstract class AbstractTDBBranchTask extends SingleRunningChildBranch<AgentTaskInformation> implements TreeNode {

	protected final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;
	protected int currentEvaluatingChild;

	@Override
	public Resource getDefinition(final Resource btDefinition) {
		String url = getUrl();
		if (url == null) {
			return btDefinition;
		}
		return vf.createIRI(url);
	}

	@Override
	public Resource getInstance(final Resource btInstance) {
		if (instance == null) {
			instance = BTUtil.getInstanceResource(getUrl(), btInstance);
		}
		return instance;
	}

	@Override
	public void simulate(final SimulationResult result) {
		Direction direction = result.getDirection();
		if (direction.equals(Direction.Up)) {
			currentEvaluatingChild = currentChildIndex;
			currentEvaluatingChild++;
		} else {
			currentEvaluatingChild = 0;
		}
		simulateChilds(result);
		if (direction.equals(Direction.Up)) {
			((TreeNode)control).simulate(result.setDirection(Direction.Up));
		}
	}

	protected void simulateChilds(final SimulationResult result) {
		while (currentEvaluatingChild < children.size) {
			((TreeNode)children.get(currentEvaluatingChild)).simulate(result.setDirection(Direction.Down));
			if (nodeLogic(result)) {
				break;
			}
			currentEvaluatingChild++;
		}
	}

	protected boolean nodeLogic(final SimulationResult result) {
		return !result.getChildResult().equals(Result.SUCCESS);
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		BTUtil.setBranchNodeModel(model, root, mode, this);
		return model;
	}

	protected void runChild(final int child) {
		runningChild = getChild(child);
		runningChild.setControl(this);
		runningChild.start();
		runningChild.run();
	}
}
