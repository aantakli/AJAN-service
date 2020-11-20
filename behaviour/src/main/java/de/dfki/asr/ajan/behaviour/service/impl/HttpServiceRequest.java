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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

public class HttpServiceRequest extends HttpEntityEnclosingRequestBase {
	public static final String OUTPUT_MIME_TYPE = "text/turtle";
	protected String mimeType = OUTPUT_MIME_TYPE;
	protected final HttpBinding binding;
	protected String payload;

	public HttpServiceRequest(final HttpBinding binding) {
		this.binding = binding;
		importProtocolVersion();
		buildHeaders();
		importTarget();
	}

	private void buildHeaders() {
		if (binding.getHeaders() == null) {
			setDefaultHeaders();
		} else {
			setHeadersFromBinding();
		}
	}

	private void setHeadersFromBinding() {
		binding.getHeaders().forEach(header -> {
			String headerName = header.getHeaderName().getFragment();
			if ("Content-Type".equalsIgnoreCase(headerName)) {
				// is handled later in the process via our Entity
				mimeType = header.getHeaderValue();
			} else {
				setHeader(headerName, header.getHeaderValue());
			}
		});
	}

	private void setDefaultHeaders() {
		setHeader("Accept", "text/turtle");
	}

	public void setEntity(final String payload) {
		this.payload = payload;
	}

	@Override
	public HttpEntity getEntity() {
		if (payload == null) {
			return null;
		}
		try {
			// the entity needs to be buffered server-side, since we want to support HTTP 1.0,
			// which requires the size of the content to be known before formulating the request.
			return new ByteArrayEntity(payload.getBytes(StandardCharsets.UTF_8.displayName()), ContentType.create(mimeType));
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	private void importProtocolVersion() {
		String boundVersion = binding.getVersion();
		if (boundVersion == null || boundVersion.equals("")) {
			setProtocolVersion(HttpVersion.HTTP_1_1);
			return;
		}
		String[] versionString = boundVersion.split("\\.");
		if (versionString.length != 2) {
			throw new IllegalArgumentException("Only Major.Minor HTTP versions are supported");
		}
		setProtocolVersion(new ProtocolVersion("HTTP",
						Integer.parseInt(versionString[0]),
						Integer.parseInt(versionString[1])));
	}

	@Override
	public String getMethod() {
		return binding.getMethod().getFragment().toUpperCase(Locale.ROOT);
	}

	private void importTarget() {
		setURI(binding.getRequestURI());
	}
}
