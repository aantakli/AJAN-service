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

import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFBean("http-core:Request")
public class HttpBinding {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("http-core:requestURI")
	@Getter @Setter
	private URI requestURI;

	@RDF("http-core:httpVersion")
	@Getter @Setter
	private String version;

	@RDF("http-core:mthd")
	@Getter @Setter
	private URI method;

	@RDF("http-core:headers")
	@Getter @Setter
	private List<HttpHeader> headers;

	@RDF("http-core:body")
	@Getter @Setter
	private ActnPayload payload;
}
