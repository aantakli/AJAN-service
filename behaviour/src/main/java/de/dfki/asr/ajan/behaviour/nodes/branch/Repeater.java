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

import com.badlogic.gdx.ai.btree.LoopDecorator;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.IntegerDistribution;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.SelectEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Direction;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.LoggerFactory;

@RDFBean("bt:Repeater")
public class Repeater extends LoopDecorator<AgentTaskInformation> implements TreeNode {
	@Getter @Setter
	@RDFSubject
	private String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Repeater.class);
	private Resource instance;
	@TaskAttribute public IntegerDistribution times;
	@TaskAttribute public IntegerDistribution evalTimes;
	private int count;
	private int evalCount;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:setTimes")
	@Getter @Setter
	private IntValue intValue;

	@RDF("bt:hasChild")
	public Task<AgentTaskInformation> getChild() {
		return child;
	}

	public Repeater() {
		// empty constructor needed
	}

	// From gdx.ai Sequence
	public Repeater(final Task<AgentTaskInformation> child) {
		this(ConstantIntegerDistribution.NEGATIVE_ONE, child);
	}

	// From gdx.ai Sequence
	public Repeater(final IntegerDistribution times, final Task<AgentTaskInformation> child) {
		super(child);
		this.times = times;
	}

	public void setChild(final Task<AgentTaskInformation> newChild) {
		child = newChild;
	}

	@Override
	public void start() {
		try {
			times = updateTimes();
			count = times.nextInt();
		} catch (SelectEvaluationException ex) {
			Logger.getLogger(Repeater.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// From gdx.ai Sequence
	@Override
	public boolean condition () {
		return loop && count != 0;
	}

	// From gdx.ai Sequence
	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		if (count > 0) {
			count--;
		}
		if (count == 0) {
			super.childSuccess(runningTask);
			loop = false;
		} else {
			loop = true;
		}
	}

	// From gdx.ai Sequence
	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		childSuccess(runningTask);
	}

	// From gdx.ai Sequence
	@Override
	protected Task<AgentTaskInformation> copyTo (final Task<AgentTaskInformation> task) {
		Repeater repeat = (Repeater)task;
		repeat.times = times; // no need to clone since it is immutable
		return super.copyTo(task);
	}

	// From gdx.ai Sequence
	@Override
	@SuppressWarnings("PMD.NullAssignment")
	public void resetTask() {
		count = 0;
		times = null;
		super.resetTask();
	}

	private IntegerDistribution updateTimes() throws SelectEvaluationException {
		BigInteger number = intValue.getIntValue(this.getObject());
		return new ConstantIntegerDistribution(number.intValue());
	}

	private IntegerDistribution updateTimes(final Repository repo) throws SelectEvaluationException {
		BigInteger number = intValue.getIntValue(repo);
		return new ConstantIntegerDistribution(number.intValue());
	}

	@Override
	public Resource getType() {
		return BTVocabulary.REPEATER;
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
		Direction direction = result.getDirection();
		try {
			if (count == 0) {
				evalTimes = updateTimes(result.getRepo().initialize());
				evalCount = evalTimes.nextInt();
			} else {
				evalCount = count;
			}
			if (direction.equals(Direction.Up)) {
				evalCount--;
			}
			evaluateChild(result);
		} catch (SelectEvaluationException ex) {
			LOG.error("Problems with defined query", ex);
			result.setChildResult(Result.FAIL);
		}
		if (direction.equals(Direction.Up)) {
			((TreeNode)control).evaluate(result.setDirection(Direction.Up));
		}
	}

	protected void evaluateChild(final EvaluationResult result) {
		while (evalCount > 0) {
			((TreeNode)child).evaluate(result.setDirection(Direction.Down));
			evalCount--;
		}
		result.setChildResult(Result.SUCCESS);
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		BTUtil.setDecoratorNodeModel(model, root, mode, this);
		return model;
	}

	@Override
	public String toString() {
		return "Repeater (" + url + " {" + child.toString() + "})";
	}
}
