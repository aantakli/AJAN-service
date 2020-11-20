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

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
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

@RDFBean("bt:Update")
public class Update extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:query")
	@Getter @Setter
	private BehaviorQuery query;
	private static final Logger LOG = LoggerFactory.getLogger(Update.class);

	@Override
	public Resource getType() {
		return BTVocabulary.UPDATE;
	}

	@Override
	public void resetTask() {
		query.setReset(true);
		super.resetTask();
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			if (performUpdateLogic(repo)) {
				LOG.info(toString() + " SUCCEEDED");
				return new LeafStatus(Status.SUCCEEDED, toString() + " SUCCEEDED");
			}
			LOG.info(toString() + " FAILED");
			return new LeafStatus(Status.FAILED, toString() + " FAILED");
		} catch (QueryEvaluationException | URISyntaxException ex) {
			LOG.info(toString() + " FAILED due to evaluation error", ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	private Boolean performUpdateLogic(final Repository repo) {
		Boolean result;
		try {
			Object obj = query.getResult(repo);
			if (obj instanceof Boolean) {
				result = (Boolean)obj;
			} else {
				return false;
			}
		} catch (QueryEvaluationException ex) {
			return false;
		}
		return result;
	}

	@Override
	public String toString() {
		return "Update (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		if (performUpdateLogic(result.getRepo().initialize())) {
			return Result.SUCCESS;
		} else {
			return Result.FAIL;
		}
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			query.setResultModel(getInstance(root.getInstance()), BTVocabulary.UPDATE_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}
}
