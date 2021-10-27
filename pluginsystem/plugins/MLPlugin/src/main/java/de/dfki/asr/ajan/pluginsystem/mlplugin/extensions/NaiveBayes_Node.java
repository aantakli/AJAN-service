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

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.test.WeatherNominal;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.TrainingTable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
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
public class NaiveBayes_Node extends AbstractTDBLeafTask implements NodeExtension {
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

	protected static final Logger LOG = LoggerFactory.getLogger(NaiveBayes_Node.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#NaiveBayes");
	}

	/*
	
	@relation weather.symbolic

	@attribute outlook {sunny, overcast, rainy}
	@attribute temperature {hot, mild, cool}
	@attribute humidity {high, normal}
	@attribute windy {TRUE, FALSE}
	@attribute play {yes, no}

	@data
	sunny,hot,high,FALSE,no
	sunny,hot,high,TRUE,no
	overcast,hot,high,FALSE,yes
	rainy,mild,high,FALSE,yes
	rainy,cool,normal,FALSE,yes
	rainy,cool,normal,TRUE,no
	overcast,cool,normal,TRUE,yes
	sunny,mild,high,FALSE,no
	sunny,cool,normal,FALSE,yes
	rainy,mild,normal,FALSE,yes
	sunny,mild,normal,TRUE,yes
	overcast,mild,high,TRUE,yes
	overcast,hot,normal,FALSE,yes
	rainy,mild,high,TRUE,no
	
	*/
	
	@Override
	public LeafStatus executeLeaf() {
		try {
			TrainingTable table = getTrainingTable();		
			int p = table.getData()[0].length;
			int k = MathEx.max(table.getObjectives()) + 1;

			ClassificationMetrics metrics = LOOCV.classification(table.getData(), table.getObjectives(), (x, y) -> {
				return getModel(k, p, x, y);
			});

			LOG.info("Metrics: " + metrics);
			
			NaiveBayes model = getModel(k, p, table.getData(), table.getObjectives());
			
			double[] shhf = {0,0,0,0};
			double[] omht = {1,1,0,0};

			double[] test = {2,1,0,1};

			LOG.info("sunny,hot,high,FALSE -> no: " + model.predict(shhf));
			LOG.info("overcast,mild,high,TRUE -> yes: " + model.predict(omht));

			LOG.info("rainy,mild,high,FALSE -> ?: " + model.predict(omht));

			String report = toString() + " SUCCEEDED";
			return new LeafStatus(Status.SUCCEEDED, report);
		} catch (Exception ex) {
			LOG.info(toString() + " FAILED due to problem evaluation error", ex);
			String report = toString() + " FAILED";
			return new LeafStatus(Status.FAILED, report);
		}
	}

	private TrainingTable getTrainingTable() throws QueryEvaluationException, URISyntaxException, MLMappingException {
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

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}
}
