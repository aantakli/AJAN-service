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
package de.dfki.asr.ajan.pluginsystem.mlplugin.extensions;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.exeptions.MLMappingException;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.MLUtil;
import de.dfki.asr.ajan.pluginsystem.mlplugin.vocabularies.MLVocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import smile.association.ARM;
import smile.association.AssociationRule;
import smile.association.FPTree;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:ARM")
public class ARM_Node extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI targetBase;

	@RDF("ml:minSupport")
	@Getter @Setter
	private int minSupport;

	@RDF("ml:confidence")
	@Getter @Setter
	private double confidence;

	@RDF("ml:itemsets")
	@Setter @Getter
	private BehaviorConstructQuery query;

	private IRI type = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
	private final ValueFactory VF = SimpleValueFactory.getInstance();
	protected static final Logger LOG = LoggerFactory.getLogger(ARM_Node.class);
	
	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#ARM");
	}
	
	@Override
	public NodeStatus executeLeaf() {
		try {
			Set<String> itemRegister = new HashSet();
			int[][] itemsets = readItemsets(itemRegister);
			List<AssociationRule> rules = runSolver(itemsets);
			/*int[][] testset = {
				{1, 3},
				{2},
				{4},
				{2, 3, 4},
				{2, 3},
				{2, 3},
				{1, 2, 3, 4},
				{1, 3},
				{1, 2, 3},
				{1, 2, 3}
			};
			List<AssociationRule> test = runSolver(testset);*/
			saveRules(rules, itemRegister);
			String report = toString() + " SUCCEEDED";
			return new NodeStatus(Status.SUCCEEDED, report);
		} catch (URISyntaxException | MLMappingException | ConditionEvaluationException ex) {
			LOG.info(ex.toString());
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, report);
		}
	}

	private int[][] readItemsets(final Set<String> itemRegister) throws URISyntaxException, MLMappingException {
		if (query != null) {
			Repository repo = BTUtil.getInitializedRepository(this.getObject(), query.getOriginBase());
			Model result = query.getResult(repo);
			return getItemsets(result, itemRegister);
		} else {
			throw new MLMappingException("No itemset query defined!");
		}
	}

	private int[][] getItemsets(final Model result, final Set<String> itemRegister) {
		List<Set<String>> list = new ArrayList();
		Model filter = result.filter(null, type, MLVocabulary.ITEMSET);
        Set<Resource> resources = filter.subjects();
		if(!resources.isEmpty()) {
			Iterator<Resource> iter = resources.iterator();
			while(iter.hasNext()) {
				list.add(getItems(result, iter.next(), itemRegister));
			}
		}
		return transformMap(list, itemRegister);
	}

	private Set<String> getItems(final Model result, final Resource subject, Set<String> itemRegister) {
		Set<String> items = new HashSet();
		Model filter = result.filter(subject, MLVocabulary.HAS_ITEM, null);
		filter.stream().map((stmt) -> stmt.getObject()).filter((value) -> (value instanceof IRI)).map((value) -> {
			itemRegister.add(value.stringValue());
			return value;
		}).forEachOrdered((value) -> {
			items.add(value.stringValue());
		});
		return items;
	}

	private int[][] transformMap(final List<Set<String>> list, final Set<String> itemRegister) {
		List<List<Integer>> sets = new ArrayList();
		list.stream().map((entryset) -> {
			List<Integer> entries = new ArrayList();
			int i = 1;
			for (String registerItem : itemRegister) {
				for (String value : entryset) {
					if(registerItem.equals(value)) {
						entries.add(i);
					}
				}
				i++;
			}
			return entries;
		}).forEachOrdered((entries) -> {
			sets.add(entries);
		});
		return sets.stream().map(u -> u.stream().mapToInt(i->i).toArray()).toArray(int[][]::new);
	}

	private List<AssociationRule> runSolver(final int[][] itemsets) {
		FPTree fpTree = FPTree.of(minSupport, itemsets);
        return ARM.apply(confidence, fpTree).collect(Collectors.toList());
	}

	private void saveRules(final List<AssociationRule> rules, final Set<String> itemRegister) throws ConditionEvaluationException, MLMappingException {
		if(targetBase == null) {
			throw new MLMappingException("No target base defined!");
		}
		if (rules.isEmpty()) {
			throw new MLMappingException("No rules found!");
		}
		Model model = new LinkedHashModel();
		Iterator<AssociationRule> iter = rules.iterator();
		while(iter.hasNext()) {
			AssociationRule rule = iter.next();
			Resource subject = VF.createBNode();
			model.add(subject, type, MLVocabulary.ASSOCIATION_RULE);
			model.add(subject, MLVocabulary.HAS_CONFIDENCE, VF.createLiteral(rule.confidence));
			model.add(subject, MLVocabulary.HAS_SUPPORT, VF.createLiteral(rule.support));
			model.add(subject, MLVocabulary.HAS_LIFT, VF.createLiteral(rule.lift));
			addIntArrayStatements(itemRegister, model, rule.antecedent, subject, MLVocabulary.HAS_ANTECENDENT);
			addIntArrayStatements(itemRegister, model, rule.consequent, subject, MLVocabulary.HAS_CONSEQUENT);
		}
		MLUtil.performWrite(this.getObject(), targetBase, model);
	}

	private void addIntArrayStatements(final Set<String> itemRegister, 
			final Model model, 
			final int[] values, 
			final Resource subject, 
			final IRI predicate) {
		for(int i = 0; i < values.length; i++) {
			int j = 0;
			Iterator<String> iter = itemRegister.iterator();
			while(iter.hasNext()) {
				String value = iter.next();
				if(values[i] == j) {
					model.add(subject, predicate, VF.createIRI(value));
					break;
				}
				j++;
			}
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
