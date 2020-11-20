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
import org.eclipse.rdf4j.sail.inferencer.fc.DedupingInferencer;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.spin.SpinSail;

public class ExecutionBeliefBase extends AbstractBeliefBase {
	private final Repository repo;

	public ExecutionBeliefBase(final TripleStoreManager.Inferencing useInferencing) {
		repo = createRepository(useInferencing);
	}

	private Repository createRepository(final TripleStoreManager.Inferencing useInferencing) {
		SpinSail spinSail;
		switch (useInferencing) {
			case RDFS:
				return new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
			case SPIN:
				spinSail = new SpinSail();
				spinSail.setBaseSail(new MemoryStore());
				return new SailRepository(spinSail);
			case RDFS_SPIN:
				spinSail = new SpinSail();
				spinSail.setBaseSail(new SchemaCachingRDFSInferencer(new DedupingInferencer(new MemoryStore())));
				return new SailRepository(spinSail);
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
