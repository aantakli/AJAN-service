/*
 * Copyright (C) 2021 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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
package de.dfki.asr.ajan.pluginsystem.mlplugin.extensions.ongoing;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.vocabularies.MLVocabulary;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.doccat.NGramFeatureGenerator;
import opennlp.tools.ngram.NGramModel;
import opennlp.tools.ngram.NGramUtils;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.StringList;
import opennlp.tools.util.TrainingParameters;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:NGram")
public class NGram_Node extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ml:itemsets")
	@Setter @Getter
	private BehaviorConstructQuery query;

	@RDF("ml:input")
	@Getter @Setter
	private String input;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("ml:bigram")
	@Setter @Getter
	private BehaviorConstructQuery bigram;

	@RDF("ml:trigram")
	@Setter @Getter
	private BehaviorConstructQuery trigram;

	@RDF("ml:sentence")
	@Setter @Getter
	private BehaviorConstructQuery sentence;

	private IRI type = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
	private final ValueFactory VF = SimpleValueFactory.getInstance();
	protected static final Logger LOG = LoggerFactory.getLogger(NGram_Node.class);
	
	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#NGram");
	}
	
	@Override
	public NodeStatus executeLeaf() {
		try {
			Collection<StringList> set = readItemsets();
			String report = toString() + " SUCCEEDED";
			return new NodeStatus(Status.SUCCEEDED, report);
		} catch (URISyntaxException | MLMappingException ex) {
			LOG.info(ex.toString());
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, report);
		}
	}

	private Collection<StringList> readItemsets() throws URISyntaxException, MLMappingException {
		Collection<StringList> set = new LinkedList<>();
		if (query != null) {
			Repository repo = BTUtil.getInitializedRepository(this.getObject(), query.getOriginBase());
			Model result = query.getResult(repo);
			return getItemsets(result, set);
		} else {
			throw new MLMappingException("No itemset query defined!");
		}

	}

	private Collection<StringList> getItemsets(final Model result, final Collection<StringList> set) {
		List<Set<String>> list = new ArrayList();
		Model filter = result.filter(null, type, MLVocabulary.ORDERED_ITEMSET);
        Set<Resource> resources = filter.subjects();
		if(!resources.isEmpty()) {
			Iterator<Resource> iter = resources.iterator();
			while(iter.hasNext()) {
				//list.add(getItems(result, iter.next(), itemRegister));
			}
		}
		//return transformMap(list, itemRegister);
		return null;
	}

	private void runTest() {
		StringList l1 = new StringList("<s>", "walk", "walk", "rest", "eat", "walk", "</s>");
		StringList l2 = new StringList("<s>", "eat", "jump", "walk", "rest", "eat", "</s>");
		StringList l3 = new StringList("<s>", "eat", "eat", "walk", "rest", "sleep", "walk", "rest", "jump", "walk", "walk", "</s>");
		StringList l4 = new StringList("<s>", "eat", "eat", "walk", "sleep", "sleep", "walk", "sleep", "jump", "walk", "walk", "</s>");
		StringList l5 = new StringList("<s>", "eat", "walk", "jump", "walk", "</s>");
		StringList l6 = new StringList("");

		Collection<StringList> set = new LinkedList<>();
		set.add(l1);
		set.add(l2);
		set.add(l3);
		set.add(l4);
		set.add(l5);
		set.add(l6);

		NGramModel nGramModel = new NGramModel();
        nGramModel.add(l1, 2, 2);
        nGramModel.add(l2, 2, 2);
        nGramModel.add(l3, 2, 2);
        nGramModel.add(l4, 2, 2);
        nGramModel.add(l5, 2, 2);
        nGramModel.add(l6, 2, 2);

        System.out.println("Total ngrams: " + nGramModel.numberOfGrams());
		for (StringList ngram: nGramModel) {
			System.out.println(nGramModel.getCount(ngram) + " - " + ngram);
		}

		Map<String, Double> map = getBigrams(set, nGramModel, "eat");
		System.out.println("Best follower: " + map.entrySet().stream().findFirst());
	}

	private Map<String, Double> getBigrams(final Collection<StringList> set, final NGramModel model, final String start) {
		Map<String, Double> map = new LinkedHashMap();
		for (StringList ngram: model) {
			if (ngram.getToken(0).equals(start)) {
				Double prob = NGramUtils.calculateBigramMLProbability(start, ngram.getToken(1), set);
				System.out.println(ngram.getToken(1) + " -> " + prob);
				map.put(ngram.getToken(1), prob);
			}
		}

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap();
		map.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		return sortedMap;
	}

	private void runTest3() {
		try {
            // read the training data
            InputStreamFactory dataIn = new MarkableFileInputStreamFactory(new File("C:\\Users\\anan02-admin\\Documents\\Playground\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\ngram.train"));
            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);
 
            // define the training parameters
            TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ITERATIONS_PARAM, 10+"");
            params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
             
            // feature generators - N-gram feature generators
            FeatureGenerator[] featureGenerators = { 
				new NGramFeatureGenerator(1,1),
				new NGramFeatureGenerator(2,3) 
			};
            DoccatFactory factory = new DoccatFactory(featureGenerators);
 
            // create a model from traning data
            DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, factory);
            System.out.println("\nModel is successfully trained.");
 
            // save the model to local
            /*BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream("model"+File.separator+"en-movie-classifier-maxent.bin"));
            model.serialize(modelOut);
            System.out.println("\nTrained Model is saved locally at : "+"model"+File.separator+"en-movie-classifier-maxent.bin");*/
 
            // test the model file by subjecting it to prediction
            DocumentCategorizer doccat = new DocumentCategorizerME(model);
            //String[] docWords = "Afterwards Stuart and Charlie notice Kate in the photos Stuart took at Leopolds ball and realise that her destiny must be to go back and be with Leopold That night while Kate is accepting her promotion at a company banquet he and Charlie race to meet her and show her the pictures Kate initially rejects their overtures and goes on to give her acceptance speech but it is there that she sees Stuarts picture and realises that she truly wants to be with Leopold".replaceAll("[^A-Za-z]", " ").split(" ");
            String[] docWords = "estava em uma marcenaria na Rua Bruno".replaceAll("[^A-Za-z]", " ").split(" ");
			double[] aProbs = doccat.categorize(docWords);
 
            // print the probabilities of the categories
            System.out.println("\n---------------------------------\nCategory : Probability\n---------------------------------");
            for(int i=0;i<doccat.getNumberOfCategories();i++){
                System.out.println(doccat.getCategory(i)+" : "+aProbs[i]);
            }
            System.out.println("---------------------------------");
 
            System.out.println("\n"+doccat.getBestCategory(aProbs)+" : is the predicted category for the given sentence.");
        } catch (IOException e) {
            System.out.println("An exception in reading the training file. Please check.");
            e.printStackTrace();
        }
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}
}
