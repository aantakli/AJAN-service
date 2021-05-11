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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:SendHTLELogs")
public class SendHTLELogs extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:endpointHTLE")
	@Getter @Setter
	private URI endpoint;

	@RDF("bt:originBase")
	@Getter @Setter
	private URI repository;

	private ObjectMapper objectMapper = new ObjectMapper();
	protected static final Logger LOG = LoggerFactory.getLogger(SendHTLELogs.class);

	String MMU_LOGS = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"SELECT ?id ?timestamp ?object\n" +
			"WHERE {\n" +
			"	?instruction rdf:type mosim:Instruction .\n" +
			"	?instruction mosim:taskId ?id .\n" +
			"	?instruction mosim:timestamp ?timestamp .\n" +
			"	?instruction mosim:jsonInstruction ?object .\n" +
			"}";

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#SendHTLELogs");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			if (readInput()) {
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
			}
			String report = toString() + " FAILED";
			LOG.info(report);
			return new LeafStatus(Status.FAILED, report);
		} catch (IOException ex) {
			String report = toString() + " FAILED";
			LOG.info(report);
			return new LeafStatus(Status.FAILED, report);
		}
	}

	protected boolean readInput() throws IOException {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("token", "DGnHku9w8OirCFz9J-wrUW7uK9Hdf");
		root.put("action", "saveMMUTask");
		ArrayNode array = root.putArray("data");
		if (!readExternalRepo(array)) {
			return false;
		}
		return sendMessage(root);
	}

	private boolean readExternalRepo(final ArrayNode array) throws IOException {
		boolean bool = false;
		Repository repo = new SPARQLRepository(repository.toString());
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.begin(IsolationLevels.SERIALIZABLE);
			TupleQuery query = conn.prepareTupleQuery(MMU_LOGS);
			TupleQueryResult result = query.evaluate();
			while(result.hasNext()) {
				bool = true;
				BindingSet set = result.next();
				ObjectNode object = array.addObject();
				object.put("taskId", set.getValue("id").stringValue());
				object.put("timestamp", set.getValue("timestamp").stringValue());
				JsonNode childNode = objectMapper.readTree(set.getValue("object").stringValue());
				object.set("instruction", childNode);
			}
			conn.close();
		}
		return bool;
	}

	private boolean sendMessage(final ObjectNode root) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(endpoint.toString());
			httpPost.setEntity(new StringEntity(root.toString()));
			CloseableHttpResponse response = client.execute(httpPost);
			return response.getStatusLine().getStatusCode() >= 200;
		}
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "SendHTLELogs (" + getLabel() + ")";
	}
	
	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}