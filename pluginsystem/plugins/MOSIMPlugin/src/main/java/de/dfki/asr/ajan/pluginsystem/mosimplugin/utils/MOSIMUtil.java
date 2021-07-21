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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.constraints.MGeometryConstraint;
import de.mosim.mmi.math.MQuaternion;
import de.mosim.mmi.math.MTransform;
import de.mosim.mmi.mmiConstants;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.scene.MCollider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author Andre Antakli
 */
public final class MOSIMUtil {
	
	protected final static ValueFactory vf = SimpleValueFactory.getInstance();

	private MOSIMUtil() {

	}	

	public static MInstruction createMInstruction(
										final String instID,
										final String actionID,
										final String mmu, 
										final Map<String,String> properties, 
										final List<MConstraint> constraints,
										final String startCdt,
										final String endCdt){
		MInstruction instruction = new MInstruction();
		instruction.setID(instID);
		instruction.setName(actionID);
		instruction.setMotionType(mmu);
		if (properties != null && !properties.isEmpty())
			instruction.setProperties(properties);
		if (constraints != null && !constraints.isEmpty())
			instruction.setConstraints(constraints);
		if (startCdt != null && !startCdt.equals(""))
			instruction.setStartCondition(startCdt);
		if (endCdt != null && !endCdt.equals(""))
			instruction.setEndCondition(endCdt);
		return instruction;
	}

	public static String getEndEvent(final String actionID) {
		return actionID + ":" + mmiConstants.MSimulationEvent_End;
	}

	public static String getStartEvent(final String actionID) {
		return actionID + ":" + mmiConstants.MSimulationEvent_Start;
	}

	public static String getEndInstruction(final String actionID) {
		return actionID + ":" + "EndInstruction";
	}

	public static String getStartInstruction(final String actionID) {
		return actionID + ":" + "StartInstruction";
	}

	public static Map<String,String> createWalkProperties(final String targetID) {
		Map<String,String> properties = new HashMap();
		properties.put("TargetID", targetID);
		return properties;
	}

	public static Map<String,String> createReachProperties(final String targetID, final String hand) {
		Map<String,String> properties = new HashMap();
		properties.put("TargetID", targetID);
		properties.put("Hand", hand);
		return properties;
	}

	public static Map<String,String> createCarryProperties(final String targetID, final String hand) {
		Map<String,String> properties = new HashMap();
		properties.put("TargetID", targetID);
		properties.put("Hand", hand);
		properties.put("AddOffset", "false");
		return properties;
	}

	public static Map<String,String> createMoveProperties(final String subjectID, final String targetID, final String hand, final String start, final String end) {
		Map<String,String> properties = new HashMap();
		properties.put("SubjectID", subjectID);
		properties.put("TargetID", targetID);
		properties.put("Hand", hand);
		if (start != null) {
			properties.put("OnStart", start);
		}
		if (end != null) {
			properties.put("OnEnd", end);
		}
		return properties;
	}

	public static Map<String,String> createReleaseProperties(final String hand, final String start, final String end) {
		Map<String,String> properties = new HashMap();
		properties.put("Hand", hand);
		if (start != null) {
			properties.put("OnStart", start);
		}
		if (end != null) {
			properties.put("OnEnd", end);
		}
		return properties;
	}

	public static Map<String,String> createGeneralProperties(final ArrayList<Value> values, final Model inputModel) throws URISyntaxException {
		Map<String,String> properties = new HashMap();
		if (!values.isEmpty()) {
			for (Value val: values) {
				String key = getObject(inputModel, (IRI) val, MOSIMVocabulary.HAS_KEY);
				String value = getObject(inputModel, (IRI) val, MOSIMVocabulary.HAS_VALUE);
				properties.put(key, value);
			}
		}
		return properties;
	}

