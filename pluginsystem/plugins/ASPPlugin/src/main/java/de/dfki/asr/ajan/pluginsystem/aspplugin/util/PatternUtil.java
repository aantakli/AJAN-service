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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PatternUtil {

	private static final Pattern PREFIX_PATTERN = Pattern.compile("_p\\((.*?)\\)");
	private static final Pattern QUOTES_PATTERN = Pattern.compile("\"\\s*(.*?)\\s*\"");
	private static final Pattern CONTENT_PATTERN = Pattern.compile("\\(\\s*(.*?)\\s*\\)");
	private static final Pattern LITERAL_PATTERN = Pattern.compile("_l\\(\\s*(.*?)\\s*\\)");
	private static final Pattern FACTS_PATTERN = Pattern.compile("(?<=\\s)(.*?[\\(\\)]*)(?=\\s|\\Z)");
	private static final Pattern STATEMENT_PATTERN = Pattern.compile("(?<=_t\\()(.*?[\\(\\)]*)(?=\\)\\s|\\)\\Z)");
	private static final Pattern TRIPLE_PATTERN = Pattern.compile("\\\"\\s*(.*?)\\s*\\\"|_r\\((.*?)\\)|_b\\((.*?)\\)|_l(.*?)\\)|(?<=[\\\",])(\\d+|true|false)\\Z");
	private static final Pattern BLANKS_PATTERN = Pattern.compile("_b\\((.*?)\\)");
	private static final Pattern XSD_PATTERN = Pattern.compile("((?<=http:\\/\\/www\\.w3\\.org\\/2001\\/XMLSchema#)(.*?)\\Z)");

	private PatternUtil() {
	
	}

	public static List<List<String>> getRulesAndConstraints(final String stableModel) {
		List<List<String>> result = new ArrayList();
		List<String> columns = Arrays.asList(stableModel.split("\\."));
		for (String column: columns) {
			result.add(getElements(column));
		}
		return result;
	}
	
	public static List<String> getElements(final String stableModel) {
		List<String> statements = new ArrayList();
		Matcher m = FACTS_PATTERN.matcher(" " + stableModel);
		while (m.find()) {
			String element = m.group().replaceAll(" ", "");
			if (element.endsWith(";") || element.endsWith(",")) {
				element = element.substring(0, element.length() - 1);
			}
			if (!element.equals(""))
				statements.add(element);
		}
		return statements;
	}

	public static List<String> getFacts(final String stableModel) {
		List<String> statements = new ArrayList();
		Matcher m = FACTS_PATTERN.matcher(" " + stableModel);
		while (m.find())
			statements.add(m.group() + ".");
		return statements;
	}

	public static List<String> getStatements(final String stableModel) {
		List<String> statements = new ArrayList();
		Matcher m = STATEMENT_PATTERN.matcher(stableModel);
		while (m.find())
			statements.add(m.group());
		return statements;
	}

	public static List<String> getTriple(final String fact) {
		Matcher m = TRIPLE_PATTERN.matcher(fact);
		List<String> parts = new ArrayList();
		while (m.find()) {
			parts.add(m.group());
		}
		return parts;
	}

	public static Map<String,String> getPrefixes(String rules) {
		Matcher m = PREFIX_PATTERN.matcher(rules);
		Map<String,String> prefixes = new HashMap();
		while (m.find()) {
			String[] prefix = m.group().split(",");
			prefixes.put(getQuotesContent(prefix[0]), getQuotesContent(prefix[1]));
		}
		return prefixes;
	}

	public static boolean getBlankContent(final String part) {
		Matcher m = BLANKS_PATTERN.matcher(part);
		return m.find();
	}

	public static String getQuotesContent(final String part) {
		Matcher m = QUOTES_PATTERN.matcher(part);
		if (m.find())
			return m.group().replaceAll("\"", "");
		else
			return "";
	}

	public static String getContent(final String part) {
		Matcher m = CONTENT_PATTERN.matcher(part);
		if (m.find())
			return m.group().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\"", "");
		else
			return "";
	}

	public static List<String> getLiteral(final String literal) {
		Matcher m = LITERAL_PATTERN.matcher(literal);
		List<String> parts = new ArrayList();
		while (m.find()) {
			String[] content = m.group().replaceAll("_l\\(", "")
										.replaceAll("\\)", "")
										.split(",");
			for (int i = 0; i < content.length; i++) {
				parts.add(content[i].replaceAll("\"", ""));
			}
		}
		return parts;
	}

	public static String getXSDType(final String literal) {
		Matcher mXSD = XSD_PATTERN.matcher(literal);
		if(mXSD.find())
			return mXSD.group();
		else
			return "";
	}
}
