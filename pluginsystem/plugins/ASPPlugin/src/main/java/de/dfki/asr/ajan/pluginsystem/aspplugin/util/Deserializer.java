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
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.ASPRules;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.net.URI;
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
		});
	}

	public static void loadRules(AgentTaskInformation taskInfo, StringBuilder set, List<URI> rules) throws URISyntaxException, RDFBeanException {
		for (URI resource : rules) {
			ASPRules rule = loadRule(taskInfo, resource);
			set.append(rewritePrefixes(rule.getRules()));
		}
	}

	private static String rewritePrefixes(final String rules) {
		String result = rules;
		Map<String,String> map = PatternUtil.getPrefixes(rules);
		for (Entry<String,String> entry : map.entrySet()) {
			result = result.replace("\""+entry.getKey()+":","\""+entry.getValue());
		}
		return result;
	}

	private static ASPRules loadRule(AgentTaskInformation taskInfo, URI resource) throws RDFBeanException {
		Repository repo = taskInfo.getDomainTDB().getInitializedRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			RDFBeanManager manager = new BehaviorBeanManager(conn, taskInfo.getExtensions());
			return manager.get(repo.getValueFactory().createIRI(resource.toString()), ASPRules.class);
		}
	}
}
