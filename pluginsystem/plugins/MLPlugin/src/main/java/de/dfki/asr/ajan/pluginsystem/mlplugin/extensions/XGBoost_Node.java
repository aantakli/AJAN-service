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
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:XGBoost")
public class XGBoost_Node extends AbstractTDBBranchTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

	@RDF("ml:trainingTbl")
	@Setter @Getter
	private BehaviorSelectQuery trainingTbl;

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

	protected static final Logger LOG = LoggerFactory.getLogger(XGBoost_Node.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#XGBoost");
	}

	@Override
	public void run () {
		try {
			if (runningChild == null) {
				testDT();
			} else {
				runningChild.run();
			}
		} catch (IOException | URISyntaxException | XGBoostError ex) {
			LOG.info(ex.toString());
			fail();
		}
	}

	private void testDT() throws IOException, URISyntaxException, XGBoostError {
		// load file from text file, also binary buffer generated by xgboost4j
		DMatrix trainMat = new DMatrix("D:\\Projects\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\agaricus.txt.train");
		DMatrix testMat = new DMatrix("D:\\Projects\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\agaricus.txt.test");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("eta", 1.0);
		params.put("max_depth", 2);
		params.put("silent", 1);
		params.put("objective", "binary:logistic");


		HashMap<String, DMatrix> watches = new HashMap<String, DMatrix>();
		watches.put("train", trainMat);
		watches.put("test", testMat);

		//set round
		int round = 2;

		//train a boost model
		Booster booster = XGBoost.train(trainMat, params, round, watches, null, null);

		//predict
		float[][] predicts = booster.predict(testMat);
	}

	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}


	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}
}
