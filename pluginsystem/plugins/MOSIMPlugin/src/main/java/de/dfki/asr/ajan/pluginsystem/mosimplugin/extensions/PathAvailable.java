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
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MInterval3;
import de.mosim.mmi.constraints.MPathConstraint;
import de.mosim.mmi.math.MVector;
import de.mosim.mmi.math.MVector3;
import de.mosim.mmi.scene.MSceneObject;
import de.mosim.mmi.services.MPathPlanningService;
import de.mosim.mmi.services.MSceneAccess;
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
@RDFBean("bt-mosim:PathAvailable")
public class PathAvailable extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;
	
	@RDF("bt-mosim:avatar")
	@Getter @Setter
	private BehaviorConstructQuery avatar;

	@RDF("bt-mosim:walkTarget")
	@Getter @Setter
	private BehaviorConstructQuery target;

	@RDF("bt-mosim:sceneObjects")
	@Getter @Setter
	private BehaviorSelectQuery objects;

	@RDF("bt-mosim:host")
	@Getter @Setter
	private BehaviorSelectQuery service;

	private String host;
	private int port;

	@RDF("bt-mosim:scene")
	@Getter @Setter
	private BehaviorSelectQuery scene;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	protected static final Logger LOG = LoggerFactory.getLogger(PathAvailable.class);
	
	private class Input {
		public Resource iri;
		public MVector vector;
	}

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetAvatar");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(service,this.getObject());
			if(!hostMap.isEmpty()) {
				Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				if(checkPath()) {
					String report = toString() + " SUCCEEDED";
					LOG.info(report);
					return new LeafStatus(Status.SUCCEEDED, report);
				} else {
					String report = toString() + " FAILED";
					LOG.info(report);
					return new LeafStatus(Status.FAILED, report);
				}
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

	private boolean checkPath() {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MPathPlanningService.Client client = new MPathPlanningService.Client(protocol);
				Input targetInp = getInput(target);
				MPathConstraint path = client.ComputePath(getInput(avatar).vector, targetInp.vector, getMSceneObjects(), getProperties());
				Model inputModel = getInputModel(targetInp.iri, path);
				MOSIMUtil.writeInput(inputModel, repository.toString(), this.getObject());
				return !path.getPolygonPoints().isEmpty();
			}
		} catch (TException | URISyntaxException ex) {
			LOG.error("Could not check path", ex);
			return false;
		}
	}

	private Input getInput(final BehaviorConstructQuery query) throws URISyntaxException {
		Input input = new Input();
		MVector vector = new MVector();
		List<Double> values = new ArrayList();
		Repository repo = BTUtil.getInitializedRepository(this.getObject(), query.getOriginBase());
		Model result = query.getResult(repo);
		input.iri = MOSIMUtil.getResource(result, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.M_WALKPOINT);
		values.add(0, new Double(MOSIMUtil.getObject(result, null, MOSIMVocabulary.HAS_POS_X)));
		values.add(1, new Double(MOSIMUtil.getObject(result, null, MOSIMVocabulary.HAS_POS_Y)));
		values.add(2, new Double(MOSIMUtil.getObject(result, null, MOSIMVocabulary.HAS_POS_Z)));
		vector.setValues(values);
		input.vector = vector;
		return input;
	}

	private List<MSceneObject> getMSceneObjects() throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(this.getObject(), objects.getOriginBase());
		List<BindingSet> result = objects.getResult(repo);
		List<MSceneObject> sObjects = new ArrayList();
		Iterator<BindingSet> itr = result.iterator();
		while (itr.hasNext()) {
			BindingSet binding = itr.next();
			sObjects.add(getMSceneObject(binding.getValue("id").stringValue()));
		}
		return sObjects;
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

	private Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("mode", "2D");
		properties.put("time", "1");
		properties.put("radius", "0.3");
		properties.put("height", "0.5");
		return properties;
	}
	
	private Model getInputModel(final Resource subject, final MPathConstraint path) {
		Model model = new LinkedHashModel();
		double length = 999;
		for (int i = 1; i < path.getPolygonPointsSize(); i++) {
			MInterval3 first = path.getPolygonPoints().get(i-1).TranslationConstraint.Limits;
			MInterval3 next = path.getPolygonPoints().get(i).TranslationConstraint.Limits;
			length += Math.sqrt(Math.pow(first.X.Max - next.X.Max, 2) + Math.pow(first.Z.Max - next.Z.Max, 2));
		}
		model.add(subject, MOSIMVocabulary.HAS_PATH_LENGTH, vf.createLiteral(length));
		return model;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "PathAvailable (" + getLabel() + ")";
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
