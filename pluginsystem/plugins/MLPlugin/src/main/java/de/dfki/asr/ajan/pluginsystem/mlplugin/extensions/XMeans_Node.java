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

import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import smile.clustering.XMeans;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:XMeans")
public class XMeans_Node extends KMeans_Node {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ml:maxClusters")
	@Setter @Getter
	private int maxClusters;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("ml:inputTbl")
	@Setter @Getter
	private BehaviorSelectQuery inputTbl;

	protected static final Logger LOG = LoggerFactory.getLogger(XMeans_Node.class);
	
	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#XMeans");
	}

	@Override
	protected void getClusters() throws IllegalArgumentException, URISyntaxException, MLMappingException, ConditionEvaluationException {
		double[][] x = getInputTable(this.getInputTbl());
		XMeans result = XMeans.fit(x, maxClusters);
		System.out.println("XMeans: \r");
        System.out.println(result);
		saveClusters(result.y);
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
