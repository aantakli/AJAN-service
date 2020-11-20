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
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.utils.Array;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
@RDFBean("bt:Parallel")
public class Parallel extends com.badlogic.gdx.ai.btree.BranchTask<AgentTaskInformation> implements NodeExtension, TreeNode {
	@Getter @Setter
	@RDFSubject
	private String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private Resource instance;
	protected int currentEvaluatingChild;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:orchestration")
	public void setOrchestrator(final URI orchestration) {
		if (orchestration.getFragment().equals("Join")) {
			this.orchestrator = Orchestrator.Join;
		}
	}

	@RDF("bt:policy")
	public void setPolicy(final URI policy) {
		if (policy.getFragment().equals("Priority")) {
			this.policy = Policy.Selector;
		}
	}

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

	// ---------------------
	// GDX-AI implementation
	// ---------------------

	/** Optional task attribute specifying the parallel policy (defaults to {@link Policy#Sequence}) */
	@TaskAttribute public Policy policy;
	/** Optional task attribute specifying the execution policy (defaults to {@link Orchestrator#Resume}) */
	@TaskAttribute public Orchestrator orchestrator;

	private boolean noRunningTasks;
	private Boolean lastResult;
	private int currentChildIndex;

	/** Creates a parallel task with sequence policy, resume orchestrator and no children */
	public Parallel () {
		this(new Array<Task<AgentTaskInformation>>());
	}

	/** Creates a parallel task with sequence policy, resume orchestrator and the given children
	 * @param tasks the children */
	public Parallel (Task<AgentTaskInformation>... tasks) {
		this(new Array<Task<AgentTaskInformation>>(tasks));
	}

	/** Creates a parallel task with sequence policy, resume orchestrator and the given children
	 * @param tasks the children */
	public Parallel (Array<Task<AgentTaskInformation>> tasks) {
		this(Policy.Sequence, tasks);
	}

	/** Creates a parallel task with the given policy, resume orchestrator and no children
	 * @param policy the policy */
	public Parallel (Policy policy) {
		this(policy, new Array<Task<AgentTaskInformation>>());
	}

	/** Creates a parallel task with the given policy, resume orchestrator and the given children
	 * @param policy the policy
	 * @param tasks the children */
	public Parallel (Policy policy, Task<AgentTaskInformation>... tasks) {
		this(policy, new Array<Task<AgentTaskInformation>>(tasks));
	}

	/** Creates a parallel task with the given policy, resume orchestrator and the given children
	 * @param policy the policy
	 * @param tasks the children */
	public Parallel (Policy policy, Array<Task<AgentTaskInformation>> tasks) {
		this(policy, Orchestrator.Resume, tasks);
	}

