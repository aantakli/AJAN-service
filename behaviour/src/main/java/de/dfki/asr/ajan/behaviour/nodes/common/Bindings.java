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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.AJANBindingsException;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;

@RDFBean("ajan:Bindings")
public class Bindings {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("ajan:bindingList")
	@Getter @Setter
	private List<Bound> list;

	@RDF("ajan:bindingQuery")
	@Getter @Setter
	private BehaviorSelectQuery query;

	public List<Bound> getBindings(final AgentTaskInformation metadata) throws AJANBindingsException, URISyntaxException {
		if (query == null) {
			if (list == null) {
				throw new AJANBindingsException("No bindings are defined!");
			}
			return list;
		}  else {
			return readInput(metadata);
		}
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private List<Bound> readInput(final AgentTaskInformation metadata) throws AJANBindingsException, URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(metadata, query.getOriginBase());
		List<BindingSet> result = query.getResult(repo);
		if (result.isEmpty()) {
			throw new AJANBindingsException("No bindings are available!");
		} else {
			List<Bound> bindings = new ArrayList();
			int i = 0;
			for (Binding binding: result.get(0)) {
				Bound bound = new Bound();
				bound.setUrl(url + "#" + i);
				bound.setDataType(getDataType(binding.getValue()));
				bound.setStringValue(binding.getValue().stringValue());
				bound.setVarName(binding.getName());
				bindings.add(bound);
				i++;
			}
			return bindings;
		}
	}

	private URI getDataType(final Value value) throws URISyntaxException {
		IRI type;
		if (value instanceof SimpleIRI) {
			type = RDFS.RESOURCE;
		} else if (value instanceof SimpleLiteral) {
			SimpleLiteral literal = (SimpleLiteral) value;
			type = literal.getDatatype();
		} else if (value instanceof SimpleBNode) {
			type = RDFS.RESOURCE;
		} else {
			type = XMLSchema.STRING;
		}
		return new URI(type.toString());
	}
}
