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

package de.dfki.asr.ajan.startup;

import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionPluginBinding;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public final class InitUtil {

	private final static ValueFactory VF = SimpleValueFactory.getInstance();

	private InitUtil() {

	}

	public static Model getActionDescription(final ActionPluginBinding plugin) {
		Model model = new LinkedHashModel();
		Resource subject = VF.createIRI("http://localhost:8090/rdf4j/repositories/services#" + plugin.getLable());
		model.add(subject, RDF.TYPE, ACTNVocabulary.PLUGIN_ACTION);
                model.add(subject, RDF.TYPE, plugin.getActionType());
		model.add(subject, ACTNVocabulary.COMMUNICATION, plugin.getCommunication().getValue());
		model.add(subject, ACTNVocabulary.VARIABLES, setVariables(model, plugin));
		model.add(subject, ACTNVocabulary.CONSUMES, setConsumes(model, plugin));
		model.add(subject, ACTNVocabulary.PRODUCES, setProduces(model, plugin));
		model.add(subject, RDFS.LABEL, VF.createLiteral(plugin.getLable()));
		model.add(subject, ACTNVocabulary.DCT_DESCRIPTION, VF.createLiteral(plugin.getDescription()));
		model.add(subject, ACTNVocabulary.RUN_BINDING, setRunBinding(model, plugin));
		return model;
	}

	private static Resource setVariables(final Model model, final ActionPluginBinding plugin) {
		Resource head = VF.createBNode();
		List<Resource> vars = new ArrayList();
		plugin.getVariables().stream().forEach((var) -> {
			vars.add(var.setModel(model));
		});
		RDFCollections.asRDF(vars, head, model);
		return head;
	}

	private static Resource setConsumes(final Model model, final ActionPluginBinding plugin) {
		Resource head = VF.createBNode();
		model.add(head, RDF.TYPE, ACTNVocabulary.CONSUMABLE);
		model.add(head, ACTNVocabulary.SPARQL, VF.createLiteral(plugin.getConsumable().getSparql()));
		return head;
	}

	private static Resource setProduces(final Model model, final ActionPluginBinding plugin) {
		Resource head = VF.createBNode();
		model.add(head, RDF.TYPE, ACTNVocabulary.PRODUCIBLE);
		model.add(head, ACTNVocabulary.SPARQL, VF.createLiteral(plugin.getProducible().getSparql()));
		return head;
	}

	private static Resource setRunBinding(final Model model, final ActionPluginBinding plugin) {
		Resource head = VF.createBNode();
		model.add(head, RDF.TYPE, ACTNVocabulary.BINDING);
                model.add(head, RDF.TYPE, plugin.getType());
		return head;
	}
}
