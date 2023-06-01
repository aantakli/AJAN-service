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

package de.dfki.asr.ajan.behaviour.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.exception.AJANRequestException;
import de.dfki.asr.ajan.common.AgentUtil;
import static de.dfki.asr.ajan.common.AgentUtil.formatForMimeType;
import de.dfki.asr.ajan.common.CSVInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Getter;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("PMD.ExcessiveImports")
public class HttpConnection implements IConnection {
	private static final Logger LOG = LoggerFactory.getLogger(HttpConnection.class);

	protected final HttpServiceRequest request;
	private final List<String> processIds;

	@Getter
	private Event event;
	@Getter
	private final UUID id;
	@Getter
	protected HttpBinding binding;
	protected int timeout = 5;
	private final RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(timeout * 500)
					.setConnectionRequestTimeout(timeout * 500)
					.setSocketTimeout(timeout * 5000).build();

	public HttpConnection(final HttpBinding binding) {
		this.binding = binding;
		request = new HttpServiceRequest(binding);
		processIds = new ArrayList<>();
		this.id = UUID.randomUUID();
	}

	@Override
	public void setPayload(final String payload) {
		request.setEntity(payload);
	}

	@Override
	public boolean addProcessId(final String id) {
		return processIds.add(id);
	}

	@Override
	public boolean removeProcessId(final String id) {
		LOG.info("remove: " + id);
		for (String index: processIds) {
			LOG.info("index :" + index);
		}
		return processIds.remove(id);
	}

	@Override
	public void setEvent(final Event event) {
		this.event = event;
	}

	@Override
	public Object execute() throws HttpResponseException, IOException, SAXException, AJANRequestException {
		LOG.info("Executing request {}", request.toString());
		return sendRequest();
	}

	private Object sendRequest() throws HttpResponseException, IOException, SAXException, AJANRequestException {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(2, false)).build();
			CloseableHttpResponse response = httpClient.execute(request)) {
			StatusLine statusLine = response.getStatusLine();
			LOG.info("Status Code:" + statusLine.getStatusCode());
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
			HttpEntity entity = response.getEntity();
			if (entity != null && entity.getContentLength() != 0) {
				return readContent(response);
			}
			return new LinkedHashModel();
		}
	}

	private String getFormatFromResponse(final HttpEntity entity) {
		Header contentType = entity.getContentType();
		if (contentType != null && contentType.getValue() != null) {
			checkExpectedMimeType(contentType.getValue());
			return contentType.getValue();
		}
		return null;
	}

	private Object readContent(final CloseableHttpResponse response) throws SAXException, AJANRequestException, IOException {
		String mimeType = getFormatFromResponse(response.getEntity());
		InputStream content = response.getEntity().getContent();
		MultivaluedMap mm = getReadHeaders(response.getAllHeaders());
		if (mimeType == null) {
			return createModelFromResponse(mm, content, "text/turtle");
		} else if (mimeType.contains("application/json")) {
			return createJsonFromResponse(mm, content);
		} else if (mimeType.contains("application/xml") || mimeType.contains("text/xml")) {
			return createXMLFromResponse(mm, content);
		} else if (mimeType.contains("text/html")) {
			return createHTMLFromResponse(mm, content);
		} else if (mimeType.contains("text/csv")) {
			return createCSVFromResponse(mm, content);
		}
		return createModelFromResponse(mm, content, mimeType);
	}

	private void checkExpectedMimeType(final String value) {
		if (binding.getHeaders() == null) {
			return;
		}
		Optional<String> expected = binding
						.getHeaders()
						.stream()
						.filter(header -> "Accept".equalsIgnoreCase(header.getHeaderName().getFragment()))
						.map(header -> header.getHeaderValue())
						.findFirst();
		if (expected.isPresent() && !expected.get().equals(value)) {
			LOG.warn("Expected MIME-Type {} from Binding, but got {}. Trying to parse anyway.", expected.get(), value);
		}
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private MultivaluedMap getReadHeaders(final Header... headers) {
		MultivaluedMap<String,String> map = new MultivaluedHashMap<>();
		for (Header header: headers) {
			List list = new ArrayList<String>();
			list.add(header.getValue());
			map.put(header.getName(), list);
		}
		return map;
	}

	private Model createModelFromResponse(final MultivaluedMap mm, final InputStream response, final String entityFormat) throws AJANRequestException {
		String mime = entityFormat;
		if (entityFormat.contains("charset")) {
			mime = entityFormat.split(";")[0];
		}
		try {
			Model model = Rio.parse(response, BASE_URI, formatForMimeType(mime));
			return AgentUtil.setMessageInformation(model, mm);
		} catch (IOException | IllegalArgumentException ex) {
			throw new AJANRequestException("Error while parsing " + mime + " based response into RDF graph.", ex);
		}
	}

	private JsonNode createJsonFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String text = new String(response.readAllBytes(), StandardCharsets.UTF_8);
		LOG.info(text);
		JsonNode input =  mapper.readTree(new ByteArrayInputStream(text.getBytes()));
		return AgentUtil.setMessageInformation(input, mm);
	}

	private Document createXMLFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException, SAXException {
		Document input = AgentUtil.getXMLFromStream(response);
		return AgentUtil.setMessageInformation(input, mm);
	}

	private Document createHTMLFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException, SAXException {
		Document input = AgentUtil.getHTMLFromStream(response);
		return AgentUtil.setMessageInformation(input, mm);
	}

	private CSVInput createCSVFromResponse(final MultivaluedMap mm, final InputStream response) throws IOException {
		CSVInput input = AgentUtil.getCSVFromStream(response);
		return AgentUtil.setMessageInformation(input, mm);
	}

	@Override
	public void response(final Object info) {
		if (event != null && info instanceof Model) {
			event.setEventInformation((Model)info);
		}
	}

	@Override
	public void response(final Object info, final String id) {
		if (event != null && processIds.contains(id) && info instanceof Model) {
			LOG.info(id);
			event.setEventInformation(id, (Model)info);
		}
	}
}
