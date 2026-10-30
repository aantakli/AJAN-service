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
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and sends behavior tree state reports (backs the report methods of {@link BTUtil}).
 */
final class BTReporter {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	// log category used by BTUtil
	private static final Logger LOG = LoggerFactory.getLogger(BTRoot.class);

	private BTReporter() {

	}

	public static void sendReport(final AgentTaskInformation info, final String report) {
		if (info != null && !info.getReportURI().isEmpty()) {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(info.getReportURI());
			HttpEntity postParams = new StringEntity(report, ContentType.DEFAULT_TEXT);
			httpPost.setEntity(postParams);
			CloseableHttpResponse httpResponse;
			try {
				httpResponse = httpClient.execute(httpPost);
				if (LOG.isInfoEnabled()) {
					LOG.info("POST Response Status: {}", httpResponse.getStatusLine().getStatusCode());
				}
			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@SuppressWarnings("PMD.ExcessiveParameterList")
	public static String createReport(final String url, final BTRoot bt, final NodeStatus state, final Debug debug, final Model detail) {
		Model reportModel = new LinkedHashModel();
		Resource report = VF.createIRI(debug.getAgentURI() + "/report/" + UUID.randomUUID());
		reportModel.add(report, RDF.TYPE, BTVocabulary.REPORT);
		reportModel.add(report, AJANVocabulary.HAS_AGENT, VF.createIRI(debug.getAgentURI()));
		reportModel.add(report, AJANVocabulary.BEHAVIOR_HAS_BT, VF.createIRI(bt.getInstance().stringValue()));
		reportModel.add(report, BTVocabulary.HAS_DEFINITION, VF.createIRI(bt.getDefinition().stringValue()));
		reportModel.add(report, BTVocabulary.BT_NODE, VF.createIRI(url));
		if (state.getStatus() != null) {
			reportModel.add(report, BTVocabulary.HAS_STATE, VF.createLiteral(state.getStatus().name()));
		}
		reportModel.add(report, RDFS.LABEL, VF.createLiteral(state.getLabel()));
		if (debug.isDebugging()) {
			reportModel.add(report, BTVocabulary.HAS_DEBUGGING, VF.createLiteral(true));
			reportModel = AgentUtil.mergeModels(reportModel, detail, null);
		}
		try {
			return ACTNUtil.getModelPayload(reportModel, "application/ld+json");
		} catch (UnsupportedEncodingException ex) {
			return "";
		}
	}
}
