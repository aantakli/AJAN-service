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
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.services.MSceneAccess;
import de.mosim.mmi.scene.MSceneObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

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

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetScene");
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query,this.getObject());
			if(!hostMap.isEmpty()) {
				host = hostMap.get("host");
				port = Integer.parseInt(hostMap.get("port"));
				List<MSceneObject> objects = getSceneObjects();
				if (objects == null) {
					String report = toString() + " FAILED";
					return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
				}
				Model inputModel = getInputModel(objects);
				MOSIMUtil.writeInput(inputModel, repository.toString(), this.getObject());
				String report = toString() + " SUCCEEDED";
				return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
			}
		} catch (URISyntaxException | IOException | ClassNotFoundException ex) {
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
		}
		String report = toString() + " FAILED";
		return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
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
			this.getObject().getLogger().info(this.getClass(), "Could not load List<MSceneObject>", ex);
			return null;
		}
	}

	private Model getInputModel(final List<MSceneObject> objects) throws IOException, ClassNotFoundException {
		Model model = new LinkedHashModel();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		for (MSceneObject object: objects) {
			IRI subject = getMSceneObjectIRI(object.ID);
			model.add(subject, rdfType, MOSIMVocabulary.M_SCENE_OBJECT);
			model.add(subject, MOSIMVocabulary.HAS_ID, vf.createLiteral(object.ID));
			model.add(subject, RDFS.LABEL, vf.createLiteral(object.Name));
			model.add(subject, MOSIMVocabulary.HAS_OBJECT, vf.createLiteral(MOSIMUtil.encodeObjectBase64(object)));
			if (object.isSetProperties() && !object.Properties.isEmpty()) {
				setProperties(model, subject, object.Properties);
			}
			if (object.isSetTransform() && object.Transform != null) {
				MOSIMUtil.setTransform(model, subject, object.Transform);
			}
			if (object.isSetCollider() && object.Collider != null) {
				MOSIMUtil.setCollider(model, subject, object.Collider);
			}
			if (object.isSetConstraints()&& !object.Constraints.isEmpty()) {
				for (MConstraint contst: object.Constraints) {
					MOSIMUtil.setConstraint(model, subject, contst);
				}
			}
		}
		return model;
	}

	private void setProperties(final Model model, final IRI subject, final Map<String,String> properties) throws IOException {
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
				case "RDF":
					setRDF(model, subject, entry.getValue());
					break;
				case "hasChildren":
					setChildren(model, subject, entry.getValue());
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

	private void setRDF(final Model model, final IRI subject, final String RDF) throws IOException {
		if (!RDF.equals("")) {
			InputStream input = new ByteArrayInputStream(RDF.getBytes());
			Model additionalRDF = Rio.parse(input, "", RDFFormat.TURTLE);
			Iterable<Statement> stmts = additionalRDF.getStatements(null, null, null);
			Iterator<Statement> itr = stmts.iterator();
			while (itr.hasNext()) {
				Statement stmt = itr.next();
				if (stmt.getSubject().equals(vf.createIRI("http://www.dfki.de/mosim-ns#This"))) {
					model.add(subject, stmt.getPredicate(), stmt.getObject());
				} else {
					model.add(stmt);
				}
			}
		}
	}

	private void setChildren(final Model model, final IRI subject, final String children) {
		for (String child : children.split(",")) {
			model.add(subject, MOSIMVocabulary.HAS_CHILD, getMSceneObjectIRI(child));
		}
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
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
