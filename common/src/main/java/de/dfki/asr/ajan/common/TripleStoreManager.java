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

import de.dfki.asr.ajan.common.exceptions.TripleStoreException;
import java.util.Set;

public interface TripleStoreManager {

	public enum Inferencing {
		NONE,
		RDFS,
		SPIN,
		RDFS_SPIN
	}

	Set<TripleDataBase> getAllTripleDataBases();

	TripleDataBase createTripleDataBase(final String id, boolean loadFiles) throws TripleStoreException;
	TripleDataBase createTripleDataBase(final String id, boolean loadFiles, Inferencing useInferencing) throws TripleStoreException;

	TripleDataBase createSecuredTripleDataBase(final String id, boolean loadFiles) throws TripleStoreException;
	TripleDataBase createSecuredTripleDataBase(final String id, boolean loadFiles, Inferencing useInferencing) throws TripleStoreException;

	TripleDataBase createSecuredAgentTripleDataBase(final String id, String managedTDB, Inferencing useInferencing, final Credentials agentAuth) throws TripleStoreException;

	void removeTripleDataBase(final TripleDataBase db) throws TripleStoreException;
}
