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

package de.dfki.asr.ajan.pluginsystem.aspplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.LoadingRulesException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.ASPConfig;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.Deserializer;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.Serializer;
import de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies.ASPVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.repository.Repository;
import org.pf4j.Extension;

@Extension
@RDFBean("asp:Problem")
public class Problem extends AbstractTDBLeafTask implements NodeExtension {

	@Getter @Setter
    private ArrayList<String> facts;
	@Getter @Setter
    private String ruleset;
	@Getter @Setter
	private Model stableModels;

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("asp:config")
    @Getter @Setter
    private ASPConfig config;

    @RDF("asp:domain")
    @Getter @Setter
    private BehaviorConstructQuery query;

    @RDF("asp:ruleSets")
    @Getter @Setter
    private List<RuleSetLocation> rules;

    @RDF("asp:write")
    @Getter @Setter
    private ASPWrite write;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/asp-ns#Problem");
	}

    @Override
    public NodeStatus executeLeaf() {
		try {
			generateRuleSet();
			if(!getConfig().runSolver(this)) {
				return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to output UNSATISFIABLE");
			}
			if(getFacts() != null) {
				setStableModels(readStableModels());
				writeSolution(getStableModels());
			}
			return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
		} catch (URISyntaxException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to URISyntaxException", ex);
		} catch (RDFBeanException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to RDFBeanException", ex);
		} catch (LoadingRulesException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to LoadingRulesException", ex);
		}
    }

    protected void generateRuleSet() throws URISyntaxException, RDFBeanException, LoadingRulesException {
            StringBuilder set = new StringBuilder();
            loadBeliefs(set);
            Deserializer.loadRules(this.getObject(), set, getRules());
            setRuleset(set.toString());
    }

    private void loadBeliefs(StringBuilder set) throws URISyntaxException {
		if (!getQuery().getSparql().isEmpty()) {
            Repository origin = BTUtil.getInitializedRepository(getObject(), getQuery().getOriginBase());
            Model model = getQuery().getResult(origin);
            Deserializer.addRuleSet(set, model);
		}
    }

    protected Model readStableModels() {
		Model origin = new LinkedHashModel();
		int number = 0;
		if (!getWrite().getRandom()) {
			for (String stableModel : getFacts()) {
				ModelBuilder builder = getBuilder(number);
				getNamedModel(builder, stableModel);
				Model model = builder.build();
				model.getNamespaces().stream().forEach(origin::setNamespace);
				model.stream().forEach(origin::add);
				number++;
			}
		}
		else {
			ModelBuilder builder = getBuilder(number);
			getNamedModel(builder,getRandomStableModel());
			origin = builder.build();
		}
		return origin;
    }

	private ModelBuilder getBuilder(int number) {
		ModelBuilder builder = new ModelBuilder();
		if (getWrite().getContext() != null) {
			Resource graph = vf.createIRI(getWrite().getContext().toString() + "_" + number);
			builder.add(graph, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, vf.createIRI(getWrite().getContext().toString()));
			builder.namedGraph(graph);
		}
		return builder;
	}

    private void getNamedModel(final ModelBuilder builder, final String stableModel) {
		BNode bnode = vf.createBNode();
		builder.add(bnode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.STABLE_MODEL);
		builder.add(bnode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.RULE_SET);
		if (getWrite().isSaveString()) {
			StringBuilder adaptedModel = new StringBuilder();
			for (String value : Arrays.asList(stableModel.split("\\) "))) {
				if(value.endsWith(")")) {
					adaptedModel.append(value).append(". ");
				}
				else {
					adaptedModel.append(value).append("). ");
				}
			}
			builder.add(bnode, ASPVocabulary.AS_RULES, adaptedModel.toString());
		}
		else {
			List<Resource> list = new ArrayList();
			Serializer.getGraphFromSolution(bnode, builder, list, stableModel);
		}
    }

    private void writeSolution(Model model) {
		if (getWrite().getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			this.getObject().getExecutionBeliefs().update(model);
		} else if (getWrite().getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			this.getObject().getAgentBeliefs().update(model);
		}
    }

    private String getRandomStableModel() {
		return getFacts().get((int) (Math.random() * getFacts().size()));
    }

    @Override
    public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

	@Override
	public String toString() {
		return "ASPProblem (" + getLabel() + ")";
	}

	@Override
	public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return SimulationResult.Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			Resource resource = getInstance(root.getInstance());
			getQuery().setResultModel(resource, BTVocabulary.DOMAIN_RESULT,  model);
			if (getStableModels() != null && !getStableModels().isEmpty()) {
				Resource resultNode = vf.createBNode();
				model.add(resource, BTVocabulary.HAS_STABLE_MODELS, resultNode);
				model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.STABLE_MODELS);
				Resource resultGraph = BTUtil.setGraphResultModel(model, getStableModels());
				model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
			}
		}
		return super.getModel(model, root, mode);
	}
}
