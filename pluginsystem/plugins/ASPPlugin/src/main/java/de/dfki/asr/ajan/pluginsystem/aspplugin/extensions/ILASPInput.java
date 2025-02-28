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

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.ClingoException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.util.Serializer;
import de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies.ASPVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies.ILPVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.pf4j.Extension;

@Extension
@RDFBean("ilp:ILASPInput")
public class ILASPInput extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;
	
	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ilp:posExamples")
	@Setter @Getter
	private BehaviorSelectQuery pos;

	@RDF("ilp:negExamples")
	@Setter @Getter
	private BehaviorSelectQuery neg;

	@RDF("ilp:hypothesisSpace")
    @Getter @Setter
    private BehaviorSelectQuery hypSpace;
	
    @RDF("ilp:domain")
    @Getter @Setter
    private BehaviorSelectQuery domain;

    @RDF("asp:ruleSets")
    @Getter @Setter
    private List<RuleSetLocation> rules;

	@RDF("asp:write")
    @Getter @Setter
    private ASPWrite write;
	
	@Getter @Setter
    private String hypothesis;
	private final String cmd = "wsl ILASP --version=4 --quiet ";

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/asp-ns#ILASPInput");
	}
    @Override
    public NodeStatus executeLeaf() {
		try {
			try {
				ClassLoader classLoader = getClass().getClassLoader();
				URL path = classLoader.getResource("ilasp/training.las");
				String filePath = path.getFile();
				File training = new File(filePath);
				try (FileWriter myWriter = new FileWriter(training,false)) {
					getILASPTrainingData(myWriter);
				}
				executeSolver(filePath.replaceFirst("/C:", "/mnt/c"));
				Model result = readStableModels();
				writeSolution(result);
				System.out.println(hypothesis);
				return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
			  } catch (ClingoException ex) {
				Logger.getLogger(ILASPInput.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (URISyntaxException | IOException ex) {
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to query evaluation error", ex);
		}
		return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED");
    }

	private void getILASPTrainingData(final FileWriter myWriter) throws URISyntaxException, IOException {
		getDomainData(myWriter);
		getHypothesisData(myWriter);
		getPositiveData(myWriter);
		getNegativeData(myWriter);
	}
	
	private void getDomainData(final FileWriter myWriter) throws URISyntaxException, IOException {
		getTrainingData("domain", domain, myWriter);
	}

	private void getHypothesisData(final FileWriter myWriter) throws URISyntaxException, IOException {
		getTrainingData("hypothesisSpace", hypSpace, myWriter);
	}
	
	private void getPositiveData(final FileWriter myWriter) throws URISyntaxException, IOException {
		getTrainingData("pos", pos, myWriter);
	}

	private void getNegativeData(final FileWriter myWriter) throws URISyntaxException, IOException {
		getTrainingData("neg", neg, myWriter);
	}

	private void getTrainingData(final String label, final BehaviorSelectQuery query, final FileWriter myWriter) throws URISyntaxException, IOException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		List<BindingSet> bindings = query.getResult(repo);
		for(BindingSet set: bindings) {
			if(set.hasBinding(label)) {
				myWriter.write(set.getBinding(label).getValue().stringValue());
			}
		}
	}
	
	public void executeSolver(final String filePath) throws ClingoException {
		StringBuilder result = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec(cmd + filePath);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ( (line = in.readLine()) != null) {
					System.out.println(line);
					result.append(line);
				}
//			} finally {
				p.waitFor();
				p.descendants().forEach(ph -> {
					ph.destroy();
				});
				p.destroy();
				hypothesis = result.toString();
			}
		} catch (IOException | InterruptedException  ex) {
			throw new ClingoException("Problems with the Runtime environment!", ex);
		}
	}
	
	protected Model readStableModels() {
		ModelBuilder builder = getBuilder();
		getNamedModel(builder, hypothesis);
		return builder.build();
    }
	
	private ModelBuilder getBuilder() {
		ModelBuilder builder = new ModelBuilder();
		if (getWrite().getContext() != null) {
			Resource graph = vf.createIRI(getWrite().getContext().toString());
			builder.add(graph, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, vf.createIRI(getWrite().getContext().toString()));
			builder.namedGraph(graph);
		}
		return builder;
	}

	 private void getNamedModel(final ModelBuilder builder, final String hypothesis) {
		BNode bnode = vf.createBNode();
		builder.add(bnode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ILPVocabulary.HYPOTHESIS);
		builder.add(bnode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.RULE_SET);
		if (getWrite().isSaveString()) {
			StringBuilder adaptedModel = new StringBuilder();
			for (String value : Arrays.asList(hypothesis.split("\\) "))) {
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
			Serializer.getGraphFromSolution(bnode, builder, list, hypothesis);
		}
    }
	
	private void writeSolution(Model model) {
		if (getWrite().getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			this.getObject().getExecutionBeliefs().update(model);
		} else if (getWrite().getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			this.getObject().getAgentBeliefs().update(model);
		}
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

	@Override
	public String toString() {
		return "ILASPInput (" + getLabel() + ")";
	}

	@Override
	public SimulationResult.Result simulateNodeLogic(SimulationResult result, Resource root) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
}
