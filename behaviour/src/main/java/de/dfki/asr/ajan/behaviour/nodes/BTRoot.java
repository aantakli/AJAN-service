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

import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.Listener;
import de.dfki.asr.ajan.behaviour.events.Producer;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.Debug;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RDFBean("bt:Root")
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.EmptyCatchBlock", "PMD.SystemPrintln"})
public class BTRoot extends BehaviorTree<AgentTaskInformation> implements TreeNode, Listener {

	private boolean block;

	@Getter @Setter
	@RDFSubject
	protected String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	private Producer goalProducer;

	@Override
	public Resource getInstance(final Resource btInstance) {
		return instance;
	}

	public Resource getInstance() {
		return getInstance(instance);
	}

	public void setInstance(final Resource btInstance) {
		instance = btInstance;
	}

	@Override
	public Resource getDefinition(final Resource btDefinition) {
		return btDefinition;
	}

	public Resource getDefinition() {
		return getDefinition(vf.createIRI(url));
	}

	@Override
	public Resource getType() {
		return BTVocabulary.BEHAVIOR_TREE;
	}

	@RDF("bt:hasChild")
	public Task<AgentTaskInformation> getChild() {
		return getChild(0);
	}

	public void setChild(final Task<AgentTaskInformation> task) {
		this.addChild(task);
	}

	@Override
	public void run () {
		if (!block) {
			block = true;
			long before = System.currentTimeMillis();
			this.step();
			if (this.getObject() != null) {
				cleareEKB();
				Debug debug = this.getObject().getDebug();
				long time = System.currentTimeMillis() - before;
				NodeStatus leafStatus = new NodeStatus(null, this.getObject().getLogger(), this.getClass(), "BTRoot(" + label + "), time = " + time + "ms, FINISHED");
				String report = BTUtil.createReport(getUrl(), this, leafStatus, debug, new LinkedHashModel());
				BTUtil.sendReport(this.getObject(),report);
			}
			try {
				this.getObject().getLogger().info(this.getClass(), "Runtime (" + Long.toString(System.currentTimeMillis() - before) + "ms)");
			} catch (Exception ex) { }
			block = false;
			if (goalProducer != null) {
				goalProducer.reportGoalStatus(status);
			}
		}
	}

	private void cleareEKB() {
		if (status != Status.RUNNING && this.getObject().isClearEKB()) {
			Repository repo = this.getObject().getExecutionBeliefs().initialize();
			try (RepositoryConnection conn = repo.getConnection()) {
				conn.clear();
				conn.clearNamespaces();
				conn.close();
			}
		}
	}

	@Override
	public void simulate(final SimulationResult result) {
		Direction direction = result.getDirection();
		if (direction == Direction.Down) {
			((TreeNode)getChild(0)).simulate(result.setDirection(Direction.Down));
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
			"BTRoot" + " { " + getChild().toString() + " }";
	}

	public Model getModel(final Model model, final Resource btInstance,  final ModelMode mode) {
		instance = btInstance;
		return getModel(model, this, mode);
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final ModelMode mode) {
		TreeNode child = (TreeNode)getChild();
		Resource definition = getDefinition();
		if (instance == null) {
			instance = BTUtil.getInstanceResource(getUrl(), root.getInstance());
		}
		model.add(instance, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, getType());
		model.add(instance, RDFS.LABEL, vf.createLiteral(label));
		model.add(instance, RDFS.ISDEFINEDBY, definition);
		model.add(instance, BTVocabulary.HAS_STATE, vf.createLiteral(getStatus().toString()));
		model.add(instance, BTVocabulary.HAS_CHILD, child.getInstance(instance));
		child.getModel(model, this, mode);
		return model;
	}

	@Override
	public void setEventInformation(final Object info) {
		this.getObject().setEventInformation(info);
		this.run();
	}

	@Override
	public void setEventInformation(final String id, final Object info) {
		this.getObject().getActionInformation().put(id, info);
		this.run();
	}

	@Override
	@SuppressWarnings("PMD.ConfusingTernary")
	public void setEventInformation(final Producer producer, final Object info) {
		if (goalProducer == null) {
			goalProducer = producer;
		} else if (!producer.equals(goalProducer)) {
			if (!goalProducer.goalIsRunning()) {
				goalProducer = producer;
			} else {
				producer.reportGoalStatus(Status.FAILED);
				return;
			}
		}
		this.setEventInformation(info);
	}
}

