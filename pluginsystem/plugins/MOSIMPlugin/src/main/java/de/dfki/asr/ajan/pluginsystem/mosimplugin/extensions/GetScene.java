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
import de.mosim.mmi.scene.MCollider;
import de.mosim.mmi.services.MSceneAccess;
import de.mosim.mmi.scene.MSceneObject;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:GetScene")
public class GetScene extends AbstractTDBLeafTask implements NodeExtension {
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

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	protected static final Logger LOG = LoggerFactory.getLogger(GetScene.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetScene");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query,this.getObject());
			if(!hostMap.isEmpty()) {
				Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				List<MSceneObject> objects = getSceneObjects();
				Model inputModel = getInputModel(objects);
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

	private List<MSceneObject> getSceneObjects() {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MSceneAccess.Client client = new MSceneAccess.Client(protocol);
				return client.GetSceneObjects();
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MSceneObject>", ex);
			return null;
		}
	}

	private Model getInputModel(final List<MSceneObject> objects) {
		Model model = new LinkedHashModel();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		for (MSceneObject object: objects) {
			IRI subject = getMSceneObjectIRI(object.ID);
			model.add(subject, rdfType, MOSIMVocabulary.M_SCENE_OBJECT);
			model.add(subject, MOSIMVocabulary.HAS_ID, vf.createLiteral(object.ID));
			model.add(subject, RDFS.LABEL, vf.createLiteral(object.Name));
			if (object.isSetTransform() && object.Transform != null) {
				MOSIMUtil.setTransform(model, subject, object.Transform);
			}
			if (object.isSetTransform() && object.Collider != null) {
				setCollider(model, subject, object.Collider);
			}
			if (object.isSetProperties() && !object.Properties.isEmpty()) {
				setProperties(model, subject, object.Properties);
			}
		}
		return model;
	}

	private void setCollider(final Model model, final IRI subject, final MCollider collider) {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource transIRI = vf.createBNode();
		model.add(subject, MOSIMVocabulary.HAS_COLLIDER, transIRI);
		model.add(transIRI, rdfType, MOSIMVocabulary.M_COLLIDER);
		model.add(transIRI, MOSIMVocabulary.HAS_ID, vf.createLiteral(collider.ID));
	}

	private void setProperties(final Model model, final IRI subject, final Map<String,String> properties) {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			switch(entry.getKey()){
				case "type":
					model.add(subject, rdfType, vf.createIRI("http://www.dfki.de/mosim-ns#" + entry.getValue()));
					break;
				case "contains":
					addContainsEntries(model, subject, entry.getValue());
					break;
				case "initialLocation":
					model.add(subject, MOSIMVocabulary.HAS_INITIAL_LOCATION, getMSceneObjectIRI(entry.getValue()));
					break;
				case "isLocatedAt":
					model.add(subject, MOSIMVocabulary.IS_LOCATED_AT, getMSceneObjectIRI(entry.getValue()));
					break;
				case "finalLocation":
					model.add(subject, MOSIMVocabulary.HAS_FINAL_LOCATION, getMSceneObjectIRI(entry.getValue()));
					break;
				default:
					break;
			}
		}
	}

	private void addContainsEntries(final Model model, final IRI subject, String entries) {
		if (entries.startsWith("{") && entries.endsWith("}")) {
			entries = entries.replace("{", "");
			entries = entries.replace("}", "");
			String[] arrEntries = entries.split(",");
			for (int i = 0; i < arrEntries.length; i++)
				model.add(subject, MOSIMVocabulary.CONTAINS, getMSceneObjectIRI(arrEntries[i]));
		}
	}

	private IRI getMSceneObjectIRI(final String ID) {
		return vf.createIRI("tcp://" + host + ":" + port + "/" + ID);
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GetScene (" + getLabel() + ")";
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
