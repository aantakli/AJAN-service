/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Guard fuer eine bewusste Auslassung, kein Verhaltenstest.
 *
 * Etappe 3 setzt KEINEN jackson.version-Override in executionservice, obwohl
 * spring-boot-dependencies:1.3.5 jackson auf 2.6.6 haelt. Das traegt nur, solange
 * niemand SPARQL-Results-JSON anfordert: RDF4Js AbstractSPARQLJSONWriter/-Parser
 * brauchen JsonFactoryBuilder/StreamReadFeature/StreamWriteFeature, die es erst ab
 * Jackson 2.10 gibt (Gate E, docs/superpowers/specs/2026-07-11-spike-results.md).
 * Der Default-Pfad ist nicht betroffen: RDF4J verhandelt fuer entfernte Tupel-Queries
 * SPARQL/XML.
 *
 * Schlaegt dieser Test fehl, ist die Auslassung nicht mehr gedeckt - entweder den
 * Verwender auf ein anderes Ergebnisformat umstellen oder den Override doch setzen.
 */
public class SparqlJsonGuardTest {

	private static final List<String> FORBIDDEN = Arrays.asList(
			"TupleQueryResultFormat.JSON",
			"BooleanQueryResultFormat.JSON",
			"SPARQLResultsJSONWriter",
			"SPARQLResultsJSONParser",
			"SPARQLBooleanJSONWriter",
			"SPARQLBooleanJSONParser");

	private static final List<String> MODULES = Arrays.asList(
			"common", "behaviour", "functions", "executionservice", "pluginsystem");

	// Positive control: an empty `hits` list is ambiguous on its own - it is produced both by a
	// genuine clean scan AND by a scan that silently found nothing to look at (wrong ajan.root,
	// a module directory renamed or moved, the file filter matching zero files). Each guarded
	// module must therefore contribute files on its own; a per-module floor also implies the
	// global one. The smallest guarded module (common) holds ~22 .java files in src/main/java.
	private static final int MIN_FILES_PER_MODULE = 10;

	@Test
	public void noProductionCodeRequestsSparqlResultsJson() throws IOException {
		Path root = Paths.get(System.getProperty("ajan.root", "..")).toAbsolutePath().normalize();
		List<String> hits = new ArrayList<>();
		for (String module: MODULES) {
			Path moduleDir = root.resolve(module);
			int scanned = Files.isDirectory(moduleDir) ? collectHits(moduleDir, hits) : 0;
			assertTrue(scanned >= MIN_FILES_PER_MODULE,
					"Module '" + module + "' contributed only " + scanned + " .java file(s) under "
					+ moduleDir + " (expected at least " + MIN_FILES_PER_MODULE + "). The guard "
					+ "no longer covers this module (wrong ajan.root, module path/list outdated) - "
					+ "fix the scan before trusting its verdict.");
		}
		assertTrue(hits.isEmpty(),
				"SPARQL-Results-JSON is unusable under the managed Jackson 2.6.6 (see Gate E). "
				+ "Either switch to another result format or set <jackson.version> in "
				+ "executionservice/pom.xml. Offending sites:\n" + String.join("\n", hits));
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private int collectHits(final Path moduleDir, final List<String> hits) throws IOException {
		int scanned = 0;
		try (Stream<Path> files = Files.walk(moduleDir)) {
			for (Path file: (Iterable<Path>) files
					.filter(p -> p.toString().endsWith(".java"))
					.filter(p -> p.toString().replace('\\', '/').contains("/src/main/java/"))::iterator) {
				scanned++;
				String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
				for (String token: FORBIDDEN) {
					if (content.contains(token)) {
						hits.add(file + " uses " + token);
					}
				}
			}
		}
		return scanned;
	}
}
