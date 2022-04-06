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
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.MLUtil;
import de.dfki.asr.ajan.pluginsystem.mlplugin.vocabularies.MLVocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import smile.clustering.KMeans;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:KMeans")
public class KMeans_Node extends AbstractTDBLeafTask implements NodeExtension, TreeNode {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ml:clusters")
	@Setter
	private int clusters;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("ml:inputTbl")
	@Setter @Getter
	private BehaviorSelectQuery inputTbl;

	private final ValueFactory VF = SimpleValueFactory.getInstance();
	
	@Getter
	private final Map<Integer,String> resourceMap = new HashMap();
	protected static final Logger LOG = LoggerFactory.getLogger(KMeans_Node.class);
	
	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#KMeans");
	}
	
	@Override
	public NodeStatus executeLeaf() {
		try{ 
			getClusters();
			String report = toString() + " SUCCEEDED";
			return new NodeStatus(Status.SUCCEEDED, report);
		} catch (IllegalArgumentException | ConditionEvaluationException | URISyntaxException | MLMappingException | QueryEvaluationException ex) {
			LOG.info(ex.toString());
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, report);
		}
	}

	protected void getClusters() throws IllegalArgumentException, URISyntaxException, MLMappingException, ConditionEvaluationException, QueryEvaluationException {
		double[][] x = getInputTable(inputTbl);
		KMeans result = KMeans.fit(x, clusters);
		System.out.println("KMeans: \r");
        System.out.println(result);
		saveClusters(result.y);

		// Test Input
		/*double[][] x = USPS.x;
        KMeans model = KMeans.fit(x, 10, 100, 4);
        System.out.println(model);*/
	}

	protected double[][] getInputTable(final BehaviorSelectQuery query) throws URISyntaxException, MLMappingException, QueryEvaluationException {
		if (query == null || query.getSparql().isEmpty()) {
			throw new MLMappingException("Empty input table!");
		}
		
		Repository repo = BTUtil.getInitializedRepository(this.getObject(), query.getOriginBase());
		List<BindingSet> result = query.getResult(repo);
		
		if (result.isEmpty() || !result.get(0).hasBinding("resource")) {
			throw new MLMappingException("Empty input table!");
		}
		
		BindingSet firstSet = result.get(0);
		
		if (!firstSet.hasBinding("resource")) {
			throw new MLMappingException("No ?resource defined!");
		}

		double[][] table = new double[result.size()][firstSet.size() - 1];
		
		Iterator<BindingSet> iter = result.iterator();
		int i = 0;
		while(iter.hasNext()) {
			BindingSet set = iter.next();
			Iterator<Binding> bindItr = set.iterator();
			int j = 0;
			while(bindItr.hasNext()) {
				Binding binding = bindItr.next();
				if(binding.getName().equals("resource")) {
					this.getResourceMap().put(i, binding.getValue().stringValue());
				} else {
					table[i][j] = Double.parseDouble(binding.getValue().stringValue());
					j++;
				}
			}
			i++;
		}

		return table;
	}

	protected void saveClusters(final int[] y) throws ConditionEvaluationException {
		//result.y --> Cluster assignments
		Model model = new LinkedHashModel();
		for (int i = 0; i < y.length; i++) {
			IRI cluster = VF.createIRI("http://www.ajan.de/behavior/ml-ns#Cluster" + y[i]);
			model.add(cluster, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MLVocabulary.CLUSTER);
			model.add(cluster, MLVocabulary.HAS_MEMBER, VF.createIRI(resourceMap.get(i)));
		}
		MLUtil.performWrite(this.getObject(), this.getTargetBase(), model);
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
