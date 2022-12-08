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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.common.exceptions.CredentialsException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

/**
 *
 * @author anan02-admin
 */
public class Credentials {

	@Getter
	private final String usersURL;
	@Getter
	private final String constraintURL;
	@Getter
	private final String loginURL;
	@Getter
	private final String user;
	@Getter
	private final String role;
	@Getter
	private final String password;
	@Getter @Setter
	private Token accessToken;
	@Getter @Setter
	private Token refreshToken;

	private int timeout;
	private long created;

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Credentials.class);

	@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.NcssConstructorCount"})
	public Credentials(final String usersURL, final String constraintURL,
					final String login, final String user, final String role,
					final String password, final Token accessToken, final Token refreshToken) {
		this.usersURL = usersURL;
		this.constraintURL = constraintURL;
		this.loginURL = login;
		this.user = user;
		this.role = role;
		this.password = password;
		if (accessToken == null) {
			this.accessToken = createToken("", "AccessToken", "Authorization");
		} else {
			this.accessToken = accessToken;
		}
		if (refreshToken == null) {
			this.refreshToken = createToken("", "RefreshToken", "RefreshToken");
		} else {
			this.refreshToken = refreshToken;
		}
	}

	private Token createToken(final String value, final String jsonField, final String header) {
		Token token = new Token();
		token.setHeader(value);
		token.setJsonField(jsonField);
		token.setHeader(header);
		return token;
	}

	public URIBuilder getConstraintURI() throws URISyntaxException {
		return new URIBuilder(loginURL).setPath(usersURL);
	}

	public URIBuilder getUserURI() throws URISyntaxException {
		return new URIBuilder(loginURL).setPath(constraintURL);
	}

	public Map<String,String> getJwtHeader() {
		Map<String,String> header = new ConcurrentHashMap();
		if (accessToken.getValue().isEmpty() && refreshToken.getValue().isEmpty()
						|| accessToken.getValue().isEmpty() && checkTimeout()) {
			sendPostRequest(this.loginURL, getLoginPayload());
		}
		if (!accessToken.getValue().isEmpty()) {
			header.put(accessToken.getHeader(), "Bearer " + accessToken.getValue());
		}
		if (!refreshToken.getValue().isEmpty()) {
			header.put(refreshToken.getHeader(), "Bearer " + refreshToken.getValue());
		}
		return header;
	}

	private boolean checkTimeout() {
		if (timeout == 0) {
			return true;
		}
		if (created == 0) {
			created = System.currentTimeMillis();
		}
		return (System.currentTimeMillis() - created) >= timeout;
	}

	private void sendPostRequest(final String url, final String payload) {
		if (!url.isEmpty()) {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(url);
				HttpEntity postParams = new StringEntity(payload, ContentType.APPLICATION_JSON);
				httpPost.setEntity(postParams);
				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode < 300) {
						String responsePayload = new BufferedReader(
											new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
												.lines().collect(Collectors.joining("\n"));
						setResponseValues(responsePayload);
					} else {
						throw new CredentialsException("It was not possible to login agent at: " + url + ", with user: " + this.user + ", to interact with AgentKnowledge! Server responded: " + statusCode);
					}
				}
			} catch (IOException ex) {
				throw new CredentialsException("It was not possible to login agent at: " + url + ", with user: " + this.user + ", to interact with AgentKnowledge! Server may not be accessible.", ex);
			}
		}
	}

	private String getLoginPayload() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.createObjectNode();
		((ObjectNode) node).put("username", user);
		if (!role.isEmpty()) {
			((ObjectNode) node).put("role", role);
		}
		((ObjectNode) node).put("password", password);
		return node.toString();
	}

	private void setResponseValues(final String payload) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(payload);
		JsonNode accessNode = node.findPath(accessToken.getJsonField());
		JsonNode refreshNode = node.findPath(refreshToken.getJsonField());
		JsonNode timeoutNode = node.findPath("expirySecs");
		if (accessNode == null) {
			accessToken.setValue("");
		} else {
			accessToken.setValue(accessNode.textValue());
		}
		if (refreshNode == null) {
			refreshToken.setValue("");
		} else {
			refreshToken.setValue(refreshNode.textValue());
		}
		if (timeoutNode == null) {
			timeout = 0;
		} else {
			timeout = timeoutNode.asInt();
		}
		created = System.currentTimeMillis();
	}
}
