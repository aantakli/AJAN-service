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

import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@RDFBean("ajan:Bound")
public class Bound {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("sp:varName")
	@Getter @Setter
	private String varName;

	@RDF("ajan:dataType")
	@Getter @Setter
	protected URI dataType;

	@RDF("rdf:value")
	@Getter @Setter
	private String stringValue = "blub";

	@SuppressWarnings("PMD.UncommentedEmptyConstructor")
	public Bound() {

	}

	public Bound(final String url, final String varName, final String value) {
		this.url = url;
		this.varName = varName;
		this.stringValue = value;
	}

	public Resource setModel(final Model model) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		Resource subject = AgentUtil.getSubject(url);
		model.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, AJANVocabulary.BOUND);
		model.add(subject, ACTNVocabulary.SPIN_VAR_NAME, vf.createLiteral(varName));
		if (dataType.toString().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
			model.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.VALUE, vf.createIRI(stringValue));
		} else {
			model.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.VALUE, vf.createLiteral(stringValue, vf.createIRI(dataType.toString())));
		}
		return subject;
	}
}
