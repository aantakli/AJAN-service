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

package de.dfki.asr.ajan.behaviour.nodes.action;

import de.dfki.asr.ajan.behaviour.nodes.action.definition.*;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.http.PerformHttpExecuteRequest;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.AbstractPerformRequest;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Succeed;
import de.dfki.asr.ajan.behaviour.service.impl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class PerformRequestTest extends AbstractTaskStepTest {

	private static final Logger LOG = LoggerFactory.getLogger(PerformRequestTest.class);
	private static final String PATH = "/test";
	private static final String TURTLE = "text/turtle";
	private static final String SERVICE_SPARQL = "DESCRIBE <http://bla/test#test>";

	private AbstractPerformRequest request;
	private final LocalTestServer testServer;

	@Spy
	private ServiceActionDefinition service;

	@Mock
	private IConnection connection;

	public PerformRequestTest() {
		testServer = new LocalTestServer(null, null);
	}

	@Override
	protected void initTestObjects() throws Exception {
		request = new PerformHttpExecuteRequest(new Succeed());
		setupTestServer();
		setupService();
	}

	private void setupTestServer() throws Exception {
		HttpRequestHandler dumbResponse = new HttpRequestHandler() {
			@Override
			public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
				HttpEntity e = new StringEntity("@prefix test: <http://test/test#> ."
												+ "test:Response test:type test:testResponse .", ContentType.create(TURTLE));
				response.setEntity(e);
			}
		};
		testServer.start();
		testServer.register(PATH, dumbResponse);
	}

	private void setupService() throws IOException {
		Mockito.doReturn(service).when(context).get(ServiceActionDefinition.class);
		Mockito.doReturn(new InputModel()).when(context).get(InputModel.class);
		Mockito.doReturn(connection).when(context).get(HttpConnection.class);
		Mockito.doReturn(new LinkedHashModel()).when(connection).execute();
	}

	@Test
	public void shouldRecieveResponseOutput() throws URISyntaxException {
		HttpBinding binding = new HttpBinding();
		service.setRun(binding);
		binding.setUrl("http://localhost:8090/rdf4j/repositories/services#test");
		binding.setPayload(new ActnPayload());
		binding.getPayload().setSparql(SERVICE_SPARQL);
		binding.setMethod(new URI("http://www.w3.org/2008/http-methods#POST"));
		binding.setRequestURI(getRequestUri());
		request.execute(context);
		assertThat(context.get(ResultModel.class), is(instanceOf(ResultModel.class)));
	}

	private URI getRequestUri() throws URISyntaxException {
		InetSocketAddress serviceAddress = testServer.getServiceAddress();
		URI uri = new URI("http", null, serviceAddress.getHostString(), serviceAddress.getPort(), PATH, null, null);
		LOG.info("Request URI: {}", uri);
		return uri;
	}

	@AfterClass
	public void shutdown() throws Exception {
		testServer.stop();
	}
}
