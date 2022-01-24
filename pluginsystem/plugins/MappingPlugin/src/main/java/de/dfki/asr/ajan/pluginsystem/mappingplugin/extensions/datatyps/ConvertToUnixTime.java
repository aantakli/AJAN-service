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

package de.dfki.asr.ajan.pluginsystem.mappingplugin.extensions.datatyps;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.vocabularies.MappingVocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:ConvertToUnixTime")
public class ConvertToUnixTime extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter @Setter
    private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;
	
	@RDF("bt:timeMapping")
    @Getter @Setter
    private BehaviorConstructQuery timeQuery;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI target;

    protected static final Logger LOG = LoggerFactory.getLogger(ConvertToUnixTime.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mapping#ConvertToUnixTime");
	}

    @Override
    public LeafStatus executeLeaf() {
		try {
			if (convertInput()) {
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
			} else {
				String report = toString() + " FAILED";
				LOG.info(report);
				return new LeafStatus(Status.FAILED, report);
			}
		} catch (URISyntaxException | ParseException | QueryInterruptedException ex) {
			String report = toString() + " FAILED";
			LOG.info(report, ex);
			return new LeafStatus(Status.FAILED, report);
		}
    }

    protected boolean convertInput() throws URISyntaxException, ParseException, QueryInterruptedException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), timeQuery.getOriginBase());
		Model resultModel = new LinkedHashModel();
		Model model = timeQuery.getResult(repo);
		if (model.contains(null, null, MappingVocabulary.DATETIME_PREDICATE)) {
			Set<IRI> mappings = Models.subjectIRIs(model.filter(null, null, MappingVocabulary.DATETIME_PREDICATE));
			if(mappings.isEmpty()) {
				return false;
			}
			for (IRI mapping: mappings) {
				Iterator<Statement> stmsIter = model.getStatements(mapping, MappingVocabulary.HAS_TIMEFORMAT, null).iterator();
				if (stmsIter.hasNext()) {
					Statement stmt = stmsIter.next();
					convert(model, mapping, stmt.getObject(), resultModel);
				}
			}
		}
		return writeModel(resultModel);
    }

	private void convert(final Model model, final IRI predicate, final Value format, final Model resultModel) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format.stringValue());
		Iterator<Statement> stmsIter = model.getStatements(null, predicate, null).iterator();
		while (stmsIter.hasNext()) {
			Statement stmt = stmsIter.next();
			Date dt = sdf.parse(stmt.getObject().stringValue());
			long epoch = dt.getTime();
			int result = (int)(epoch/1000);
			resultModel.add(stmt.getSubject(), predicate, vf.createLiteral(result));
		}
	}

    protected boolean writeModel(final Model model) throws URISyntaxException {
		boolean result = false;
		if (!model.isEmpty()) {
			if (target.equals(new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString()))) {
					this.getObject().getExecutionBeliefs().update(model);
			} else if (target.equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
					this.getObject().getAgentBeliefs().update(model);
			}
			result = true;
		}
		return result;
    }

    @Override
    public void end() {
            LOG.info("Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
            return "ConvertToUnixTime (" + getLabel() + ")";
    }

	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
