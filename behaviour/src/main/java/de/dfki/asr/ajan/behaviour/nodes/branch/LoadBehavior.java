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

package de.dfki.asr.ajan.behaviour.nodes.branch;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.LoadBehaviorException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.LoggerFactory;

@RDFBean("bt:LoadBehavior")
public class LoadBehavior extends AbstractTDBBranchTask {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoadBehavior.class);

	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:resetBehavior")
	@Setter @Getter
	private Boolean resetBehavior;

	@RDF("bt:behaviorUri")
	@Setter @Getter
	private BehaviorSelectQuery behaviorURI;

	@Override
	public Resource getType() {
		return BTVocabulary.LOAD_BEHAVIOR;
	}

	@Override
	public String toString() {
		return "LoadBehavior (" + url + ")";
	}

	@Override
	public void run () {
		if (runningChild != null) {
			runningChild.run();
			return;
		}
		if (resetBehavior) {
			children.clear();
		}
		if (initBehavior()) {
			runChild(0);
		}
	}

	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}

	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	private boolean initBehavior() {
		if (children.size == 0) {
			try {
				BTRoot behavior = generateBehavior();
				if (behavior.getChildCount() > 0) {
					addChild(behavior);
					LOG.info(toString() + " SUCCEEDED");
					LOG.info("Status (SUCCEEDED)");
					success();
					return true;
				}
			} catch (LoadBehaviorException | URISyntaxException ex) {
				LOG.error(toString() + " FAILED due to evaluation error", ex);
				LOG.info("Status (FAILED)");
				fail();
				return false;
			}
		}
		return true;
	}

	private BTRoot generateBehavior() throws LoadBehaviorException, URISyntaxException {
		BTRoot behavior;
		Repository repo = BTUtil.getInitializedRepository(getObject(), behaviorURI.getOriginBase());
		Resource btResource = getBehaviorResource(repo);
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn, this.getObject().getExtensions());
			behavior = manager.get(btResource, BTRoot.class);
		} catch (RDF4JException | RDFBeanException ex) {
			throw new LoadBehaviorException("Could not load BehaviorTree", ex);
		}
		return behavior;
	}

	private Resource getBehaviorResource(final Repository repo) throws URISyntaxException, LoadBehaviorException {
		List<BindingSet> result = behaviorURI.getResult(repo);
		if (result.isEmpty()) {
			throw new LoadBehaviorException("No ?behaviorURI defined");
		}
		BindingSet bindings = result.get(0);
		Value strValue = bindings.getValue("behaviorURI");
		if (strValue == null) {
			throw new LoadBehaviorException("No ?behaviorURI defined");
		} else {
			return vf.createIRI(strValue.stringValue());
		}
	}

	@Override
	public void evaluate(final EvaluationResult result) {
		EvaluationResult.Direction direction = result.getDirection();
		if (direction.equals(EvaluationResult.Direction.Down)) {
			BTRoot behavior;
			try {
				behavior = generateBehavior();
				if (behavior.getChildCount() > 0) {
					behavior.evaluate(result.setDirection(EvaluationResult.Direction.Down));
				} else {
					result.setChildResult(EvaluationResult.Result.FAIL);
				}
			} catch (LoadBehaviorException | URISyntaxException ex) {
				LOG.error(toString() + " FAILED due to evaluation error", ex);
				result.setChildResult(EvaluationResult.Result.FAIL);
			}
		} else {
			((TreeNode)control).evaluate(result.setDirection(EvaluationResult.Direction.Up));
		}
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			behaviorURI.setResultModel(getInstance(root.getInstance()), BTVocabulary.BEHAVIOR_URI_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}
}
