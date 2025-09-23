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

import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.aspplugin.exception.MalformedStatementException;
import de.dfki.asr.ajan.pluginsystem.aspplugin.vocabularies.ASPVocabulary;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.util.ResourceUtils;

public final class Serializer {
	
	private Serializer() {

	}

	public static void getGraphFromSolution(Resource bnode, ModelBuilder builder, List<Resource> terms, String stableModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		if (stableModel.contains(":-")) {
			getGraphFromRules(bnode, builder, stableModel);
		} else {
			getGraphFromFacts(builder, terms, stableModel);
			BNode head = vf.createBNode();
			builder.add(bnode, ASPVocabulary.HAS_FACTS, head);
			Model partsModel = RDFCollections.asRDF(terms, head, new LinkedHashModel());
			partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
		}
	}

	public static void getGraphFromRules(Resource bnode, ModelBuilder builder, String stableModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		List<List<String>> columns = PatternUtil.getRulesAndConstraints(stableModel);
		List<Resource> constraints = new ArrayList();
		List<Resource> rules = new ArrayList();
		columns.stream().forEach((column) -> {
			if (column.get(0).endsWith(":-")) {
				List<Resource> constraint = new ArrayList();
				getGraphFromConstraint(vf, column, builder, constraint, stableModel);
				BNode head = vf.createBNode();
				BNode subject = vf.createBNode();
				constraints.add(subject);
				builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.CONSTRAINT);
				builder.add(subject, ASPVocabulary.HAS_ATOMS, head);
				Model partsModel = RDFCollections.asRDF(constraint, head, new LinkedHashModel());
				partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
			} else {
				BNode rule = vf.createBNode();
				builder.add(rule, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.RULE);
				getGraphFromRule(rule, vf, column, builder, stableModel);
				rules.add(rule);
			}
		});
		if (!constraints.isEmpty()) {
			BNode head = vf.createBNode();
			builder.add(bnode, ASPVocabulary.HAS_CONSTRAINTS, head);
			Model partsModel = RDFCollections.asRDF(constraints, head, new LinkedHashModel());
			partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
		}
		if (!rules.isEmpty()) {
			BNode head = vf.createBNode();
			builder.add(bnode, ASPVocabulary.HAS_RULES, head);
			Model partsModel = RDFCollections.asRDF(rules, head, new LinkedHashModel());
			partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
		}
	}
	
	public static void getGraphFromConstraint(ValueFactory vf, List<String> column, ModelBuilder builder, List<Resource> terms, String stableModel) {
		column.stream().forEach((statement) -> {
			if (!statement.equals(":-")) {
				BNode subject = vf.createBNode();
				terms.add(subject);
				builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.ATOM);
				extractStatement(builder, statement, subject, vf);
			}
		});
	}
	
	public static void getGraphFromRule(Resource rule, ValueFactory vf, List<String> column, ModelBuilder builder, String stableModel) {
		boolean bodyStart = false;
		boolean not = false;
		List<Resource> atoms = new ArrayList();
		BNode list = vf.createBNode();
		for (String statement: column) {
			if (statement.equals(":-")) {
				bodyStart = true;
			} else if (statement.equals("not")) {
				not = true;
			} else if (!bodyStart) {
				BNode subject = vf.createBNode();
				builder.add(rule, ASPVocabulary.HAS_HEAD, subject);
				builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.FACT);
				extractStatement(builder, statement, subject, vf);
			} else if (bodyStart) {
				BNode subject = vf.createBNode();
				atoms.add(subject);
				builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.ATOM);
				if (bodyStart && not) {
					not = false;
					builder.add(subject, ASPVocabulary.HAS_NAF, true);
				} else {
					builder.add(subject, ASPVocabulary.HAS_NAF, false);
				}
				extractStatement(builder, statement, subject, vf);
			} 
		}
		BNode body = vf.createBNode();
		builder.add(rule, ASPVocabulary.HAS_BODY, body);
		builder.add(body, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.BODY);
		builder.add(body, ASPVocabulary.HAS_ATOMS, list);
		Model partsModel = RDFCollections.asRDF(atoms, list, new LinkedHashModel());
		partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
	}

	public static void getGraphFromFacts(ModelBuilder builder, List<Resource> terms, String stableModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		List<String> statements = PatternUtil.getFacts(stableModel);
		statements.stream().forEach((statement) -> {
			BNode subject = vf.createBNode();
			terms.add(subject);
			builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.FACT);
			extractStatement(builder, statement, subject, vf);
		});
	}

	private static void extractStatement(ModelBuilder builder, String atom, BNode subject, ValueFactory vf) {
		if (atom.startsWith("_t")) {
			extractRDFStatements(PatternUtil.getTriple(atom),vf,builder);
		}
		else {
			String aspPredicate;
			if (atom.contains("(")) {
				aspPredicate = atom.substring(0,atom.indexOf("("));
			} else if (atom.endsWith(".")){
				aspPredicate = atom.substring(0,atom.indexOf("."));
			} else {
				aspPredicate = atom;
			}
			if (aspPredicate.startsWith("-")) {
				builder.add(subject, ASPVocabulary.HAS_OPPOSITE, true);
				builder.add(subject, ASPVocabulary.HAS_PREDICATE, aspPredicate.substring(1));
			}
			else {
				builder.add(subject, ASPVocabulary.HAS_OPPOSITE, false);
				builder.add(subject, ASPVocabulary.HAS_PREDICATE, aspPredicate);
			}
			if (atom.contains("(")) {
				String first = atom.substring(atom.indexOf("(")+1);
				String last;
				if (first.endsWith(".")) last = first.substring(0,first.lastIndexOf(")."));
				else last = first.substring(0,first.lastIndexOf(")"));
				List<Literal> list = new ArrayList();
				getParts(list,vf,last);
				BNode head = vf.createBNode();
				List<Resource> terms = createTerms(builder, vf, list);
				builder.add(subject, ASPVocabulary.HAS_TERMS, head);
				Model partsModel = RDFCollections.asRDF(terms, head, new LinkedHashModel());
				partsModel.forEach(stmt -> {builder.add(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());});
			}
		}
	}

	private static void getParts(final List<Literal> list, final ValueFactory vf, final String fact) {
		if (fact.startsWith("\"")) {
			String first = fact.substring(fact.indexOf('"'));
			if (first.contains("\",")) {
				String[] last = first.split("\",",2);
				list.add(vf.createLiteral(last[0]+'"'));
				getParts(list,vf,last[1]);
			}
			else {
				String last = first.substring(0,first.lastIndexOf('"')+1);
				list.add(vf.createLiteral(last));
			}
		}
		else if (fact.contains(",")) {
			String last = fact.substring(0,fact.lastIndexOf(","));
			list.add(vf.createLiteral(last));
			getParts(list,vf,fact.substring(fact.indexOf(",")+1));
		}
		else {
			try {
				list.add(vf.createLiteral(Integer.parseInt(fact)));
			} catch (NumberFormatException e) {
				list.add(vf.createLiteral(fact));
			}
		}
	}

	private static List<Resource> createTerms(ModelBuilder builder, final ValueFactory vf, List<Literal> list) {
		List<Resource> terms = new ArrayList();
		for (Literal lit: list) {
			BNode subject = vf.createBNode();
			try { 
					Integer.parseInt(lit.stringValue());
					builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.TERM);
					builder.add(subject, ASPVocabulary.HAS_INT_VALUE, lit.stringValue());
				} catch(NumberFormatException e) { 
					if (Character.isUpperCase(lit.stringValue().charAt(0))) {
						builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.VARIABLE);
						builder.add(subject, ASPVocabulary.HAS_VALUE, lit.stringValue());
					} else if (Character.isLowerCase(lit.stringValue().charAt(0))) {
						builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.CONSTANT);
						builder.add(subject, ASPVocabulary.HAS_VALUE, lit.stringValue());
					} else {
						builder.add(subject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, ASPVocabulary.TERM);
						builder.add(subject, ASPVocabulary.HAS_STRING_VALUE, lit.stringValue());
					}
				}
			terms.add(subject);
		}
		return terms;
	}

	private static void extractRDFStatements(final List<String> parts, final ValueFactory vf, final ModelBuilder builder) {
		if (parts.size() == 3) {
			try {
				Resource subject = getResource(vf,parts.get(0),builder);
				IRI predicate = (IRI) getResource(vf,parts.get(1),builder);
				if(parts.get(2).startsWith("_r(")) {
					Resource object = vf.createIRI(readNewResource(parts.get(2)));
					builder.add(subject, predicate, object);
				} else {
					Literal literal = extractLiteral(vf,parts.get(2));
					if(literal == null) {
						builder.add(subject, predicate, getResource(vf,parts.get(2),builder));
					}
					else
						builder.add(subject, predicate, literal);
				}
			}
			catch (ClassCastException | MalformedStatementException ex) {}
		}
	}

	private static Resource getResource(ValueFactory vf, String part, ModelBuilder builder) throws MalformedStatementException {
		if (!PatternUtil.getBlankContent(part)) {
			if(part.startsWith("_r(")) {
				return vf.createIRI(readNewResource(part));
			}
			String output = PatternUtil.getQuotesContent(part);
			if(ResourceUtils.isUrl(output)) {
				return vf.createIRI(output);
			} else {
				throw new MalformedStatementException("Wrong Resource description!");
			}
		} else {
			return getBNode(vf, part, builder);
		}
	}

	private static Resource getBNode(ValueFactory vf, String part, ModelBuilder builder) {
		String output = "";
		String[] parts = part.split(",");
		if (parts.length == 2) {
			String part2 = parts[1].replaceAll("\"","").replaceAll("\\)", "");
			output = PatternUtil.getQuotesContent(parts[0]) + part2;
		} else {
			output = PatternUtil.getQuotesContent(part);
			if (output.equals("")) {
				output = PatternUtil.getContent(part);
				output = "aspBlank_" + output;
			}
		}
		BNode bNode;
		if (output.equals(""))
			bNode = vf.createBNode();
		else
			bNode = vf.createBNode(output);
		builder.add(bNode, RDF.TYPE, AJANVocabulary.GENERATED_BNODE);
		return bNode;
	}

	private static String readNewResource(final String part) throws MalformedStatementException {
		String content = part.replaceAll("_r\\(", "").replaceAll("\"","");
		String[] parts = content.replaceAll("\\)", "").split(",");
		try {
			if (ResourceUtils.isUrl(parts[0])) {
				return parts[0] + Integer.parseInt(parts[1]); 
			} else {
				throw new MalformedStatementException("Wrong Resource description!");
			}
		} catch (NumberFormatException | NullPointerException ex) {
			throw new MalformedStatementException("Second argument in _r() is no integer!");
		}
		
	}

	private static Literal extractLiteral(ValueFactory vf, String part) {
		Literal literal = null;
		if (PatternUtil.getBlankContent(part))
			return null;
		List<String> parts = PatternUtil.getLiteral(part);
		switch (parts.size()) {
			case 2:
				String xsd = PatternUtil.getXSDType(parts.get(1));
				if(!xsd.isEmpty()) {
					literal = readXSD(vf, parts.get(0), xsd);
				}
				break;
			case 1:
				literal = getLiteral(vf, parts.get(0));
				break;
			default:
				literal = getLiteral(vf, part);
				break;
		}
		return literal;
	}

	private static Literal readXSD(ValueFactory vf, String literal, String type) {
		if (type == null)
			return null;
		try {
			switch (type) {
				case "string":
					return vf.createLiteral(literal);
				case "boolean":
					return vf.createLiteral(Boolean.parseBoolean(literal));
				case "decimal":
					return vf.createLiteral(new BigDecimal(Double.parseDouble(literal)));
				case "float":
					return vf.createLiteral(Float.parseFloat(literal));
				case "double":
					return vf.createLiteral(Double.parseDouble(literal));
				case "short":
					return vf.createLiteral(Short.parseShort(literal));
				case "integer":
					return vf.createLiteral(Integer.parseInt(literal));
				case "int":
					return vf.createLiteral(Integer.parseInt(literal));
				case "long":
					return vf.createLiteral(Long.parseLong(literal));
				default:
					return null;
			}
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static Literal getLiteral(ValueFactory vf, String part) {
		Literal number;
		String input = part.replaceAll("\"", "");
		number = getNumber(vf,input);
		if (number == null) {
				if(ResourceUtils.isUrl(input)) {
					return null;
				}
				switch (part) {
					case "true":
						return vf.createLiteral(true);
					case "false":
						return vf.createLiteral(false);
					case "":
						return null;
					default:
						return vf.createLiteral(input);
				}
			}
			else
				return number;
	}

	private static Literal getNumber(ValueFactory vf, String output) {
		Literal literal = null;
		for (int i = 0; i < output.length(); i++) {
			if ((i == 0) && (output.charAt(i) == '-')) {
				continue;
			}
			if (!Character.isDigit(output.charAt(i)))
				break;
			if ((i+1) == output.length()) {
				if (i < 5)
					literal = vf.createLiteral(Short.parseShort(output));
				else if (i < 10)
					literal = vf.createLiteral(Integer.parseInt(output));
				else if (i < 19)
					literal = vf.createLiteral(Long.parseLong(output));
			}
		}
		return literal;
	}
}
