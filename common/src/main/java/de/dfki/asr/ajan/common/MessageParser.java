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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.xerces.parsers.DOMParser;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Internal helper; the public facade is {@link AgentUtil}. It parses JSON, XML, HTML and CSV message bodies.
 */
final class MessageParser {

  private MessageParser() {}

  public static JsonNode getJsonFromStream(final InputStream is) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readTree(is);
  }

  public static Document getXMLFromStream(final InputStream is) throws SAXException, IOException {
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(is));
    return parser.getDocument();
  }

  public static Document getHTMLFromStream(final InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = is.read(buffer);
    while (length != -1) {
      result.write(buffer, 0, length);
      length = is.read(buffer);
    }
    String html = result.toString(StandardCharsets.UTF_8.name());
    W3CDom w3cDom = new W3CDom();
    return w3cDom.fromJsoup(Jsoup.parse(html, Parser.xmlParser()));
  }

  public static CSVInput getCSVFromStream(final InputStream is) throws IOException {
    CSVInput input;
    try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
      input = new CSVInput();
      input.setContent(reader.readAll());
    } catch (CsvException e) {
      throw new IOException("Malformed CSV input", e);
    }
    return input;
  }
}
