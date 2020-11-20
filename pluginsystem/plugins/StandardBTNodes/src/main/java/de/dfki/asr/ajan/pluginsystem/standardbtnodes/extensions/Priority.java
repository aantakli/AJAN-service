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

package de.dfki.asr.ajan.pluginsystem.standardbtnodes.extensions;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:Priority")
public class Priority extends AbstractTDBBranchTask implements NodeExtension {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:hasChildren")
	public List<Task<AgentTaskInformation>> getChildren() {
		return Arrays.asList(children.items);
	}

	@Override
	protected boolean nodeLogic(final EvaluationResult result) {
		return result.getChildResult().equals(Result.SUCCESS);
	}

	public void setChildren(final List<Task<AgentTaskInformation>> children) {
		this.children.clear();
		children.forEach((task) -> {
			this.children.add(task);
		});
	}

	@Override
	public Resource getType() {
		return StandardBTVocabulary.PRIORITY;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Priority (");
		sb.append(url);
		sb.append(" {");
		for (Task<AgentTaskInformation> task : children) {
			sb.append(task.toString());
			sb.append(", ");
		}
		sb.append(" })");
		return sb.toString();
	}

	@Override
	// From gdx.ai Sequence
	public void childFail (Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		++currentChildIndex;
		if (currentChildIndex < children.size) {
			run(); // Run next child
		} else {
			resetSkippedChilds(currentChildIndex);
			fail(); // All children processed, return failure status
		}
	}

	@Override
	// From gdx.ai Sequence
	public void childSuccess (Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		resetSkippedChilds(++currentChildIndex);
		success(); // Return success status when a child says it succeeded
	}

	private void resetSkippedChilds(final int childIndex) {
		for (int i = childIndex; i < children.size; i++) {
			getChild(i).resetTask();
		}
	}
}
