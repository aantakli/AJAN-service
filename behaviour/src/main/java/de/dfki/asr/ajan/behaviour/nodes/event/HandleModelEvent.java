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

package de.dfki.asr.ajan.behaviour.nodes.event;

import de.dfki.asr.ajan.behaviour.events.ModelEventInformation;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.common.AgentUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

@RDFBean("bt:HandleEvent")
public class HandleModelEvent extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:validate")
	@Getter @Setter
	private BehaviorConstructQuery query;
	protected BehaviorConstructQuery constructQuery;
	private IRI context;
	protected static final Logger LOG = LoggerFactory.getLogger(HandleModelEvent.class);

	@Override
	public Resource getType() {
		return BTVocabulary.HANDLE_EVENT;
	}

	@Override
	public void reset() {
		if (constructQuery != null) {
			constructQuery.setReset(true);
		}
		super.reset();
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			constructQuery = query;
			if (handleEvent()) {
				LOG.info(toString() + " SUCCEEDED");
				return new LeafStatus(Status.SUCCEEDED, toString() + " SUCCEEDED");
			} else {
				LOG.info(toString() + " FAILED");
				return new LeafStatus(Status.FAILED, toString() + " FAILED");
			}
		} catch (ConditionEvaluationException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	protected boolean handleEvent() throws ConditionEvaluationException {
		boolean result = false;
		try {
			Model model = getEventModel();
			if (!model.isEmpty()) {
				if (constructQuery.getTargetBase().equals(new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString()))) {
					this.getObject().getExecutionBeliefs().update(addNamedGraph(model));
				} else if (constructQuery.getTargetBase().equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
					this.getObject().getAgentBeliefs().update(addNamedGraph(model));
				}
				result = true;
			}
		} catch (QueryEvaluationException | URISyntaxException ex) {
			throw new ConditionEvaluationException(ex);
		}
		return result;
	}

	protected Model getEventModel() {
		Model model = new LinkedHashModel();
		Object info = this.getObject().getEventInformation();
		if (info instanceof ModelEventInformation) {
			if (constructQuery == null || constructQuery.getSparql().isEmpty()) {
				model = ((ModelEventInformation) info).getModel();
			} else {
				model = constructQuery.getResult(((ModelEventInformation) info).getModel());
				if (model.contains(null, BTVocabulary.HAS_EVENT_CONTEXT, null)) {
					Optional<IRI> contextIRI = Models.objectIRI(model.filter(null, BTVocabulary.HAS_EVENT_CONTEXT, null));
					if (contextIRI.isPresent()) {
						context = contextIRI.get();
					}
					model.remove(null, BTVocabulary.HAS_EVENT_CONTEXT, null);
				}
			}
		}
		return model;
	}

	protected Model addNamedGraph(final Model model) {
		if (context != null) {
			String ctx = context.toString();
			String graphName = ctx + "_" + UUID.randomUUID().toString();
			model.add(vf.createIRI(graphName), org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, vf.createIRI(ctx));
			model.add(vf.createIRI(graphName), BTVocabulary.HAS_TIMESTAMP, vf.createLiteral(createTimeStamp(), XMLSchema.DATETIME));
			return AgentUtil.setNamedGraph(model.iterator(), graphName);
		}
		return model;
	}

	protected String createTimeStamp() {
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		return formatter.format(dateTime);
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "HandleEvent (" + label + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL) && constructQuery != null) {
			constructQuery.setResultModel(getInstance(root.getInstance()), BTVocabulary.VALIDATE_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.UNCLEAR;
	}
}
