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

package de.dfki.asr.ajan.rest.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

@Provider
@Consumes("application/json")
@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.AvoidPrefixingMethodParameters"})
@Component
public class JsonConsumer implements MessageBodyReader<JsonNode> {

	@Override
	public boolean isReadable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
		if (!type.isAssignableFrom(ObjectNode.class)) {
			return false;
		}
		return mt.toString().equals("application/json");
	}

	@Override
	public JsonNode readFrom(final Class<JsonNode> t, final Type type, final Annotation[] annts, final MediaType mt, final MultivaluedMap<String, String> mm, final InputStream in) throws IOException, WebApplicationException {
		if (!mt.toString().equals("application/json")) {
			String msg = "Can not consume Json of mimetype + " + mt.toString();
			Response response = Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(msg).build();
			throw new WebApplicationException(response);
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(in);
	}

}
