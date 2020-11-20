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

package de.dfki.asr.ajan.behaviour.load;

import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class ServiceClientTest {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceClientTest.class);
	private static final String PATH = "/test";
	private static final String TURTLE = "text/turtle";

	private final LocalTestServer server;
	private UnmarshalBehaviourTest.MockTDB servicesTDB;
	private HttpRequestHandler handler;

	public ServiceClientTest() {
		server = new LocalTestServer(null, null);
	}

	@BeforeClass
	public void startServer() throws Exception {
		LOG.info("Starting server.");
		HttpRequestHandler dumbResponse = new HttpRequestHandler() {
			@Override
			public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
				HttpEntity e = new StringEntity("", ContentType.create(TURTLE));
				response.setEntity(e);
			}
		};
		handler = Mockito.spy(dumbResponse);
		server.start();
		server.register(PATH, handler);
	}

	@BeforeClass
	public void readDescriptions() throws MalformedURLException, IOException {
		servicesTDB = new UnmarshalBehaviourTest.MockTDB("serviceMock");
		servicesTDB.add(getClass().getResourceAsStream("serviceClientBindings.ttl"), RDFFormat.TURTLE);
	}

	@BeforeMethod
	public void resetHandlerCalls() {
		Mockito.reset(handler);
	}

	private ServiceActionDefinition getService(final String serviceID) throws RDFBeanException, URISyntaxException {
		Repository repo = servicesTDB.getInitializedRepository();
		RepositoryConnection conn = repo.getConnection();
		AJANPluginLoader pluginLoader = new AJANPluginLoader();
		RDFBeanManager manager = new BehaviorBeanManager(conn, pluginLoader.getNodeExtensions());
		String rootIRI = "http://www.ajan.de/ajan-test#" + serviceID;
		ServiceActionDefinition service = manager.get(repo.getValueFactory().createIRI(rootIRI), ServiceActionDefinition.class);
		conn.close();
		assertThat(service, notNullValue());
		assertThat(service.getRun(), notNullValue());
		service.getRun().setRequestURI(getRequestUri());
		return service;
	}

	@Test
	public void shouldIncludeAcceptHeader() throws RDFBeanException, URISyntaxException, IOException, HttpException {
		HttpRequest request = performRequest("POSTAcceptTurtle");
		assertThat(request.getFirstHeader("accept").getValue(), is(TURTLE));
		assertThat(request.getRequestLine().getMethod(), is("POST"));
		assertThat(request, is(instanceOf(HttpEntityEnclosingRequest.class)));
		HttpEntityEnclosingRequest er = (HttpEntityEnclosingRequest) request;
		assertThat(er.getEntity().getContentLength(), is(not(0)));
		assertThat(er.getEntity().getContentType().getValue(), is(TURTLE));
	}

	@Test
	public void shouldModifyRequestLine() throws RDFBeanException, URISyntaxException, IOException, HttpException {
		HttpRequest request = performRequest("GETviaHTTP10");
		assertThat(request.getRequestLine().getMethod(), is("GET"));
		assertThat(request.getRequestLine().getProtocolVersion().toString(), is("HTTP/1.0"));
		assertThat(request.getFirstHeader("accept").getValue(), is(TURTLE));
	}

	private HttpRequest performRequest(final String serviceName) throws HttpException, URISyntaxException, RDFBeanException, IOException {
		ServiceActionDefinition s = getService(serviceName);
		HttpConnection client = new HttpConnection(s.getRun());
		client.setPayload("test");
		client.execute();
		ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
		Mockito.verify(handler).handle(request.capture(), Mockito.any(), Mockito.any());
		return request.getValue();
	}

	@AfterClass
	public void shutdownServer() throws Exception {
		server.stop();
	}

	private URI getRequestUri() throws URISyntaxException {
		InetSocketAddress serviceAddress = server.getServiceAddress();
		URI uri = new URI("http", null, serviceAddress.getHostString(), serviceAddress.getPort(), PATH, null, null);
		LOG.info("Request URI: {}", uri);
		return uri;
	}
}
