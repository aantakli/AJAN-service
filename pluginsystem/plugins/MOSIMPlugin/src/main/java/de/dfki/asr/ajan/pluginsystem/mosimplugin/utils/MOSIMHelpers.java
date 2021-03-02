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
package de.dfki.asr.ajan.pluginsystem.mosimplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import static de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil.getBindings;
import static de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil.getTransform;
import de.mosim.mmi.scene.MSceneObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public final class MOSIMHelpers {

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
			"SELECT ?id ?object ?label \n" +
			"WHERE {\n" +
			"	?object rdf:type mosim:MSceneObject .\n" +
			"	?object mosim:id <@ID> .\n" +
			"	?object rdfs:label ?label .\n" +
			"	?object mosim:transform ?transform .\n" +
			"	?transform rdf:type mosim:MTransform .\n" +
			"	?transform mosim:id ?id .\n" +
			"	?transform mosim:object ?object .\n" +
			"}";

	public final static String MTRANSFORM = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT ?id ?object \n" +
			"WHERE {\n" +
			"	?transform rdf:type mosim:MTransform .\n" +
			"	?transform mosim:object ?object .\n" +
			"}";
	
	private MOSIMHelpers() {
	
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

	private static String getOperator(final String input) {
		String[] part = input.split(">");
		if (part.length > 1)
			return part[1];
		else
			return "";
	}

	public static void setHelperProperties(final Map<String,String> properties, String[] keyvalue, final Map<String,String> objectIDs, final AgentTaskInformation info) throws URISyntaxException {
		if (keyvalue[1].startsWith("<id@")) {
			String operator = getOperator(keyvalue[1]);
			String value = keyvalue[1].replace(operator, "").replace("<id@", "").replace(">", "");
			String newValue = objectIDs.get(value);
			properties.put(keyvalue[0], newValue + operator);
		} else if (keyvalue[1].startsWith("<actionId@")) {
			String operator = getOperator(keyvalue[1]);
			String newValue = getActionID(keyvalue[1].replace(operator, ""), info);
			properties.put(keyvalue[0], newValue + operator);
		}
	}
}