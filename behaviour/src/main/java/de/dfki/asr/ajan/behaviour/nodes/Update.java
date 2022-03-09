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
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cyberborean.rdfbeans.annotations.*;
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

	@RDF("bt:useW3C")
	@Getter @Setter
	private boolean useW3C;

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
	public NodeStatus executeLeaf() {
		try {
			Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
			if (performUpdateLogic(repo)) {
				LOG.info(toString() + " SUCCEEDED");
				return new NodeStatus(Status.SUCCEEDED, toString() + " SUCCEEDED");
			}
			LOG.info(toString() + " FAILED");
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		} catch (QueryEvaluationException | URISyntaxException ex) {
			LOG.info(toString() + " FAILED due to evaluation error", ex);
			return new NodeStatus(Status.FAILED, toString() + " FAILED");
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	private Boolean performUpdateLogic(final Repository repo) {
		if (useW3C) {
			return senW3CQueryMsg();
		}
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

	private Boolean senW3CQueryMsg() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(query.getOriginBase());
		HttpEntity postParams = new StringEntity(query.getSparql(), ContentType.create("application/sparql-update"));
		httpPost.setEntity(postParams);
		CloseableHttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(httpPost);
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() >= 300) {
				LOG.info("POST Response Status: " + httpResponse.getStatusLine().getStatusCode());
				return false;
			}
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
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
