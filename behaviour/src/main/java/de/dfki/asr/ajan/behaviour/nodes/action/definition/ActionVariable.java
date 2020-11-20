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

package de.dfki.asr.ajan.behaviour.nodes.action.definition;

import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@RDFBean("actn:ActionVariable")
@SuppressWarnings("CPD-START")
public class ActionVariable {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("sp:varName")
	@Getter @Setter
	private String varName;

	@SuppressWarnings("PMD.UncommentedEmptyConstructor")
	public ActionVariable() {

	}

	public ActionVariable(final String url, final String varName) {
		this.url = url;
		this.varName = varName;
	}

	public Resource setModel(final Model model) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		Resource subject = AgentUtil.getSubject(url);
		model.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ACTNVocabulary.ACTION_VARIABLE);
		model.add(subject, ACTNVocabulary.SPIN_VAR_NAME, vf.createLiteral(varName));
		return subject;
	}
}
