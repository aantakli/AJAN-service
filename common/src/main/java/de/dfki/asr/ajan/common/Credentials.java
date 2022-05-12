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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Data;
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
@Data
public class Credentials {
	private final String tokenController;
	private final String user;
	private final String role;
	private final String password;

	private String token = "";
	private final static int TIMEOUT = 1800000;
	private long created;

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Credentials.class);

	public Map<String,String> getJwtHeader() {
		Map<String,String> header = new ConcurrentHashMap();
		if (token.isEmpty() || checkTimeout()) {
			setToken();
		}
		if (!token.isEmpty()) {
			header.put("Authorization", "Bearer " + token);
		}
		return header;
	}

	private boolean checkTimeout() {
		if (created == 0) {
			created = System.currentTimeMillis();
		}
		return (System.currentTimeMillis() - created) >= TIMEOUT;
	}

	private void setToken() {
		if (tokenController != null && !tokenController.equals("")) {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				URIBuilder builder = new URIBuilder(tokenController);
				builder.setParameter("userId", user);
				builder.setParameter("role", role);
				builder.setParameter("pswd", password);
				HttpGet httpGet = new HttpGet(builder.build());
				try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
					if (response.getStatusLine().getStatusCode() < 300) {
						token = new BufferedReader(
								new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
									.lines()
									.collect(Collectors.joining("\n"));
						created = System.currentTimeMillis();
					}
				}
			} catch (URISyntaxException | IOException ex) {
				LOG.info("It was not possible to get token!", ex);
			}
		}
	}
}
