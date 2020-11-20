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
@RDFBean("bt:Sequence")
public class Sequence extends AbstractTDBBranchTask implements NodeExtension {
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

	public void setChildren(final List<Task<AgentTaskInformation>> newChildren) {
		children.clear();
		newChildren.forEach((task) -> {
			children.add(task);
		});
	}

	@Override
	public Resource getType() {
		return StandardBTVocabulary.SEQUENCE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(26);
		sb.append("Sequence (");
		sb.append(url);
		sb.append(" { ");
		for (Task<AgentTaskInformation> task : children) {
			sb.append(task.toString());
			sb.append(", ");
		}
		sb.append(" })");
		return sb.toString();
	}

	@Override
	// From gdx.ai Sequence
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		++currentChildIndex;
		if (currentChildIndex < children.size) {
			run(); // Run next child
		} else {
			resetSkipedChilds(currentChildIndex);
			success(); // All children processed, return success status
		}
	}

	@Override
	// From gdx.ai Sequence
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		resetSkipedChilds(++currentChildIndex);
		fail(); // Return failure status when a child says it failed
	}

	private void resetSkipedChilds(final int childIndex) {
		for (int i = childIndex; i < children.size; i++) {
			getChild(i).resetTask();
		}
	}
}
