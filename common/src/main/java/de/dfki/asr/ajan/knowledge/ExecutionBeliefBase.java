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

package de.dfki.asr.ajan.knowledge;

import de.dfki.asr.ajan.common.TripleStoreManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class ExecutionBeliefBase extends AbstractBeliefBase {
	private final Repository repo;

	public ExecutionBeliefBase(final TripleStoreManager.Inferencing useInferencing) {
		repo = createRepository(useInferencing);
	}

	private Repository createRepository(final TripleStoreManager.Inferencing useInferencing) {
		switch (useInferencing) {
			case RDFS:
				return new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
			case SPIN:
			case RDFS_SPIN:
				// RDF4J hat das SPIN-Sail mit 4.0 entfernt (rdf4j-sail-spin
				// existiert ab 4.0 nicht mehr auf Maven Central). Beide Zweige
				// waren schon vor der Migration unerreichbar: RDFAgentBuilder
				// und ParameterAgentBuilder setzen Inferencing.NONE hart. Der
				// Enum-Wert bleibt erhalten, damit die Konfigurationsoberflaeche
				// unveraendert bleibt; die Auswahl scheitert jetzt laut.
				throw new UnsupportedOperationException(
						"SPIN inferencing is no longer supported: RDF4J removed rdf4j-sail-spin in 4.0");
			default:
				return new SailRepository(new MemoryStore());
		}
	}

	@Override
	public Repository initialize() {
		repo.init();
		return repo;
	}

}