	public static String getInstructionDef(final MInstruction instruction) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.valueToTree(instruction);
		return node.toString();
	}

	public static Map<String,IRI> getConstraintObj64(final ArrayList<Value> values, final Model inputModel) {
		Map<String,IRI> map = new HashMap();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		if (!values.isEmpty()) {
			for (Value val: values) {
				String type = getObject(inputModel, (Resource) val, rdfType);
				String obj64 = getObject(inputModel, (Resource) val, MOSIMVocabulary.HAS_OBJECT);
				map.put(obj64, vf.createIRI(type));
			}
		}
		return map;
	}

	public static List<MConstraint> createConstraints(final Map<String,IRI> obj64s) {
		List<MConstraint> list = new ArrayList();
		for (Entry<String,IRI> entry: obj64s.entrySet()) {
			MConstraint constraint = getConstraint(entry.getKey(), entry.getValue());
			list.add(constraint);
		}
		return list;
	}

	public static MConstraint getConstraint(final String obj64, final IRI type) {
		try {
			if (type.toString().equals(MOSIMVocabulary.M_CONSTRAINT.toString())) {
				return (MConstraint) MOSIMUtil.decodeObjectBase64(obj64);
			}
			else if (type.toString().equals(MOSIMVocabulary.M_TRANSFORM.toString())) {
				MConstraint constraint = new MConstraint();
				MGeometryConstraint geo = new MGeometryConstraint();
				geo.ParentObjectID = "";
				MTransform trans = (MTransform) MOSIMUtil.decodeObjectBase64(obj64);
				geo.ParentToConstraint = trans;
				constraint.ID = trans.ID;
				constraint.GeometryConstraint = geo;
				return constraint;
			}
			else {
				return new MConstraint(); 
			}
		} catch (IOException | ClassNotFoundException ex) {
			return new MConstraint();
		}
	}

	public static void setHost(final AgentTaskInformation info, String cosimHost, int cosimPort) throws URISyntaxException {
		List<BindingSet> bindings = MOSIMUtil.getBindings(MOSIMHelpers.HOST, info);
		if (!bindings.isEmpty()) {
			cosimHost = bindings.get(0).getValue("host").stringValue();
			cosimPort = Integer.parseInt(bindings.get(0).getValue("port").stringValue());
		}
	}

	public static List<BindingSet> getBindings(final String sparql, final AgentTaskInformation info) throws URISyntaxException {
		URI originBase = new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString());
		Repository repo = BTUtil.getInitializedRepository(info, originBase);
		try (RepositoryConnection conn = repo.getConnection()) {
			TupleQuery query = conn.prepareTupleQuery(sparql);
			TupleQueryResult result = query.evaluate();
			List<BindingSet> bindings = new ArrayList();
			while (result.hasNext()) {
				bindings.add(result.next());
			}
			return bindings;
		}
	}

    public static Resource getResource(final Model model, final IRI predicate, final IRI resource) {
        Model filter = model.filter(null, predicate, resource);
        Set<Resource> subjects = filter.subjects();
		if(subjects.isEmpty()) {
			return null;
		}
        Resource resj = new ArrayList<>(subjects).get(0);
        return resj;
    }

	public static String getObject(final Model model, final Resource subject, final IRI predicate) {
        ArrayList<Value> objects = getObjects(model, subject, predicate);
		if (objects.isEmpty()) {
			return "";
		}
        return objects.get(0).stringValue();
    }

	public static ArrayList<Value> getObjects(final Model model, final Resource subject, final IRI predicate) {
        Model filter = model.filter(subject, predicate, null);
        Set<Value> objects = filter.objects();
		return new ArrayList<>(objects);
    }

	public static void writeInput(final Model model, final String repository, final AgentTaskInformation info) {
		if (repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			info.getExecutionBeliefs().update(model);
		} else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			info.getAgentBeliefs().update(model);
		}
	}

	public static void removeInput(final Model model, final String repository, final AgentTaskInformation info) {
		if (repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			info.getExecutionBeliefs().update(new LinkedHashModel(), model, false);
		} else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			info.getAgentBeliefs().update(new LinkedHashModel(), model, false);
		}
	}

	public static Map<String,String> getHostInfos(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
		Map<String,String> host = new HashMap();
		if (query != null) {
			Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
			List<BindingSet> result = query.getResult(repo);
			if (!result.isEmpty()) {
				BindingSet bindings = result.get(0);
				host.put(bindings.getValue("host").stringValue(), bindings.getValue("port").stringValue());
			}
		}
		return host;
	}
	
	public static int getPortInfos(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
		List<BindingSet> result = query.getResult(repo);
		if (!result.isEmpty()) {
			BindingSet bindings = result.get(0);
			return Integer.parseInt(bindings.getValue("port").stringValue());
		}
		return 0;
	}

	public static MTransform getTransform(final BindingSet binding) throws URISyntaxException, IOException, ClassNotFoundException {
		MTransform transform = (MTransform) decodeObjectBase64(binding.getValue("object").stringValue());
		return transform;
	}
	
	public static MTransform getLookAtTransform(final MTransform sourcePoint, final MTransform destPoint) {
		Vector3D srcVec = new Vector3D(sourcePoint.Position.X,sourcePoint.Position.Y,sourcePoint.Position.Z);
		Vector3D dstVec = new Vector3D(destPoint.Position.X,destPoint.Position.Y,destPoint.Position.Z);
		Vector3D direction = dstVec.subtract(srcVec).normalize();

		Vector3D front = new Vector3D(0,0,1);
		Vector3D up = new Vector3D(0,1,0);
		Vector3D rotAxis = front.crossProduct(direction);
		if (rotAxis.getNormSq() == 0)
			rotAxis = up;
		double dot = front.dotProduct(direction);
		double angl = Math.acos(dot);
		double s = Math.sin(angl/2);
		Vector3D u = rotAxis.normalize();		
		Quaternion q = new Quaternion(u.getX()*s,u.getY()*s,u.getZ()*s,Math.cos(angl/2));
		return sourcePoint.setRotation(new MQuaternion(q.getQ0(),q.getQ1(),q.getQ2(),q.getQ3()));
	}

	public static void setTransform(final Model model, final Resource subject, final MTransform transform) throws IOException {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource transIRI = vf.createBNode();
		model.add(subject, MOSIMVocabulary.HAS_TRANSFORM, transIRI);
		model.add(transIRI, rdfType, MOSIMVocabulary.M_TRANSFORM);
		model.add(transIRI, MOSIMVocabulary.HAS_ID, vf.createLiteral(transform.ID));
		model.add(transIRI, MOSIMVocabulary.HAS_OBJECT, vf.createLiteral(encodeObjectBase64(transform)));
	}

	public static void setCollider(final Model model, final IRI subject, final MCollider collider) throws IOException {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource transIRI = vf.createBNode();
		model.add(subject, MOSIMVocabulary.HAS_COLLIDER, transIRI);
		model.add(transIRI, rdfType, MOSIMVocabulary.M_COLLIDER);
		model.add(transIRI, MOSIMVocabulary.HAS_ID, vf.createLiteral(collider.ID));
		model.add(transIRI, MOSIMVocabulary.HAS_OBJECT, vf.createLiteral(encodeObjectBase64(collider)));
	}

	public static void setConstraint(final Model model, final Resource subject, final MConstraint constr) throws IOException {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource transIRI = vf.createBNode();
		model.add(subject, MOSIMVocabulary.HAS_CONSTRAINT, transIRI);
		model.add(transIRI, rdfType, MOSIMVocabulary.M_CONSTRAINT);
		model.add(transIRI, MOSIMVocabulary.HAS_ID, vf.createLiteral(constr.ID));
		model.add(transIRI, MOSIMVocabulary.HAS_OBJECT, vf.createLiteral(encodeObjectBase64(constr)));
	}

	public static String encodeObjectBase64(final Object object) throws IOException {
		String output = "";
		try (ByteArrayOutputStream objIn = new ByteArrayOutputStream()) {
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(objIn)) {
				objectOutputStream.writeObject(object);
			}
			output = Base64.getEncoder().encodeToString(objIn.toByteArray());
		}
		return output;
	}

	public static Object decodeObjectBase64(final String encoded) throws IOException, ClassNotFoundException {
		byte[] decoded = Base64.getDecoder().decode(encoded);
		InputStream objOut = new ByteArrayInputStream(decoded);
		ObjectInputStream objectInputStream = new ObjectInputStream(objOut);
		return objectInputStream.readObject();
	}

	public static String getTimeStamp() {
		Date date= new Date();
		Timestamp ts = new Timestamp(date.getTime());
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(ts);
	}
}
