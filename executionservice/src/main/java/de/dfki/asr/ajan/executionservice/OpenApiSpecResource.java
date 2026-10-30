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

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Serves the OpenAPI 3 description of the AJAN REST resources at /ajan/swagger.json.
 * Only this resource is registered (Spring component), so swagger-core's own
 * OpenApiResource (/openapi.json) is not exposed.
 */
@Component
@Path("/swagger.json")
public class OpenApiSpecResource extends BaseOpenApiResource {
	private static final String RESOURCE_PACKAGE = "de.dfki.asr.ajan.rest";

	public OpenApiSpecResource() {
		final OpenAPI api = new OpenAPI().servers(List.of(new Server().url(ExecutionServiceApplication.BASE_PATH)));
		setOpenApiConfiguration(new SwaggerConfiguration().openAPI(api).resourcePackages(Set.of(RESOURCE_PACKAGE)));
	}

	@GET
	@Produces("application/json")
	public Response getSpec(@Context final HttpHeaders headers, @Context final ServletConfig config,
			@Context final UriInfo uriInfo) {
		try {
			// No Application: swagger-core would prefix every path with /ajan; the base path
			// is declared once as the server URL instead (paths stay "/agents", as before).
			return getOpenApi(headers, config, null, uriInfo, "json");
		} catch (Exception e) { // NOPMD - swagger-core declares a plain Exception
			throw new WebApplicationException("Cannot build the OpenAPI description", e);
		}
	}
}
