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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDF4JTripleDataBase extends AbstractSparqlTripleDataBase {
	private final HTTPRepository repo;
	private final Credentials auth;

	public RDF4JTripleDataBase(final RepositoryInfo info) throws MalformedURLException {
		super(info.getId(), info.getLocation(), new URL(info.getLocation().toString() + "/statements"));
		repo = new HTTPRepository(info.getLocation().toString());
		auth = null;
	}

	public RDF4JTripleDataBase(final RepositoryInfo info, final Credentials auth) throws MalformedURLException {
		super(info.getId(), info.getLocation(), new URL(info.getLocation().toString() + "/statements"));
		repo = new HTTPRepository(info.getLocation().toString());
		repo.setHttpClient(createHttpClient());
		this.auth = auth;
	}

	private CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create()
				.evictExpiredConnections()
				.setRetryHandler(new RetryHandlerStale())
				.setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryHandler())
				.useSystemProperties()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.build();
	}

	private static class RetryHandlerStale implements HttpRequestRetryHandler {
		private final Logger logger = LoggerFactory.getLogger(RetryHandlerStale.class);

		@Override
		public boolean retryRequest(final IOException ioe, final int count, final HttpContext context) {
			// only try this once
			if (count > 1) {
				return false;
			}
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			HttpConnection conn = clientContext.getConnection();
			if (conn != null) {
				synchronized (this) {
					if (conn.isStale()) {
						try {
							logger.warn("Closing stale connection");
							conn.close();
							return true;
						} catch (IOException e) {
							logger.error("Error closing stale connection", e);
						}
					}
				}
			}
			return false;
		}
	}

	private class ServiceUnavailableRetryHandler implements ServiceUnavailableRetryStrategy {
		private final Logger logger = LoggerFactory.getLogger(ServiceUnavailableRetryHandler.class);

		@Override
		public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
			if (checkResponse(response, executionCount)) {
				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpConnection conn = clientContext.getConnection();

				synchronized (this) {
					try {
						logger.info("Cleaning up closed connection");
						conn.close();
						return true;
					} catch (IOException e) {
						logger.error("Error cleaning up closed connection", e);
					}
				}
			}
			return false;
		}

		private boolean checkResponse(final HttpResponse response, final int executionCount) {
			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				if (header.getName().equals("AccessToken")) {
					auth.getAccessToken().setValue(header.getValue());
				}
				if (header.getName().equals("RefreshToken")) {
					auth.getRefreshToken().setValue(header.getValue());
				}
			}
			// only retry on `408`
			if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
				return false;
			}
			String keepAlive = System.getProperty("http.keepAlive", "true");
			if (!"true".equalsIgnoreCase(keepAlive)) {
				return false;
			}
			int pooledConnections = Integer.parseInt(System.getProperty("http.maxConnections", "5"));
			return executionCount > (pooledConnections + 1);
		}

		@Override
		public long getRetryInterval() {
			return 1000;
		}
	}

	@Override
	public Repository getInitializedRepository() {
		if (auth != null) {
			repo.setAdditionalHttpHeaders(getToken());
		}
		if (!repo.isInitialized()) {
			repo.init();
		}
		return repo;
	}

	private Map<String,String> getToken() {
		return auth.getJwtHeader();
	}

}
