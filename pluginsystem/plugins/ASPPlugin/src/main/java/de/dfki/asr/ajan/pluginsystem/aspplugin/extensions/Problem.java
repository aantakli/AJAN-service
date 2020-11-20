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
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.ASPConfig;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.Deserializer;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.Serializer;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.Repository;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("asp:Problem")
public class Problem extends AbstractTDBLeafTask implements NodeExtension {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Problem.class);
    private ArrayList<String> facts;
    private String ruleset;
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
    private List<URI> rules;

    @RDF("asp:write")
    @Getter @Setter
    private ASPWrite write;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@Override
	public Resource getType() {
		ValueFactory vf = SimpleValueFactory.getInstance();
		return vf.createIRI("http://www.ajan.de/behavior/asp-ns##Problem");
	}

    public void setFacts(ArrayList<String> set) {
            facts = set;
    }

    public String getRuleset() {
            return ruleset;
    }

    @Override
    public LeafStatus executeLeaf() {
            try {
				generateRuleSet();
				if(!config.runSolver(this)) {
					LOG.info(toString() + " UNSATISFIABLE");
					return new LeafStatus(Status.FAILED, toString() + " UNSATISFIABLE");
				}
				if(facts != null)
					writeStableModels();
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
            } catch (URISyntaxException | RDFBeanException ex) {
				LOG.info(toString() + " FAILED due to query evaluation error", ex);
				return new LeafStatus(Status.FAILED, toString() + " FAILED");
            }
    }

    private void generateRuleSet() throws URISyntaxException, RDFBeanException {
            StringBuilder set = new StringBuilder();
            loadBeliefs(set);
            Deserializer.loadRules(this.getObject(), set, rules);
            ruleset = set.toString();
            LOG.info("Input RuleSet: " + ruleset);
    }

    private void loadBeliefs(StringBuilder set) throws URISyntaxException {
            Repository origin = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
            Model model = query.getResult(origin);
            Deserializer.addRuleSet(set, model);
    }

    private void writeStableModels() {
		stableModels = getStableModels();
        writeSolution(stableModels);
    }

    private Model getStableModels() {
            Model origin = new LinkedHashModel();
            int number = 0;
            if (!write.getRandom()) {
                    for (String stableModel : facts) {
                            Model model = getNamedModel(number,stableModel);
                            model.getNamespaces().stream().forEach(origin::setNamespace);
                            model.stream().forEach(origin::add);
                            number++;
                    }
            }
            else
                    origin = getNamedModel(number,getRandomStableModel());
            return origin;
    }

    private Model getNamedModel(int number, String stableModel) {
            ModelBuilder builder = new ModelBuilder();
            if (write.getContext() != null)
                    builder.namedGraph(write.getContext().toString() + number);
            Serializer.getGraphFromSolution(builder, stableModel);
            return builder.build();
    }

    private void writeSolution(Model model) {
            if (write.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
                    this.getObject().getExecutionBeliefs().update(model);
            } else if (write.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
                    this.getObject().getAgentBeliefs().update(model);
            }
    }

    private String getRandomStableModel() {
            return facts.get((int) (Math.random() * facts.size()));
    }

    @Override
    public void end() {
            LOG.info("ASPProblem (" + getStatus() + ")");
    }

	@Override
	public String toString() {
		return "ASPProblem (" + label + ")";
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			Resource resource = getInstance(root.getInstance());
			query.setResultModel(resource, BTVocabulary.DOMAIN_RESULT,  model);
			if (stableModels != null && !stableModels.isEmpty()) {
				Resource resultNode = vf.createBNode();
				model.add(resource, BTVocabulary.HAS_STABLE_MODELS, resultNode);
				model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.STABLE_MODELS);
				Resource resultGraph = BTUtil.setGraphResultModel(model, stableModels);
				model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
			}
		}
		return super.getModel(model, root, mode);
	}
}
