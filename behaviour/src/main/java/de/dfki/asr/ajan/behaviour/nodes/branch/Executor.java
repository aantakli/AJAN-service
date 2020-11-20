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
import de.dfki.asr.ajan.behaviour.exception.SelectEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
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
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:Executor")
public class Executor extends AbstractTDBBranchTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:selectChild")
	@Setter @Getter
	private IntValue selectedChild;

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
				int child = selectedChild.getIntValue(this.getObject()).intValueExact();
				runChild(child);
			} else {
				runningChild.run();
			}
		} catch (SelectEvaluationException ex) {
			LOG.info(ex.toString());
			fail();
		}
	}

	@Override
	public void childFail(final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}

	@Override
	public void childSuccess(final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	@Override
	public void evaluate(final EvaluationResult result) {
		EvaluationResult.Direction direction = result.getDirection();
		if (direction.equals(EvaluationResult.Direction.Up)) {
			((TreeNode)control).evaluate(result);
		} else {
			try {
				Repository evalRepo = result.getRepo().initialize();
				int childIndex = selectedChild.getIntValue(evalRepo).intValueExact();
				((TreeNode)this.getChild(childIndex)).evaluate(result.setDirection(Direction.Down));
			} catch (SelectEvaluationException ex) {
				LOG.error("Problems with the Select Query", ex);
				result.setChildResult(EvaluationResult.Result.FAIL);
			}
		}
	}
}
