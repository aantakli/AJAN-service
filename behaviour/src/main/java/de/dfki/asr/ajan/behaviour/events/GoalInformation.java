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

package de.dfki.asr.ajan.behaviour.events;

import de.dfki.asr.ajan.behaviour.nodes.common.Bound;
import de.dfki.asr.ajan.common.AJANVocabulary;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class GoalInformation {

	private final List<Bound> bindings;

	public GoalInformation(final List<Bound> bindings) {
		this.bindings = bindings;
	}

	public Model getModel() {
		Model model = new LinkedHashModel();
		ValueFactory vf = SimpleValueFactory.getInstance();
		Resource subject = vf.createBNode();
		Resource list = vf.createBNode();
		model.add(subject, RDF.TYPE, AJANVocabulary.GOAL_INFORMATION);
		List<Resource> bndList = new ArrayList();
		for (Bound bound: bindings) {
			bndList.add(bound.setModel(model));
		}
		model = RDFCollections.asRDF(bndList,list,model);
		model.add(subject, AJANVocabulary.HAS_BINDINGS,list);
		return model;
	}
}
