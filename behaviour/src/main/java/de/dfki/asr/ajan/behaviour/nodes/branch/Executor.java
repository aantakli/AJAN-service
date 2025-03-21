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

package de.dfki.asr.ajan.behaviour.nodes.branch;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.SelectSimulationException;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:Executor")
public class Executor extends AbstractTDBBranchTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:sequence")
	@Setter @Getter
	private boolean sequence;

	@RDF("bt:selectChild")
	@Setter @Getter
	private IntValue selectedChild;

	private List<Integer> executionOrder = new ArrayList<>();
	private int childIndex;

	@RDF("bt:hasChildren")
	public List<Task<AgentTaskInformation>> getChildren() {
		return Arrays.asList(children.items);
	}

	public void setChildren(final List<Task<AgentTaskInformation>> newChildren) {
		children.clear();
		newChildren.stream().forEach((task) -> {
			children.add(task);
		});
	}

	@Override
	public Resource getType() {
		return BTVocabulary.EXECUTOR;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(26);
		sb.append("Executor (").append(url).append(" { ");
		for (Task<AgentTaskInformation> task : children) {
			sb.append(task.toString());
			sb.append(", ");
		}
		sb.append(" })");
		return sb.toString();
	}

	@Override
	public void run () {
		try {
			if (runningChild == null) {
				executionOrder = selectedChild.getIntValue(this.getObject());
				childIndex = 0;
				runChildByIndex();
			} else {
				runningChild.run();
			}
		} catch (SelectSimulationException ex) {
			LOG.info(ex.toString());
			fail();
		}
	}

	private void runChildByIndex() {
		int child = executionOrder.get(childIndex);
		if (child < 0 || child > children.size) {
			LOG.info(toString() + "FAILED");
			LOG.info("No matching child found!");
			fail();
		} else {
			runChild(child);
		}
	}

	@Override
	public void childFail(final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		childIndex += 1;
		if (!sequence && executionOrder.size() > childIndex) {
			runChildByIndex();
		} else {
			resetSkipedChilds();
			childIndex = 0;
			fail();
		}
	}

	@Override
	public void childSuccess(final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		childIndex += 1;
		if (sequence && executionOrder.size() > childIndex) {
			runChildByIndex();
		} else {
			resetSkipedChilds();
			childIndex = 0;
			success();
		}
	}

	private void resetSkipedChilds() {
		for (int i = childIndex; i < executionOrder.size(); i++) {
			getChild(executionOrder.get(i)).resetTask();
		}
	}

	@Override
	public void simulate(final SimulationResult result) {
		SimulationResult.Direction direction = result.getDirection();
		if (direction.equals(SimulationResult.Direction.Up)) {
			((TreeNode)control).simulate(result);
		} else {
			try {
				Repository evalRepo = result.getRepo().initialize();
				int child = selectedChild.getIntValue(evalRepo).get(0);
				((TreeNode)this.getChild(child)).simulate(result.setDirection(Direction.Down));
			} catch (SelectSimulationException ex) {
				LOG.error("Problems with the Select Query", ex);
				result.setChildResult(SimulationResult.Result.FAIL);
			}
		}
	}
}
