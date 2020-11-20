/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.standardbtnodes.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.exceptions.BeliefBaseUpdateException;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import java.net.URI;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:Timestamp")
public class Timestamp extends AbstractTDBLeafTask implements NodeExtension {
	@Getter @Setter
	@RDFSubject
	private String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:subject")
	@Getter @Setter
	private URI subject;

	@RDF("bt:predicate")
	@Getter @Setter
	private URI predicate;

	@RDF("bt:valueType")
	@Getter @Setter
	private URI valueType;

	@Override
	public Resource getType() {
		return StandardBTVocabulary.TIMESTAMP;
	}

	private static final Logger LOG = LoggerFactory.getLogger(Timestamp.class);

	@Override
	public LeafStatus executeLeaf() {
		try {
			setTimestamp();
			String report = toString() + " SUCCEEDED";
			LOG.info(report);
			return new LeafStatus(Status.SUCCEEDED, report);
		} catch (BeliefBaseUpdateException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED due to query evaluation error");
		}
	}

	private void setTimestamp() throws BeliefBaseUpdateException {
		try {
			Model model = getModel();
			this.getObject().getAgentBeliefs().update(model);
		} catch (DatatypeConfigurationException | QueryEvaluationException ex) {
			throw new BeliefBaseUpdateException(ex);
		}
	}

	private Model getModel() throws DatatypeConfigurationException {
		Model model = new LinkedHashModel();
		IRI sub = vf.createIRI(subject.toString());
		model.add(sub,org.eclipse.rdf4j.model.vocabulary.RDF.TYPE,StandardBTVocabulary.BT_PREDICATE);
		model.add(sub,vf.createIRI(predicate.toString()),getTimestamp());
		return model;
	}

	private Literal getTimestamp() throws DatatypeConfigurationException {
		long milliseconds = System.currentTimeMillis();
		if(XMLSchema.DATETIME.toString().equals(valueType.toString())) {
			GregorianCalendar gregory = new GregorianCalendar();
			gregory.setTimeInMillis(System.currentTimeMillis());
			return vf.createLiteral(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory));
		} else {
			return vf.createLiteral(milliseconds);
		}
	}

	@Override
	public void end() {
		LOG.info("Timestamp (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "Timestamp (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return Result.SUCCESS;
	}
}
