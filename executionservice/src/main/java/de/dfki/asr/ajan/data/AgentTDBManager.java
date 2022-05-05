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

package de.dfki.asr.ajan.data;

import de.dfki.asr.ajan.common.Credentials;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.common.TripleStoreManager;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import lombok.Getter;
import lombok.Setter;

public class AgentTDBManager {

	@Getter @Setter
	private TripleDataBase agentTemplatesTDB;
	@Getter @Setter
	private TripleDataBase behaviorTDB;
	@Getter @Setter
	private TripleDataBase domainTDB;
	@Getter @Setter
	private TripleDataBase serviceTDB;

	private final TripleStoreManager tdbManager;

	public AgentTDBManager(final TripleStoreManager manager) {
		this.tdbManager = manager;
	}

	public TripleDataBase createAgentTDB(final String id, final boolean overwrite, final Inferencing infer, final Credentials auth) {
		return tdbManager.createSecuredTripleDataBase(id, overwrite, infer, auth);
	}
}
