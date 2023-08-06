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

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.ExcessiveParameterList","PMD.ExcessiveImports"})
public final class BTUtil {

	public enum ModelMode {
		NORMAL,
		DETAIL
	}

	public enum DebugMode {
		NONE,
		PAUSE,
		RESUME,
		STEP
	}

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final Logger LOG = LoggerFactory.getLogger(BTRoot.class);

	private BTUtil() {

	}

	public static Resource getInstanceResource(final String url, final Resource btInstance) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		if (url != null) {
			try {
				URI uri = new URI(url);
				return vf.createIRI(btInstance.stringValue() + "#" + uri.getFragment());
			} catch (URISyntaxException ex) {
				return vf.createBNode();
			}
		}
		return vf.createBNode();
	}

	public static void setGeneralNodeModel(final Model model, final BTRoot btRoot, final ModelMode mode, final Task task) {
		TreeNode node = (TreeNode)task;
		Resource instance = node.getInstance(btRoot.getInstance());
		model.add(instance, RDF.TYPE, node.getType());
		model.add(instance, RDFS.ISDEFINEDBY, node.getDefinition(btRoot.getDefinition()));
		String state = task.getStatus().toString();
		if (task instanceof AbstractTDBLeafTask && "FRESH".equals(state)) {
			state = ((AbstractTDBLeafTask)task).getLastStatus().toString();
		}
		model.add(instance, BTVocabulary.HAS_STATE, VF.createLiteral(state));
	}

	public static void setDecoratorNodeModel(final Model model, final BTRoot btRoot, final ModelMode mode, final Decorator task) {
		TreeNode node = (TreeNode)task;
		if (task.getChild(0) != null) {
			TreeNode treechild = (TreeNode)task.getChild(0);
			model.add(node.getInstance(btRoot.getInstance()), BTVocabulary.HAS_CHILD, treechild.getInstance(btRoot.getInstance()));
			treechild.getModel(model, btRoot, mode);
		}
	}

	public static void setBranchNodeModel(final Model model, final BTRoot btRoot, final ModelMode mode, final BranchTask task) {
		TreeNode node = (TreeNode)task;
		Resource head = VF.createBNode();
		model.add(node.getInstance(btRoot.getInstance()), BTVocabulary.HAS_CHILDREN, head);
		List<Resource> list = new ArrayList();
		for (int i = 0; i < task.getChildCount(); i++) {
			TreeNode chNode = (TreeNode) task.getChild(i);
			Resource instance = chNode.getInstance(btRoot.getInstance());
			if (instance == null) {
				instance = BTUtil.getInstanceResource(chNode.getUrl(), btRoot.getInstance());
			}
			list.add(instance);
			chNode.getModel(model, btRoot, mode);
		}
		RDFCollections.asRDF(list, head, model);
	}

	public static Resource setQueryResultModel(final Resource resource, final Model model) {
		Resource resultNode = VF.createBNode();
		model.add(resource, BTVocabulary.HAS_QUERY_RESULT, resultNode);
		model.add(resultNode, RDF.TYPE, BTVocabulary.QUERY_RESULT);
		return resultNode;
	}

	public static void setQueryBindingModel(final List<BindingSet> result, final Resource resource, final Model model) {
		if (!result.isEmpty()) {
			for (BindingSet set: result) {
				Iterator<Binding> itr = set.iterator();
				Resource bindingNode = VF.createBNode();
				model.add(resource, BTVocabulary.HAS_QUERY_BINDING, bindingNode);
				while (itr.hasNext()) {
					Binding bind = itr.next();
					model.add(bindingNode, RDF.TYPE, BTVocabulary.QUERY_BINDING);
					model.add(bindingNode, BTVocabulary.HAS_NAME, VF.createLiteral(bind.getName()));
					model.add(bindingNode, BTVocabulary.HAS_VALUE, VF.createLiteral(bind.getValue().stringValue()));
				}
			}
		}
	}

	public static Resource setGraphResultModel(final Model model, final Model result) {
		Resource resultGraph = VF.createBNode();
		model.add(resultGraph, RDF.TYPE, BTVocabulary.GRAPH);
		AgentUtil.mergeModels(model, result, resultGraph);
		return resultGraph;
	}

	public static Repository getInitializedRepository(final AgentTaskInformation metadata, final URI url) throws URISyntaxException, QueryEvaluationException {
		Repository repo;
		if (url.equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
			repo = metadata.getAgentBeliefs().initialize();
		} else if (url.equals(new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString()))) {
			repo = metadata.getExecutionBeliefs().initialize();
		} else if (url.equals(new URI(AJANVocabulary.BEHAVIOR_KNOWLEDGE.toString()))) {
			repo = metadata.getBehaviorTDB().getInitializedRepository();
		} else if (url.equals(new URI(AJANVocabulary.SERVICE_KNOWLEDGE.toString()))) {
			repo = metadata.getServiceTDB().getInitializedRepository();
		} else if (url.equals(new URI(AJANVocabulary.DOMAIN_KNOWLEDGE.toString()))) {
			repo = metadata.getDomainTDB().getInitializedRepository();
		} else {
			// -----------------------------------------------------------------
			// TODO: General Repository desription for SPARQL + Update Endpoints
			// -----------------------------------------------------------------
			repo = new SPARQLRepository(url.toString(), url.toString());
			repo.init();
		}
		return repo;
	}

	public static Resource setBasicEvaluationResult(final Model stmts, final String url) {
		Resource root = VF.createBNode();
		stmts.add(root, RDF.TYPE, BTVocabulary.EVALUATION_RESULT);
		stmts.add(root, BTVocabulary.BT_NODE, VF.createIRI(url));
		return root;
	}

	public static void reportState(final String url, final Model debugModel, final AgentTaskInformation info, final NodeStatus leafStatus) {
		if (!info.getReportURI().equals("")) {
			Debug debug = info.getDebug();
			BTRoot bt = info.getBt();
			Model detail = new LinkedHashModel();
			if (debug.isDebugging()) {
				detail = debugModel;
			}
			String report = BTUtil.createReport(url, bt, leafStatus, debug, detail);
			BTUtil.sendReport(info, report);
		}
	}

	public static void sendReport(final AgentTaskInformation info, final String report) {
		if (info != null && !info.getReportURI().equals("")) {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(info.getReportURI());
			HttpEntity postParams = new StringEntity(report, ContentType.DEFAULT_TEXT);
			httpPost.setEntity(postParams);
			CloseableHttpResponse httpResponse;
			try {
				httpResponse = httpClient.execute(httpPost);
				LOG.info("POST Response Status: " + httpResponse.getStatusLine().getStatusCode());
			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

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
