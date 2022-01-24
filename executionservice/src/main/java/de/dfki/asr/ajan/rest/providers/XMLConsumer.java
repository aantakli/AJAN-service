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

import de.dfki.asr.ajan.common.AgentUtil;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Provider
@Consumes({"application/xml", "text/xml"})
@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.AvoidPrefixingMethodParameters"})
@Component
public class XMLConsumer implements MessageBodyReader<Document> {

    @Override
    public boolean isReadable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        if (!type.isAssignableFrom(Document.class)) {
            return false;
        }
        return mt.toString().equals("application/xml") || mt.toString().equals("text/xml") ;
    }

    @Override
    public Document readFrom(final Class<Document> t, final Type type, final Annotation[] annts, final MediaType mt, final MultivaluedMap<String, String> mm, final InputStream in) throws IOException, WebApplicationException {
        if (!mt.toString().equals("application/xml") && !mt.toString().equals("text/xml")) {
            createErrorMsg(mt);
        }
        try {
            return AgentUtil.getXMLFromStream(in);
        }
        catch (SAXException ex) {
            createErrorMsg(mt);
        }
        return null;
    }

    private void createErrorMsg(final MediaType mt) {
        String msg = "Can not consume XML of mimetype + " + mt.toString();
        Response response = Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(msg).build();
        throw new WebApplicationException(response);
    }

}
