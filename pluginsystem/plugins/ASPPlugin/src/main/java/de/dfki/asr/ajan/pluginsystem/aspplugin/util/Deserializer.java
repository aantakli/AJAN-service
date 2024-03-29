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

package de.dfki.asr.ajan.pluginsystem.aspplugin.util;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.LoadingRulesException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.ASPRules;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.RuleSetLocation;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Atom;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Body;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Fact;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Rule;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Term;
import de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies.ASPVocabulary;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public final class Deserializer {

	private Deserializer() {

	}

	public static void addRuleSet(StringBuilder set, Model model) {
		model.stream().forEach((statement) -> {
			if (statement.getPredicate().equals(ASPVocabulary.AS_RULES)) {
				if (statement.getObject().isLiteral()) {
					set.append(statement.getObject().stringValue());
				}
			} else {
				if (statement.getSubject() instanceof BNode) {
					set.append("_t(_b(\"").
						append(statement.getSubject().stringValue()).
						append("\")");
				} else {
					set.append("_t(\"").
						append(statement.getSubject().stringValue()).
						append("\"");
				}
				set.append(",\"").append(statement.getPredicate().stringValue());
				if(statement.getObject() instanceof Literal) {
					Literal literal = (Literal) statement.getObject();
					set.append("\",_l(\"").append(statement.getObject().stringValue()).
						append("\",\"").append(literal.getDatatype()).
						append("\")).");
				} else if (statement.getObject() instanceof BNode) {
					set.append("\",_b(\"").
						append(statement.getObject().stringValue()).
						append("\")).");
				} else {
					set.append("\",\"").append(statement.getObject().stringValue()).
					append("\").");
				}
			}
		});
	}

	public static void loadRules(AgentTaskInformation taskInfo, StringBuilder set, List<RuleSetLocation> ruleSets) throws URISyntaxException, RDFBeanException, LoadingRulesException {
		if (ruleSets != null && ruleSets instanceof List && ruleSets.get(0) instanceof RuleSetLocation) {
			for (RuleSetLocation ruleSet : ruleSets) {
				ASPRules rules = loadRule(taskInfo, ruleSet);
				if (rules == null) {
					throw new LoadingRulesException("Rules are not loadable!");
				}
				if (rules.getFacts() != null) {
					set.append(getStringFacts(rules.getFacts()));
				}
				if (rules.getRules() != null) {
					set.append(getStringRules(rules.getRules()));
				}
				/*if (rules.getConstraints() != null) {
					set.append(rewritePrefixes(rules.getStringRules()));
				}*/
				if (rules.getStringRules() != null) {
					set.append(rewritePrefixes(rules.getStringRules()));
				}
			}
		}
	}

	private static String getStringFacts(final List<Fact> facts) {
		StringBuilder builder = new StringBuilder();
		for (Fact fact: facts) {
			if (fact.isOpposite()) {
				builder.append("-");
			}
			builder.append(fact.getPredicate());
			getTermsString(builder, fact.getTerms());
			builder.append(". ");
		}
		return builder.toString();
	}

	private static void getTermsString(final StringBuilder builder, final List<? extends Term> terms) {
		if (terms != null) {
			builder.append("(");
			for (Term term: terms) {
				if (term.getValue() != null) {
					builder.append(term.getValue());
				}
				else if (term.getStringValue() != null) {
					builder.append(term.getStringValue());
				}
				else{
					builder.append(term.getIntValue());
				}
				builder.append(",");
			}
			builder.setLength(builder.length() - 1);
			builder.append(")");
		}
	}

	private static String getStringRules(final List<Rule> rules) {
		StringBuilder builder = new StringBuilder();
		for (Rule rule: rules) {
			Fact head = rule.getHead();
			if (head.isOpposite()) {
				builder.append("-");
			}
			builder.append(head.getPredicate());
			getTermsString(builder, head.getTerms());
			builder.append(" ").append(":-");
			Body body = rule.getBody();
			getAtomsAsString(builder, body.getAtoms());
			builder.append(". ");
		}
		return builder.toString();
	}

	private static void getAtomsAsString(final StringBuilder builder, final List<Atom> atoms) {
		for (Atom atom: atoms) {
			builder.append(" ");
			if (atom.isNaf()) {
				builder.append("not ");
			}
			if (atom.isOpposite()) {
				builder.append("-");
			}
			builder.append(atom.getPredicate());
			getTermsString(builder, atom.getTerms());
			builder.append(",");
		}
		builder.setLength(builder.length() - 1);
	}

	private static String rewritePrefixes(final String rules) {
		String result = rules;
		Map<String,String> map = PatternUtil.getPrefixes(rules);
		for (Entry<String,String> entry : map.entrySet()) {
			result = result.replace("\""+entry.getKey()+":","\""+entry.getValue());
		}
		return result;
	}

	private static ASPRules loadRule(AgentTaskInformation taskInfo, RuleSetLocation ruleSet) throws RDFBeanException {	
		Repository repo =  taskInfo.getDomainTDB().getInitializedRepository();
		if (ruleSet.getOriginBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			repo = taskInfo.getExecutionBeliefs().initialize();
		} else if (ruleSet.getOriginBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			repo = taskInfo.getAgentBeliefs().initialize();
		} else if (ruleSet.getOriginBase().toString().equals(AJANVocabulary.DOMAIN_KNOWLEDGE.toString())) {
			repo = taskInfo.getDomainTDB().getInitializedRepository();
		}
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn, taskInfo.getExtensions());
			return manager.get(repo.getValueFactory().createIRI(ruleSet.getRule().toString()), ASPRules.class);
		}
	}
}
