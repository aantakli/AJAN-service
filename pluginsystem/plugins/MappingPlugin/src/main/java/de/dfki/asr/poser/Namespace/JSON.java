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

package de.dfki.asr.poser.Namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class JSON {
	public static final String NAMESPACE = "http://some.json.ontology/";
	public static final String PREFIX = "json";
	public static final IRI NUMBER = getIri("Number");
	public static final IRI STRING = getIri("String");
	public static final IRI OBJECT = getIri("Object");
	public static final IRI ARRAY = getIri("Array");
	public static final IRI BOOLEAN = getIri("Boolean");
	public static final IRI VALUE = getIri("value");
	public static final IRI DATA_TYPE = getIri("dataType");
	public static final IRI INPUT_DATA_TYPE = getIri("InputDataType");
	public static final IRI ROOT = getIri("hasRoot");

	private static IRI getIri(String localName) {
		return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
	}
}