	/** Creates a parallel task with the given orchestrator, sequence policy and the given children
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Orchestrator orchestrator, Array<Task<AgentTaskInformation>> tasks) {
		this(Policy.Sequence, orchestrator, tasks);
	}
	
	/** Creates a parallel task with the given orchestrator, sequence policy and the given children
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Orchestrator orchestrator, Task<AgentTaskInformation>... tasks) {
		this(Policy.Sequence, orchestrator, new Array<Task<AgentTaskInformation>>(tasks));
	}
	
	/** Creates a parallel task with the given orchestrator, policy and children
	 * @param policy the policy
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Policy policy, Orchestrator orchestrator, Array<Task<AgentTaskInformation>> tasks) {
		super(tasks);
		this.policy = policy;
		this.orchestrator = orchestrator;
		noRunningTasks = true;
	}

	@Override
	public void run () {
		orchestrator.execute(this);
	}

	@Override
	public void childRunning (Task<AgentTaskInformation> task, Task<AgentTaskInformation> reporter) {
		noRunningTasks = false;
	}

	@Override
	public void childSuccess (Task<AgentTaskInformation> runningTask) {
		lastResult = policy.onChildSuccess(this);
	}

	@Override
	public void childFail (Task<AgentTaskInformation> runningTask) {
		lastResult = policy.onChildFail(this);
	}

	@Override
	public void resetTask () {
		super.resetTask();
		noRunningTasks = true;
	}

	@Override
	protected Task<AgentTaskInformation> copyTo (Task<AgentTaskInformation> task) {
		Parallel parallel = (Parallel)task;
		parallel.policy = policy; // no need to clone since it is immutable
		parallel.orchestrator = orchestrator; // no need to clone since it is immutable
		return super.copyTo(task);
	}
	
	public void resetAllChildren() {
		for (int i = 0, n = getChildCount(); i < n; i++) {
			Task<AgentTaskInformation> child = getChild(i);
			child.reset();
		}
	}

	@Override
	public void evaluate(final EvaluationResult result) {
		EvaluationResult.Direction direction = result.getDirection();
		if (direction.equals(EvaluationResult.Direction.Up)) {
			currentEvaluatingChild = currentChildIndex;
			currentEvaluatingChild++;
		} else if (currentChildIndex < children.size) {
			currentEvaluatingChild = currentChildIndex;
		} else {
			currentEvaluatingChild = 0;
		}
		evaluateChilds(result);
		if (direction.equals(EvaluationResult.Direction.Up)) {
			((TreeNode)control).evaluate(result.setDirection(EvaluationResult.Direction.Up));
		}
	}

	protected void evaluateChilds(final EvaluationResult result) {
		while (currentEvaluatingChild < children.size) {
			((TreeNode)children.get(currentEvaluatingChild)).evaluate(result.setDirection(EvaluationResult.Direction.Down));
			if (nodeLogic(result)) {
				break;
			}
			currentEvaluatingChild++;
		}
	}

	protected boolean nodeLogic(final EvaluationResult result) {
		if (policy == Policy.Sequence) {
			return !result.getChildResult().equals(EvaluationResult.Result.SUCCESS);
		} else {
			return result.getChildResult().equals(EvaluationResult.Result.SUCCESS);
		}
	}
	
	/** The enumeration of the child orchestrators supported by the {@link Parallel} task */
	public enum Orchestrator {
		/** The default orchestrator - starts or resumes all children every single step */
		Resume() {
			@Override
			public void execute(Parallel parallel) {
				parallel.noRunningTasks = true;
				parallel.lastResult = null;
				for (parallel.currentChildIndex = 0; parallel.currentChildIndex < parallel.children.size; parallel.currentChildIndex++) {
					Task child = parallel.children.get(parallel.currentChildIndex);
					if (child.getStatus() == Status.RUNNING) {
						child.run();
					} else {
						child.setControl(parallel);
						child.start();
						if (child.checkGuard(parallel))
							child.run();
						else
							child.fail();
					}

					if (parallel.lastResult != null) { // Current child has finished either with success or fail
						parallel.cancelRunningChildren(parallel.noRunningTasks ? parallel.currentChildIndex + 1 : 0);
						if (parallel.lastResult)
							parallel.success();
						else
							parallel.fail();
						return;
					}
				}
				parallel.running();
			}
		},
		/** Children execute until they succeed or fail but will not re-run until the parallel task has succeeded or failed */
		Join() {
			@Override
			public void execute(Parallel parallel) {
				parallel.noRunningTasks = true;
				parallel.lastResult = null;
				for (parallel.currentChildIndex = 0; parallel.currentChildIndex < parallel.children.size; parallel.currentChildIndex++) {
					Task child = parallel.children.get(parallel.currentChildIndex);
					
					switch(child.getStatus()) {
					case RUNNING:
						child.run();
						break;
					case SUCCEEDED:
					case FAILED:
						break;
					default:
						child.setControl(parallel);
						child.start();
						if (child.checkGuard(parallel))
							child.run();
						else
							child.fail();
						break;
					}
					
					if (parallel.lastResult != null) { // Current child has finished either with success or fail
						parallel.cancelRunningChildren(parallel.noRunningTasks ? parallel.currentChildIndex + 1 : 0);
						parallel.resetAllChildren();
						if (parallel.lastResult)
							parallel.success();
						else
							parallel.fail();
						return;
					}
				}
				parallel.running();
			}
		};
		
		/**
		 * Called by parallel task each run
		 * @param parallel The {@link Parallel} task
		 */
		public abstract void execute(Parallel parallel);
	}
	
	@Override
	public void reset() {
		policy = Policy.Sequence;
		orchestrator = Orchestrator.Resume;
		noRunningTasks = true;
		lastResult = null;
		currentChildIndex = 0;
		super.reset();
	}

	/** The enumeration of the policies supported by the {@link Parallel} task. */
	public enum Policy {
		/** The sequence policy makes the {@link Parallel} task fail as soon as one child fails; if all children succeed, then the
		 * parallel task succeeds. This is the default policy. */
		Sequence() {
			@Override
			public Boolean onChildSuccess (Parallel parallel) {
				switch(parallel.orchestrator) {
				case Join:
					return parallel.noRunningTasks && parallel.children.get(parallel.children.size - 1).getStatus() == Status.SUCCEEDED ? Boolean.TRUE : null;
				case Resume:
				default:
					return parallel.noRunningTasks && parallel.currentChildIndex == parallel.children.size - 1 ? Boolean.TRUE : null;
				}
			}

			@Override
			public Boolean onChildFail (Parallel parallel) {
				return Boolean.FALSE;
			}
		},
		/** The selector policy makes the {@link Parallel} task succeed as soon as one child succeeds; if all children fail, then the
		 * parallel task fails. */
		Selector() {
			@Override
			public Boolean onChildSuccess (Parallel parallel) {
				return Boolean.TRUE;
			}

			@Override
			public Boolean onChildFail (Parallel parallel) {
				return parallel.noRunningTasks && parallel.currentChildIndex == parallel.children.size - 1 ? Boolean.FALSE : null;
			}
		};

		/** Called by parallel task each time one of its children succeeds.
		 * @param parallel the parallel task
		 * @return {@code Boolean.TRUE} if parallel must succeed, {@code Boolean.FALSE} if parallel must fail and {@code null} if
		 *         parallel must keep on running. */
		public abstract Boolean onChildSuccess (Parallel parallel);

		/** Called by parallel task each time one of its children fails.
		 * @param parallel the parallel task
		 * @return {@code Boolean.TRUE} if parallel must succeed, {@code Boolean.FALSE} if parallel must fail and {@code null} if
		 *         parallel must keep on running. */
		public abstract Boolean onChildFail (Parallel parallel);

	}

	// -------------------------
	// GDX-AI Implemantation END
	// -------------------------

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Parallel (");
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
	public Resource getType() {
		return StandardBTVocabulary.PARALLEL;
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
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		BTUtil.setBranchNodeModel(model, root, mode, this);
		return model;
	}
}
