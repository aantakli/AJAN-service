/*
 * Copyright (C) 2022 anan02-admin.
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
import org.eclipse.rdf4j.http.client.RDF4JProtocolSession;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;

/**
 *
 * @author anan02-admin
 */
public class AJANRepositoryManager extends RemoteRepositoryManager {

	private Credentials auth;
	private final String serverURL;

	public AJANRepositoryManager(final String serverURL) {
		super(serverURL);
		this.serverURL = serverURL;
	}

	public AJANRepositoryManager(final String serverURL, final Credentials auth) {
		super(serverURL);
		this.serverURL = serverURL;
		this.auth = auth;
	}

	@Override
	public void addRepositoryConfig(final RepositoryConfig config) throws RepositoryException, RepositoryConfigException {
		try (RDF4JProtocolSession protocolSession = getSharedHttpClientSessionManager()
				.createRDF4JProtocolSession(serverURL)) {
			if (auth != null) {
				protocolSession.setAdditionalHttpHeaders(auth.getJwtHeader());
			}

			int serverProtocolVersion = Integer.parseInt(protocolSession.getServerProtocol());
			if (serverProtocolVersion < 9) { // explicit PUT create operation was introduced in Protocol version 9
				throw new RepositoryException(
						"Remote Server RDF4J Protocol version not compatible with this version of RDF4J");
			} else {
				if (hasRepositoryConfig(config.getID())) {
					protocolSession.updateRepository(config);
				} else {
					protocolSession.createRepository(config);
				}
			}
		} catch (IOException | QueryEvaluationException | UnauthorizedException | NumberFormatException e) {
			throw new RepositoryException(e);
		}
	}
}
