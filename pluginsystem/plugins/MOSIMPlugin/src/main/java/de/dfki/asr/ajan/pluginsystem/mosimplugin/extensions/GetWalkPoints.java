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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.math.MTransform;
import de.mosim.mmi.scene.MSceneObject;
import de.mosim.mmi.services.MSceneAccess;
import de.mosim.mmi.services.MWalkPoint;
import de.mosim.mmi.services.MWalkPointEstimationService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:GetWalkPoints")
public class GetWalkPoints extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:host")
	@Getter @Setter
	private BehaviorSelectQuery query;

	private String host;
	private int port;

	@RDF("bt-mosim:scene")
	@Getter @Setter
	private BehaviorSelectQuery scene;

	@RDF("bt-mosim:sceneObjects")
	@Getter @Setter
	private BehaviorSelectQuery objects;

	@RDF("bt-mosim:walkTarget")
	@Getter @Setter
	private BehaviorSelectQuery target;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;
	
	private MSceneObject targetObj;

	protected static final Logger LOG = LoggerFactory.getLogger(GetWalkPoints.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetWalkPoints");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query, this.getObject());
			if(!hostMap.isEmpty()) {
				Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				targetObj = getWalkTarget();
				List<MWalkPoint> walkPoints = getWalkPoints();
				LOG.info("Found Walkpoints: " + walkPoints.size());
				Model inputModel = getWalkPointsModel(walkPoints);
				MOSIMUtil.writeInput(inputModel, repository.toString(), this.getObject());
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED";
			LOG.info(report);
			return new LeafStatus(Status.FAILED, report);
		}
		String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
	}

	private List<MWalkPoint> getWalkPoints() throws URISyntaxException {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MWalkPointEstimationService.Client client = new MWalkPointEstimationService.Client(protocol);
				return client.EstimateWalkPoints(getMSceneObjects(targetObj), targetObj, 10, new HashMap<String,String>());
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MWalkPoint>", ex);
			return null;
		}
	}

	private List<MSceneObject> getMSceneObjects(final MSceneObject targetObj) throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(this.getObject(), objects.getOriginBase());
		List<BindingSet> result = objects.getResult(repo);
		List<MSceneObject> sObjects = new ArrayList();
		sObjects.add(targetObj);
		Iterator<BindingSet> itr = result.iterator();
		while (itr.hasNext()) {
			BindingSet binding = itr.next();
			sObjects.add(getMSceneObject(binding.getValue("id").stringValue()));
		}
		return sObjects;
	}

	private MSceneObject getWalkTarget() throws URISyntaxException {
		if (query != null) {
			Repository repo = BTUtil.getInitializedRepository(this.getObject(), target.getOriginBase());
			List<BindingSet> result = target.getResult(repo);
			if (!result.isEmpty()) {
				BindingSet bindings = result.get(0);
				String id = bindings.getValue("targetID").stringValue();
				MSceneObject obj = getMSceneObject(id);
				return obj;
			}
		}
		return null;
	}

	private MSceneObject getMSceneObject(final String id) throws URISyntaxException {
		Map<String,String> hostMap = MOSIMUtil.getHostInfos(scene, this.getObject());
		Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
		try {
			try (TTransport transport = new TSocket(entry.getKey(), Integer.parseInt(entry.getValue()))) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MSceneAccess.Client client = new MSceneAccess.Client(protocol);
				return client.GetSceneObjectByID(id);
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MSceneObject>", ex);
			return null;
		}
	}
	
	private Model getWalkPointsModel(final List<MWalkPoint> walkpoints) {
		Model model = new LinkedHashModel();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		for (MWalkPoint point: walkpoints) {
			if (point.PositionConstraint != null && point.PositionConstraint.ParentToConstraint != null) {
				Resource walkpoint = vf.createBNode();
				model.add(walkpoint, rdfType, MOSIMVocabulary.M_WALKPOINT);
				MTransform pointT = MOSIMUtil.getLookAtTransform(point.PositionConstraint.ParentToConstraint, targetObj.Transform);
				MOSIMUtil.setTransform(model, walkpoint, pointT);
			}
		}
		return model;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GetWalkPoints (" + getLabel() + ")";
	}
	
	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
