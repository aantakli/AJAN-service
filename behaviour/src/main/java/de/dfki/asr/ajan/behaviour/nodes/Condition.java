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

package de.dfki.asr.ajan.behaviour.nodes;

import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorAskQuery;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:Condition")
public class Condition extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:query")
	@Getter @Setter
	private BehaviorAskQuery query;
	private static final Logger LOG = LoggerFactory.getLogger(Condition.class);

	@Override
	public Resource getType() {
		return BTVocabulary.CONDITION;
	}

	@Override
	public void reset() {
		query.setReset(true);
		super.reset();
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			if (performConditionLogic(repo)) {
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
			} else {
				String report = toString() + " FAILED";
				LOG.info(report);
				return new LeafStatus(Status.FAILED, report);
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED due to evaluation error";
			LOG.info(report, ex);
			return new LeafStatus(Status.FAILED, report);
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	private Boolean performConditionLogic(final Repository repo) {
		try {
			return query.getResult(repo);
		} catch (QueryEvaluationException ex) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Condition (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		if (performConditionLogic(result.getRepo().initialize())) {
			return Result.SUCCESS;
		} else {
			return Result.FAIL;
		}
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(ModelMode.DETAIL)) {
			query.setResultModel(getInstance(root.getInstance()), BTVocabulary.CONSTRAINT_RESULT,  model);
		}
		return super.getModel(model, root, mode);
	}
}
