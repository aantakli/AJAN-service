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
import java.math.BigDecimal;
import java.util.List;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.util.ResourceUtils;

public final class Serializer {
	private Serializer() {

	}

	public static void getGraphFromSolution(ModelBuilder builder, String stableModel) {
		List<String> statements = PatternUtil.getStatements(stableModel);
		statements.stream().forEach((statement) -> {
			extractStatement(builder, statement);
		});
	}

	private static void extractStatement(ModelBuilder builder, String fact) {
		List<String> parts = PatternUtil.getTriple(fact);
		ValueFactory vf = SimpleValueFactory.getInstance();
		if (parts.size() == 3) {
			try {
				Resource subject = getResource(vf,parts.get(0),builder);
				IRI predicate = (IRI) getResource(vf,parts.get(1),builder);
				Literal literal = extractLiteral(vf,parts.get(2));
				if(literal == null) {
					builder.add(subject, predicate, getResource(vf,parts.get(2),builder));
				}
				else
					builder.add(subject, predicate, literal);
			}
			catch (ClassCastException | MalformedStatementException ex) {}
		}
	}

	private static Resource getResource(ValueFactory vf, String part, ModelBuilder builder) throws MalformedStatementException {
		String output = PatternUtil.getQuotesContent(part);
		if (!PatternUtil.getBlankContent(part)) {
			if(ResourceUtils.isUrl(output)) {
				return vf.createIRI(output);
			} else
				throw new MalformedStatementException("Wrong Resource description!");
		} else {
			BNode bNode = vf.createBNode(output);
			builder.add(bNode, RDF.TYPE, AJANVocabulary.GENERATED_BNODE);
			return bNode;
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
		number = getNumber(vf,part);
		if (number == null) {
				if(ResourceUtils.isUrl(part)) {
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
						return vf.createLiteral(part);
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
