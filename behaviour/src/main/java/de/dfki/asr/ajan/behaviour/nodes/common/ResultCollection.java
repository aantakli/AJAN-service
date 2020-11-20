/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 *
 * @author Andre Antakli
 */
public class ResultCollection {

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	@Getter
	private final List<Resource> steps = new ArrayList();
	@Getter
	private final Model model = new LinkedHashModel();

	public Model getCollectionModel() {
		Model collection = new LinkedHashModel();
		Resource root = vf.createBNode();
		Resource head = vf.createBNode();
		collection.add(root, RDF.TYPE, BTVocabulary.BT_EVALUATION_RESULT);
		collection.add(root, BTVocabulary.HAS_STEPS, head);
		RDFCollections.asRDF(steps, head, collection);
		Iterator<Statement> itr = model.iterator();
		while (itr.hasNext()) {
			collection.add(itr.next());
		}
		return collection;
	}
}
