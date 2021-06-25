/*
 * Copyright (C) 2021 Daniel Spieldenner (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.poser.Converter;

import de.dfki.asr.poser.Namespace.JSON;
import de.dfki.asr.poser.exceptions.DataTypeException;
import de.dfki.asr.poser.util.InputDataReader;
import de.dfki.asr.poser.util.RDFModelUtil;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RdfToJson {

	private Model jsonModel;
	/**
	 * Receives data in ttl format, finds the corresponding json objects to be generated
	 * in the semantic description of the target payload and generates the necessary objects
	 * including parent data structures to embed it in
	 * @param inputModel the data in semantic representation sent by the remote service
	 * @param jsonModel the semantic representation of the JSON to generate
	 * @return
	 */
	public String buildJsonString(final Model inputModel, final Model jsonModel) {
		this.jsonModel = jsonModel;

		inputModel.setNamespace("rdfs", "https://www.w3.org/TR/rdf-schema/");
		inputModel.setNamespace("json", "http://some.json.ontology/");
		// check the RDF description of the JSON API model for the desired input type
		Model rootObject = RDFModelUtil.getRootObject(jsonModel);
		JSONObject jsonResult = new JSONObject();
		Object object = buildJsonObjectFromModel(rootObject, inputModel, jsonResult);
		if (object instanceof JSONArray)
			return ((JSONArray)object).toString();
		else 
			return ((JSONObject)object).toString();
	}

	private Object buildJsonObjectFromModel(Model objectModel, Model inputModel, JSONObject resultObject) {
		Value jsonDataType = RDFModelUtil.getValueTypeForObject(objectModel);
		if (JSON.ROOT_OBJECT.equals(jsonDataType)) {
			JSONObject childJSON = resultObject;
			Set<Value> childValues = getChildValuesOfRoot(objectModel);
			for(Value child: childValues) {
				Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
				childJSON = (JSONObject)buildJsonObjectFromModel(childObjectModel, inputModel, childJSON);
			}
			return childJSON;
		}
		if (JSON.ROOT_ARRAY.equals(jsonDataType)) {
			JSONArray array = new JSONArray();
			Set<Value> childValues = getChildValuesOfRoot(objectModel);
			if(objectModel.predicates().contains(JSON.DATA_TYPE)) {
				String valueType = RDFModelUtil.getCorrespondingInputValueType(objectModel, jsonModel);
				Model dataTypeModel = InputDataReader.getModelForType(valueType, inputModel);
				for(Resource subj: dataTypeModel.subjects()) {
					Model subInputModel = InputDataReader.getSubInputModel(subj, inputModel);
					JSONObject childJSON = new JSONObject();
					for(Value child: childValues) {
						Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
						array.put(buildJsonObjectFromModel(childObjectModel, subInputModel, childJSON));
					}
				}
			}
			return array;
		}
		String jsonKey = RDFModelUtil.getKeyForObject(objectModel);
		if (!RDFModelUtil.isLiteral(jsonDataType)) {
			Set<Value> childValues = getChildValuesOfJsonObject(objectModel, jsonKey);
			//if this object maps to a input data type, make sure to generate an object for each corresponding value set
			if(objectModel.predicates().contains(JSON.DATA_TYPE)) {
				String valueType = RDFModelUtil.getCorrespondingInputValueType(objectModel, jsonModel);
				Model dataTypeModel = InputDataReader.getModelForType(valueType, inputModel);
				for(Resource subj: dataTypeModel.subjects()) {
					Model subInputModel = InputDataReader.getSubInputModel(subj, inputModel);
					JSONObject childJSON = new JSONObject();
					for(Value child: childValues) {
						Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
						childJSON = (JSONObject)buildJsonObjectFromModel(childObjectModel, subInputModel, childJSON);
					}
					addToParentObject(jsonDataType, resultObject, jsonKey, childJSON);
				}
				return resultObject;
			}
			return createChildObjectsForValues(childValues, inputModel, jsonDataType, resultObject, jsonKey);
		}
		else {
			String valueType = RDFModelUtil.getCorrespondingInputValueType(objectModel, jsonModel);
			String propertyName = RDFModelUtil.getPredicateNameForTypeFromModel(valueType, jsonModel);
			String valueResult = InputDataReader.getValueForType(valueType, propertyName, inputModel);
			return addLiteralValueToKey(jsonDataType, resultObject, jsonKey, valueResult);
		}
	}

	private JSONObject createChildObjectsForValues(Set<Value> childValues, Model inputModel1, Value jsonDataType, JSONObject resultObject, String jsonKey) throws JSONException, DataTypeException {
		JSONObject childJSON = new JSONObject();
		for (Value child : childValues) {
			Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
			childJSON = (JSONObject)buildJsonObjectFromModel(childObjectModel, inputModel1, childJSON);
			addToParentObject(jsonDataType, resultObject, jsonKey, childJSON);
		}
		return resultObject;
	}

	private JSONObject addLiteralValueToKey(Value jsonDataType, JSONObject resultObject, String jsonKey, String valueResult) throws NumberFormatException {
		if (JSON.NUMBER.equals(jsonDataType)) {
			return addToResult(resultObject, jsonKey, Double.parseDouble(valueResult));
		}
		else if (JSON.BOOLEAN.equals(jsonDataType)) {
			Boolean resultValue = ("1".equals(valueResult) || "true".equalsIgnoreCase(valueResult));
			return addToResult(resultObject, jsonKey, resultValue);
		}
		else {
			return addToResult(resultObject, jsonKey, valueResult);
		}
	}

	private void addToParentObject(Value jsonDataType, JSONObject resultObject, String jsonKey, JSONObject childJSON) throws JSONException, DataTypeException {
		if (JSON.OBJECT.equals(jsonDataType)) {
			resultObject.put(jsonKey, childJSON);
		} else if (JSON.ARRAY.equals(jsonDataType)) {
			resultObject.append(jsonKey, childJSON);
		}  else {
			throw new DataTypeException("Could not determine JSON collection type");
		}
	}

	private Set<Value> getChildValuesOfJsonObject(Model objectModel, String jsonKey) throws DataTypeException {
		Set<Value> childValues = objectModel.filter(null, JSON.VALUE, null).objects();
		if(childValues.isEmpty()) {
			throw new DataTypeException("No value found for key " + jsonKey);
		}
		return childValues;
	}

	private Set<Value> getChildValuesOfRoot(Model objectModel) throws DataTypeException {
		Set<Value> childValues = objectModel.filter(null, JSON.VALUE, null).objects();
		if(childValues.isEmpty()) {
			throw new DataTypeException("No value found for Root");
		}
		return childValues;
	}

	private JSONObject addToResult(JSONObject resultObject, String jsonKey, Object jsonValue) {
		return resultObject.put(jsonKey, jsonValue);
	}
}