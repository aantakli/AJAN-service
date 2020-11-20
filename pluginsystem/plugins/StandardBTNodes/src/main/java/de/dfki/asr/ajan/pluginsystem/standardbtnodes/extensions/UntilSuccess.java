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
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:UntilSuccess")
public class UntilSuccess extends com.badlogic.gdx.ai.btree.decorator.UntilSuccess<AgentTaskInformation> implements NodeExtension, TreeNode {
	@Getter @Setter
	@RDFSubject
	private String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:hasChild")
	public Task<AgentTaskInformation> getChild() {
		return child;
	}

	public void setChild(final Task<AgentTaskInformation> newChild) {
		child = newChild;
	}

	@Override
	public String toString() {
		return "UntilSuccess (" + url + " {" + child.toString() + "})";
	}

	@Override
	public Resource getType() {
	    return StandardBTVocabulary.UNTIL_SUCCESS;
	}

	@Override
	public Resource getInstance(final Resource btInstance) {
		if (instance == null) {
			instance = BTUtil.getInstanceResource(url, btInstance);
		}
		return instance;
	}

	@Override
	public Resource getDefinition(final Resource btDefinition) {
		if (url == null) {
			return btDefinition;
		}
		return vf.createIRI(url);
	}

	@Override
	public void evaluate(final EvaluationResult result) {
		// no body needed here
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		BTUtil.setDecoratorNodeModel(model, root, mode, this);
		return model;
	}
}
