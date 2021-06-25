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

package de.dfki.asr.poser.util;

import de.dfki.asr.poser.Namespace.JSON;
import de.dfki.asr.poser.exceptions.DataTypeException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class RDFModelUtil {

	private static final Set<IRI> LITERAL_JSON_TYPES = getLiteralJsonTypes();

	/**
	 * Get the semantic representation of the JSON Object to be constructed from the JSON modelfile
	 * @param jsonType The type for which to build the JSON Object
	 * @param jsonModel The RDF model of the expected JSON API
	 * @return The model describing the Json Object for the given type
	 */
	public static Model getModelForJsonObject(String jsonType, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI predicateValueIri = vf.createIRI("http://some.json.ontology/dataType");
		IRI objectJsonTypeIri = vf.createIRI(jsonType);
		// get the subject of the model representing the data
		Resource jsonObjectDescription = jsonModel.filter(null, predicateValueIri, objectJsonTypeIri).subjects().iterator().next();
		Model jsonObjectModel = jsonModel.filter(jsonObjectDescription, null, null);
		return jsonObjectModel;
	}

	/**
	 * Get the Input Type the JSON API is expecting from the JSON Model file
	 * @param jsonModel The RDF description of the JSON API
	 * @return The expected input type
	 */
	public static String getDesiredInputType(Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI entryPoint = vf.createIRI("http://some.json.ontology/EntryPoint");
		for(Statement typeDescription: jsonModel.filter(entryPoint, null, null)) {
			Value typeObject = typeDescription.getObject();
			return typeObject.stringValue();
		}
		throw new NoSuchElementException("No entry point description found in JSON model");
	}

	/**
	 * Given a semantic representation of a API JSONObject from the jsonModelFile, get the
	 * value of the resource describing the key
	 * @param jsonObjectModel The Model to query for the name of the JSON key
	 * @return The Json key as String
	 */
	public static String getKeyForObject(Model jsonObjectModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI keyIri = vf.createIRI( "http://some.json.ontology/key");
		Optional<Literal> key = Models.objectLiteral(jsonObjectModel.filter(null, keyIri, null));
		if(!key.isPresent()) {
			throw new NoSuchElementException("JSON Object " + jsonObjectModel.toString() + " is missing a key");
		}
		return key.get().stringValue();
	}

	/**
	 * Get the resources from the provided json object model that will be represented as a literal,
	 * and as such need to be retrieved from the semantic input
	 * @param jsonObjectModel
	 * @return The list of values that need to be read from the semantic input data
	 */
	public static Value getValueTypeForObject(Model jsonObjectModel) {
		Set<Value> valueModel = jsonObjectModel.filter(null, RDF.TYPE , null).objects();
		if (valueModel.isEmpty()) {
			throw new NoSuchElementException("Object is missing a value definition");
		}
		if (valueModel.size() > 1) {
			throw new DataTypeException("Multiple data types defined for object");
		}
		return valueModel.iterator().next();
	}

	/**
	 * Check whether the given value is a literal, according to the json description model provided
	 * @param value the IRI of the value to be checked for being a literal
	 * @return true if the value is a literal, false otherwise
	 */
	public static boolean isLiteral(Value value) {
		return LITERAL_JSON_TYPES.contains(value);
	}

	/**
	 * Get the type of a value from the JSON descriptionModel
	 * @param value The JSON value for which the corresponding semantic type description is needed
	 * @param jsonModel The model representing the JSON payload description
	 * @return The value in string representation
	 */
	public static String getTypeOfValue(Value value, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI valueObjectIri = vf.createIRI(value.stringValue());
		IRI dataTypeIri = vf.createIRI("http://some.json.ontology/dataType");
		Set<Value> datatypes = jsonModel.filter(valueObjectIri, dataTypeIri, null).objects();
		if (datatypes.isEmpty()) {
			throw new NoSuchElementException("JSON object contains no reference to a semantic datatype");
		}
		return datatypes.iterator().next().stringValue();
	}

	public static String getPredicateNameForTypeFromModel(String dataType, Model jsonModel ) {
		IRI dataTypeIri = SimpleValueFactory.getInstance().createIRI(dataType);
		IRI dataTypeContext = SimpleValueFactory.getInstance().createIRI("http://some.json.ontology/InputDataType");
		Set<IRI> predicates = jsonModel.filter(dataTypeIri, null, null, dataTypeContext).predicates();
		if (predicates.size() != 1) {
			throw new DataTypeException("No unique data type definition found");
		}
		return predicates.iterator().next().stringValue();
	}

	/**
	 * Returns the type to be checked for in the input data set, depending on the description in the json object model
	 * @param jsonObjectModel The model for the JSON object to be generated
	 * @param jsonModel The model API description including the input data description
	 * @return The type of the value to check for in the input data type description
	 */
	public static String getCorrespondingInputValueType(Model jsonObjectModel, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI dataTypePredicateIri = vf.createIRI("http://some.json.ontology/dataType");
		Optional<IRI> apiDataType = Models.objectIRI(jsonObjectModel.filter(null, dataTypePredicateIri, null));
		if(!apiDataType.isPresent()) {
			throw new DataTypeException("No input data type found for a value");
		}
		IRI dataType = apiDataType.get();
		return dataType.stringValue();
	}

	/**
	 *
	 * @param resource The resource for which to get the model
	 * @param jsonModel The model of the json payload from which to retrieve the information
	 * @return A model representing a single resource, e.g. a single JSON element
	 */
	public static Model getModelForResource(Value resource, Model jsonModel) {
		IRI resourceIRI = SimpleValueFactory.getInstance().createIRI(resource.stringValue());
		Model childObjectModel = jsonModel.filter(resourceIRI, null, null);
		return childObjectModel;
	}

	private static Set<IRI> getLiteralJsonTypes() {
		Set<IRI> literalTypes = new HashSet<>();
		literalTypes.add(JSON.NUMBER);
		literalTypes.add(JSON.STRING);
		literalTypes.add(JSON.BOOLEAN);
		return literalTypes;
	}

	public static Model getRootObject(Model jsonModel) {
		Optional<IRI> subjOfRoot = Models.objectIRI(jsonModel.filter(null, JSON.HAS_ROOT, null));
		if(!subjOfRoot.isPresent()) {
			throw new NoSuchElementException("No root element found for JSON model");
		}
		Model rootObject = jsonModel.filter(subjOfRoot.get(), null, null);
		return rootObject;
	}
}
