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

package de.dfki.asr.ajan.executionservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Serves the OpenAPI 3 description of the AJAN REST resources at /ajan/swagger.json.
 * The spec is built once from the resource package and cached as an immutable string.
 * Only this resource is registered (Spring component), so swagger-core's own
 * OpenApiResource (/openapi.json) is not exposed. It does not extend BaseOpenApiResource
 * because that class's getOpenApi(...) declares "throws Exception"; Scanner and Reader are
 * used directly instead. No Application is involved, so paths stay "/agents" and the base
 * path is declared once as the server URL.
 */
@Component
@Path("/swagger.json")
public class OpenApiSpecResource {
	private static final String RESOURCE_PACKAGE = "de.dfki.asr.ajan.rest";

	private final String spec;

	public OpenApiSpecResource() {
		spec = buildSpec();
	}

	private static SwaggerConfiguration configuration() {
		final OpenAPI api = new OpenAPI().servers(List.of(new Server().url(ExecutionServiceApplication.BASE_PATH)));
		return new SwaggerConfiguration().openAPI(api).resourcePackages(Set.of(RESOURCE_PACKAGE));
	}

	private static String buildSpec() {
		final SwaggerConfiguration config = configuration();
		final JaxrsAnnotationScanner<?> scanner = new JaxrsAnnotationScanner<>();
		scanner.setConfiguration(config);
		try {
			return Json.mapper().writeValueAsString(new Reader(config).read(scanner.classes()));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize the OpenAPI description", e);
		}
	}

	@GET
	@Produces("application/json")
	public String getSpec() {
		return spec;
	}
}
