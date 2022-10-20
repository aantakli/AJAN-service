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
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

@RDFBean("bt:Write")
public class Write extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:query")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@Override
	public Resource getType() {
		return BTVocabulary.WRITE;
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			performWrite();
			String report = toString() + " SUCCEEDED";
			return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
		} catch (ConditionEvaluationException ex) {
			String report = toString() + " FAILED due to evaluation error";
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
		}
	}

	private void performWrite() throws ConditionEvaluationException {
		try {
			if (query.getTargetBase().toString().equals(AJANVocabulary.DOMAIN_KNOWLEDGE.toString())
							|| query.getTargetBase().toString().equals(AJANVocabulary.SERVICE_KNOWLEDGE.toString())
							|| query.getTargetBase().toString().equals(AJANVocabulary.BEHAVIOR_KNOWLEDGE.toString())) {
				return;
			}
			Model model = getInputModel();
			if (query.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
				this.getObject().getExecutionBeliefs().update(model);
			} else if (query.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
				this.getObject().getAgentBeliefs().update(model);
			} else {
				updateExternalRepo(new SPARQLRepository(query.getTargetBase().toString()), model);
			}
		} catch (URISyntaxException | QueryEvaluationException ex) {
			throw new ConditionEvaluationException(ex);
		}
	}

	private void updateExternalRepo(final Repository repo, final Model model) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.begin(IsolationLevels.SERIALIZABLE);
			removeStatementsSupersededBy(model, conn, false);
			addStatementsWith(model,conn);
			conn.commit();
		}
	}

	@SuppressWarnings("PMD.UselessParentheses")
	private void removeStatementsSupersededBy(final Model model, final RepositoryConnection connection, final boolean mode) {
		model.stream().filter((stmt) -> (!(stmt.getSubject() instanceof BNode) || (stmt.getSubject() instanceof BNode) && mode)).forEachOrdered((stmt) -> {
			connection.remove(stmt.getSubject(), stmt.getPredicate(), null, (Resource) stmt.getContext());
		});
	}

	private void addStatementsWith(final Model model, final RepositoryConnection connection) {
		model.forEach((stmt) -> {
			connection.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), (Resource) stmt.getContext());
		});
	}

	private Model getInputModel() throws URISyntaxException {
		Repository origin = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		return query.getResult(origin);
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			query.setResultModel(getInstance(root.getInstance()), BTVocabulary.WRITE_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}

	@Override
	public String toString() {
		return "Write (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return Result.SUCCESS;
	}
}
