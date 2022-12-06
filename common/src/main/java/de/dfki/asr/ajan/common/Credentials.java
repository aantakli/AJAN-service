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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

/**
 *
 * @author anan02-admin
 */
public class Credentials {

	private final boolean externalAccess;
	private final String tripleStoreURL;
	@Getter
	private final String user;
	@Getter
	private final String role;
	@Getter
	private final String password;

	private String accessToken = "";
	private String refreshToken = "";

	private int timeout;
	private long created;

	private static final String TOKEN = "/tokenizer/token";
	private static final String CONSTRAINT = "/tokenizer/constraint";
	private static final String USER = "/tokenizer/user";

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Credentials.class);

	public Credentials(final String url, final String accessToken, final String refreshToken) {
		externalAccess = true;
		this.tripleStoreURL = url;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.user = this.role = this.password = "";
	}

	public Credentials(final String url, final String user, final String role, final String password) {
		externalAccess = false;
		this.tripleStoreURL = url;
		this.user = user;
		this.role = role;
		this.password = password;
	}

	public URIBuilder getTokenURI() throws URISyntaxException {
		return new URIBuilder(tripleStoreURL).setPath(TOKEN);
	}

	public URIBuilder getConstraintURI() throws URISyntaxException {
		return new URIBuilder(tripleStoreURL).setPath(CONSTRAINT);
	}

	public URIBuilder getUserURI() throws URISyntaxException {
		return new URIBuilder(tripleStoreURL).setPath(USER);
	}

	public Map<String,String> getJwtHeader() {
		if (externalAccess) {
			return getExternalToken();
		} else {
			return getTokenizerToken();
		}
	}

	private Map<String,String> getTokenizerToken() {
		Map<String,String> header = new ConcurrentHashMap();
		if (accessToken.isEmpty() || checkTimeout()) {
			setTokenizerToken();
		}
		if (!accessToken.isEmpty()) {
			header.put("Authorization", "Bearer " + accessToken);
		}
		return header;
	}

	private Map<String,String> getExternalToken() {
		Map<String,String> header = new ConcurrentHashMap();
		if (!accessToken.isEmpty()) {
			header.put("AccessToken", accessToken);
		}
		if (!refreshToken.isEmpty()) {
			header.put("RefreshToken", refreshToken);
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

	public void setAccessToken(final String token) {
		accessToken = token;
	}

	public void setRefreshToken(final String token) {
		refreshToken = token;
	}

	private void setTokenizerToken() {
		if (tripleStoreURL != null && !tripleStoreURL.equals("")) {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				URIBuilder builder = getTokenURI();
				builder.setParameter("user", user);
				builder.setParameter("role", role);
				builder.setParameter("pswd", password);
				HttpGet httpGet = new HttpGet(builder.build());
				try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
					if (response.getStatusLine().getStatusCode() < 300) {
						String payload = new BufferedReader(
											new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
												.lines()
												.collect(Collectors.joining("\n"));
						setResponseValues(payload);
					}
				}
			} catch (URISyntaxException | IOException ex) {
				LOG.info("It was not possible to get token!", ex);
			}
		}
	}

	private void setResponseValues(final String payload) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(payload);
		accessToken = node.findValue("token").textValue();
		timeout = node.findValue("expirySecs").asInt();
		created = System.currentTimeMillis();
	}
}
