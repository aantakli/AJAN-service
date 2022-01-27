/*
 * Copyright (C) 2021 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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
package de.dfki.asr.ajan.pluginsystem.mlplugin.extensions;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.NaiveBayesException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.TrainingTable;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import smile.classification.NaiveBayes;
import smile.math.MathEx;
import smile.stat.distribution.Distribution;
import smile.stat.distribution.EmpiricalDistribution;
import smile.util.IntSet;
import smile.validation.ClassificationMetrics;
import smile.validation.LOOCV;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:NaiveBayes")
public class NaiveBayes_Node extends AbstractTDBBranchTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ml:objective")
	@Setter @Getter
	private String objective;

	@RDF("ml:trainingTbl")
	@Setter @Getter
	private BehaviorSelectQuery trainingTbl;

	@RDF("ml:stateTbl")
	@Setter @Getter
	private BehaviorSelectQuery stateTbl;

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

	protected static final Logger LOG = LoggerFactory.getLogger(NaiveBayes_Node.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#NaiveBayes");
	}

	@Override
	public void run () {
		try {
			if (runningChild == null) {
				runChildByIndex();
			} else {
				runningChild.run();
			}
		} catch (NaiveBayesException | MLMappingException| URISyntaxException | QueryEvaluationException ex) {
			LOG.info(ex.toString());
			fail();
		}
	}

	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		resetSkipedChilds();
		success();
	}

	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		resetSkipedChilds();
		fail();
	}

	private void resetSkipedChilds() {
		for (int i = 0; i < children.size; i++) {
			getChild(i).resetTask();
		}
	}

	private void runChildByIndex() throws NaiveBayesException, URISyntaxException, QueryEvaluationException, MLMappingException {
		int child = getChildId();
		if (child < 0 || child > children.size) {
			LOG.info(toString() + "FAILED");
			LOG.info("No matching child found!");
			fail();
		} else {
			runChild(child);
		}
	}

	public int getChildId() throws NaiveBayesException, URISyntaxException, QueryEvaluationException, MLMappingException {
		TrainingTable table = getTrainingTable();		
		int p = table.getData()[0].length;
		int k = MathEx.max(table.getObjectives()) + 1;

		ClassificationMetrics metrics = LOOCV.classification(table.getData(), table.getObjectives(), (x, y) -> {
			return getModel(k, p, x, y);
		});
		LOG.info("Metrics: " + metrics);

		NaiveBayes model = getModel(k, p, table.getData(), table.getObjectives());
		return getState(table, model);
	}

	private TrainingTable getTrainingTable() throws QueryEvaluationException, URISyntaxException, MLMappingException, NaiveBayesException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), trainingTbl.getOriginBase());
		List<BindingSet> result = trainingTbl.getResult(repo);
		if (result.isEmpty()) {
			throw new MLMappingException("No ?trainingTbl is defined!");
		}

		return new TrainingTable(result, objective);
	}

	private NaiveBayes getModel(int k, int p, double[][] x, int[] y) {
		int n = x.length;
		double[] priori = new double[k];
		Distribution[][] condprob = new Distribution[k][p];
		for (int i = 0; i < k; i++) {
			priori[i] = 1.0 / k;
			final int c = i;
			for (int j = 0; j < p; j++) {
				final int f = j;
				int[] xij = IntStream.range(0, n).filter(l -> y[l] == c).map(l -> (int) x[l][f]).toArray();
				int[] xj = IntStream.range(0, n).map(l -> (int) x[l][f]).toArray();
				condprob[i][j] = EmpiricalDistribution.fit(xij, IntSet.of(xj));
			}
		}
		return new NaiveBayes(priori, condprob);
	}

	private int getState(final TrainingTable table, final NaiveBayes model) throws URISyntaxException {
		Map<String,Set<String>> options = table.getOptions();
		Set<String> map = options.keySet();
		double[] input = new double[map.size() - 1];
		
		Repository repo = BTUtil.getInitializedRepository(getObject(), stateTbl.getOriginBase());
		List<BindingSet> result = stateTbl.getResult(repo);
		BindingSet bindings =  result.get(0);
		
		Iterator<String> iter = map.iterator();
		int i = 0;
		while(iter.hasNext()) {
			String attr = iter.next();
			if (!attr.equals(objective)) {
				Binding value = bindings.getBinding(attr);
				input[i] = getValue(value, options.get(attr));
				i++;
			}
		}		
		int prediction = model.predict(input);
		printResult(table, bindings, input, prediction);
		
		return prediction;
	}

	private double getValue(final Binding binding, final Set<String> options) {
		Iterator<String> iter = options.iterator();
		double i = 0;
		while(iter.hasNext()) {
			String value = iter.next();
			if (value.equals(binding.getValue().stringValue()))
				return i;
			else i++;
		}
		return i;
	}

	private void printResult(final TrainingTable table, final BindingSet bindings, final double[] input, final int prediction) {
		Map<String,Set<String>> options = table.getOptions();
		StringBuilder builder = new StringBuilder();
		StringBuilder inputStr = new StringBuilder();

		setAttributesLog(builder, inputStr, bindings, options);
		setInputLog(builder, inputStr);
		setSateLog(builder, input);
		setObecjtivesLog(builder, options.get(objective));
		setPredictionLog(builder, prediction);
		LOG.info(builder.toString());
	}

	private void setAttributesLog(final StringBuilder builder, final StringBuilder inputStr, final BindingSet bindings, final Map<String,Set<String>> options) {
		builder.append("\r Attributes: ");
		Set<String> attrs = options.keySet();
		Iterator<String> atIter = attrs.iterator();
		while(atIter.hasNext()) {
			String attr = atIter.next();
			if(!attr.equals(objective)) {
				builder.append(attr + " ");
				inputStr.append(bindings.getBinding(attr).getValue().stringValue() + " ");
			}
		}
	}
	
	private void setInputLog(final StringBuilder builder, final StringBuilder inputStr) {
		builder.append("\r Input: ");
		builder.append(inputStr.toString());
	}

	private void setSateLog(final StringBuilder builder, final double[] input) {
		builder.append("\r State: ");
		for(int i=0; i < input.length; i++) {
			builder.append(input[i]);
			builder.append(" ");
		}
	}

	private void setObecjtivesLog(final StringBuilder builder, final Set<String> objectives) {
		Iterator<String> obIter = objectives.iterator();
		int i = 0;
		builder.append("\r Objectives: ");
		while(obIter.hasNext()) {
			builder.append(obIter.next() + " -> " + i + " ");
			i++;
		}
	}
	
	private void setPredictionLog(final StringBuilder builder, final int prediction) {
		builder.append("\r Predicition: " + prediction);
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}
}
