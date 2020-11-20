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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.math.MQuaternion;
import de.mosim.mmi.math.MTransform;
import de.mosim.mmi.math.MVector3;
import de.mosim.mmi.mmiConstants;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.scene.MSceneObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private final static int KEY = 0;
	private final static int VALUE = 1;
	
	protected final static ValueFactory vf = SimpleValueFactory.getInstance();
	
	public final static String OBJECT = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"SELECT ?id \n" +
			"WHERE {\n" +
			"	?object rdf:type mosim:MSceneObject .\n" +
			"	?object rdfs:label <name> .\n" +
			"	?object mosim:id ?id .\n" +
			"}";

	public final static String ACTION = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"SELECT ?id ?name\n" +
			"WHERE {\n" +
			"	?action mosim:actionID ?id .\n" +
			"	?action mosim:mmu ?name .\n" +
			"	?action mosim:actionName <actionName> .\n" +
			"}";

	public final static String HOST = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?host ?port \n" +
			"WHERE {\n" +
			"	?cosim rdf:type mosim:CoSimulator .\n" +
			"	?cosim mosim:host ?host .\n" +
			"	?cosim mosim:port ?port .\n" +
			"}";

	public final static String MSCENEOBJECT = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?id ?posX ?posY ?posZ ?rotX ?rotY ?rotZ ?rotW \n" +
			"WHERE {\n" +
			"	?object rdf:type mosim:MSceneObject .\n" +
			"	?object mosim:id <@ID> .\n" +
			"	?object mosim:transform ?transform .\n" +
			"	?transform rdf:type mosim:MTransform .\n" +
			"	?transform mosim:id ?id .\n" +
			"	?transform mosim:posX ?posX .\n" +
			"	?transform mosim:posY ?posY .\n" +
			"	?transform mosim:posZ ?posZ .\n" +
			"	?transform mosim:rotX ?rotX .\n" +
			"	?transform mosim:rotY ?rotY .\n" +
			"	?transform mosim:rotZ ?rotZ .\n" +
			"	?transform mosim:rotW ?rotW .\n" +
			"}";

	public final static String MTRANSFORM = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?id ?posX ?posY ?posZ ?rotX ?rotY ?rotZ ?rotW \n" +
			"WHERE {\n" +
			"	?transform rdf:type mosim:MTransform .\n" +
			"	?transform mosim:id ?id .\n" +
			"	?transform mosim:posX ?posX .\n" +
			"	?transform mosim:posY ?posY .\n" +
			"	?transform mosim:posZ ?posZ .\n" +
			"	?transform mosim:rotX ?rotX .\n" +
			"	?transform mosim:rotY ?rotY .\n" +
			"	?transform mosim:rotZ ?rotZ .\n" +
			"	?transform mosim:rotW ?rotW .\n" +
			"}";

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

	public static Map<String,String> createGeneralProperties(final String general, final Map<String,String> objectIDs, final AgentTaskInformation info) throws URISyntaxException {
		Map<String,String> properties = new HashMap();
		if (!general.equals("")) {
			String[] list = general.split(";");
			for (String list1 : list) {
				String[] keyvalue = list1.replace(" ", "").split(",");
				if (keyvalue[1].startsWith("<id@")) {
					String operator = getOperator(keyvalue[1]);
					String value = keyvalue[1].replace(operator, "").replace("<id@", "").replace(">", "");
					String newValue = objectIDs.get(value);
					properties.put(keyvalue[KEY], newValue + operator);
				} else if (keyvalue[1].startsWith("<actionId@")) {
					String operator = getOperator(keyvalue[1]);
					String newValue = getActionID(keyvalue[1].replace(operator, ""),info);
					properties.put(keyvalue[KEY], newValue + operator);
				} else {
					properties.put(keyvalue[KEY], keyvalue[VALUE]);
				}
			}
		}
		return properties;
	}

	public static void setHost(final AgentTaskInformation info, String cosimHost, int cosimPort) throws URISyntaxException {
		List<BindingSet> bindings = MOSIMUtil.getBindings(MOSIMUtil.HOST, info);
		if (!bindings.isEmpty()) {
			cosimHost = bindings.get(0).getValue("host").stringValue();
			cosimPort = Integer.parseInt(bindings.get(0).getValue("port").stringValue());
		}
	}

	private static String getOperator(final String input) {
		String[] part = input.split(">");
		if (part.length > 1)
			return part[1];
		else
			return "";
	}

	public static String getActionID(final String actionName, final AgentTaskInformation info) throws URISyntaxException {
		String value = actionName.replace("<actionId@", "").replace(">", "");
		String query = ACTION.replace("<actionName>", '"' + value + '"');
		List<BindingSet> bindings = getBindings(query,info);
		return bindings.get(0).getValue("id").stringValue();
	}

	public static String getConditionInput(final String cond, final AgentTaskInformation info) throws URISyntaxException {
		if (cond.startsWith("<actionId@")) {
			String operator = getOperator(cond);
			return getActionID(cond.replace(operator, ""), info) + operator;
		} else {
			return cond;
		}
	}

	public static Map<String,String> getObjectIDs(final AgentTaskInformation info, final String sparql, String objects) throws URISyntaxException {
		Map<String,String> objectIDs = new HashMap();
		String[] objectList = objects.replace(" ", "").split(";");
		for (String object: objectList) {
			String query = sparql;
			query = query.replace("<name>", '"' + object + '"');
			List<BindingSet> bindings = getBindings(query, info);
			objectIDs.put(object, bindings.get(0).getValue("id").stringValue());
		}
		return objectIDs;
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
        Model filter = model.filter(subject, predicate, null);
        Set<Value> objects = filter.objects();
        Value objj = new ArrayList<>(objects).get(0);
        return objj.stringValue();
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

	public static MSceneObject getMSceneObject(final String id, final Repository repo) {
		try (RepositoryConnection conn = repo.getConnection()) {
			String query = MSCENEOBJECT.replace("<@ID>", "'" + id + "'");
			TupleQuery tuplQ = conn.prepareTupleQuery(query);
			TupleQueryResult result = tuplQ.evaluate();
			List<BindingSet> bindings = new ArrayList();
			while (result.hasNext()) {
				bindings.add(result.next());
			}
			MSceneObject obj = new MSceneObject();
			obj.ID = id;
			obj.Name = bindings.get(0).getValue("posX").stringValue();
			obj.Transform = getTransform(bindings.get(0));
			return obj;
		} catch (Exception ex) {
			return null;
		}
	}

	public static MTransform getTransform(final BindingSet binding) throws URISyntaxException {
		MTransform transform = new MTransform();
		transform.ID = binding.getValue("id").stringValue();
		MVector3 position = new MVector3();
		position.X = Double.parseDouble(binding.getValue("posX").stringValue());
		position.Y = Double.parseDouble(binding.getValue("posY").stringValue());
		position.Z = Double.parseDouble(binding.getValue("posZ").stringValue());
		MQuaternion rotation = new MQuaternion();
		rotation.X = Double.parseDouble(binding.getValue("rotX").stringValue());
		rotation.Y = Double.parseDouble(binding.getValue("rotY").stringValue());
		rotation.Z = Double.parseDouble(binding.getValue("rotZ").stringValue());
		rotation.W = Double.parseDouble(binding.getValue("rotW").stringValue());
		transform.Position = position;
		transform.Rotation = rotation;
		return transform;
	}

	public static List<String> getTransformVars() {
		List<String> list = new ArrayList();
		list.add("id");
		list.add("posX");
		list.add("posY");
		list.add("posZ");
		list.add("rotX");
		list.add("rotY");
		list.add("rotZ");
		list.add("rotW");
		return list;
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

	public static void setTransform(final Model model, final Resource subject, final MTransform transform) {
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource transIRI = vf.createBNode();
		model.add(subject, MOSIMVocabulary.HAS_TRANSFORM, transIRI);
		model.add(transIRI, rdfType, MOSIMVocabulary.M_TRANSFORM);
		model.add(transIRI, MOSIMVocabulary.HAS_ID, vf.createLiteral(transform.ID));
		model.add(transIRI, MOSIMVocabulary.HAS_POS_X, vf.createLiteral(transform.Position.X));
		model.add(transIRI, MOSIMVocabulary.HAS_POS_Y, vf.createLiteral(transform.Position.Y));
		model.add(transIRI, MOSIMVocabulary.HAS_POS_Z, vf.createLiteral(transform.Position.Z));
		model.add(transIRI, MOSIMVocabulary.HAS_ROT_X, vf.createLiteral(transform.Rotation.X));
		model.add(transIRI, MOSIMVocabulary.HAS_ROT_Y, vf.createLiteral(transform.Rotation.Y));
		model.add(transIRI, MOSIMVocabulary.HAS_ROT_Z, vf.createLiteral(transform.Rotation.Z));
		model.add(transIRI, MOSIMVocabulary.HAS_ROT_W, vf.createLiteral(transform.Rotation.W));
	}
}
