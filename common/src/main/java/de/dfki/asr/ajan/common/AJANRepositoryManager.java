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

import de.dfki.asr.ajan.common.exceptions.TripleStoreException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
			if (initProtocolSession(protocolSession)) {
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

	@Override
	public boolean removeRepository(final String repositoryID) throws RepositoryException, RepositoryConfigException {
		try (RDF4JProtocolSession protocolSession = getSharedHttpClientSessionManager()
				.createRDF4JProtocolSession(serverURL)) {
			if (initProtocolSession(protocolSession)) {
				protocolSession.deleteRepository(repositoryID);
			}
		} catch (IOException | QueryEvaluationException | UnauthorizedException | NumberFormatException e) {
			throw new RepositoryException(e);
		}
		return true;
	}

	private boolean initProtocolSession(final RDF4JProtocolSession protocolSession) throws IOException {
		if (auth != null) {
			protocolSession.setAdditionalHttpHeaders(auth.getJwtHeader());
		}
		int serverProtocolVersion = Integer.parseInt(protocolSession.getServerProtocol());
		if (serverProtocolVersion < 9) { // explicit PUT create operation was introduced in Protocol version 9
			throw new RepositoryException(
					"Remote Server RDF4J Protocol version not compatible with this version of RDF4J");
		}
		return true;
	}

	public void setupAgentSecurityConfiguration(final Credentials agentAuth) throws URISyntaxException {
		if (auth != null) {
			Map<String,String> token = auth.getJwtHeader();
			try {
				sendPutRequest(createRoleRepoURI(auth.getConstraintURI(),agentAuth.getUser()), token);
				sendPutRequest(createUserRolePswdURI(auth.getUserURI(), agentAuth), token);
			} catch (IOException | URISyntaxException ex) {
				Logger.getLogger(AJANRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void removeAgentSecurityConfiguration(final String id) {
		if (auth != null) {
			Map<String,String> token = auth.getJwtHeader();
			try {
				sendDeleteRequest(createRoleRepoURI(auth.getConstraintURI(),id), token);
				sendDeleteRequest(createUserRoleURI(auth.getUserURI(),id), token);
			} catch (IOException | URISyntaxException ex) {
				Logger.getLogger(AJANRepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void sendPutRequest(final URI uri, final Map<String,String> token) throws URISyntaxException, IOException {
		HttpPut httpPut = new HttpPut(uri);
		String headerName = "Authorization";
		httpPut.addHeader(headerName, token.get(headerName));
		sendInformations(httpPut, uri);
	}

	private void sendDeleteRequest(final URI uri, final Map<String,String> token) throws URISyntaxException, IOException {
		HttpDelete httpDelete = new HttpDelete(uri);
		httpDelete.addHeader("Authorization", token.get("Authorization"));
		sendInformations(httpDelete, uri);
	}

	private URI createRoleRepoURI(final URIBuilder builder, final String user) throws URISyntaxException {
		builder.setParameter("role", user);
		builder.setParameter("repo", user);
		return builder.build();
	}

	private URI createUserRoleURI(final URIBuilder builder, final String user) throws URISyntaxException {
		builder.setParameter("user", user);
		builder.setParameter("role", user);
		return builder.build();
	}

	private URI createUserRolePswdURI(final URIBuilder builder, final Credentials agentAuth) throws URISyntaxException {
		builder.setParameter("user", agentAuth.getUser());
		builder.setParameter("role", agentAuth.getUser());
		builder.setParameter("pswd", agentAuth.getPassword());
		return builder.build();
	}

	private void sendInformations(final HttpRequestBase httpMethod, final URI uri) throws IOException, URISyntaxException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			try (CloseableHttpResponse response = httpClient.execute(httpMethod)) {
				int code = response.getStatusLine().getStatusCode();
				if (response.getStatusLine().getStatusCode() >= 300) {
					throw new TripleStoreException("error while putting security infos to: " + uri.toString() + ". Status code: " + code);
				}
			}
		}
	}
}
