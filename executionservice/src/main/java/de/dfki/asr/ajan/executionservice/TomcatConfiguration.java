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

import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat-11-Defaults auf Undertow-1.3-Paritaet (Etappe 4, Ruling R10).
 *
 * <p>Tomcat lehnt ein kodiertes Slash ({@code %2F}) im Pfad per Default mit 400 ab
 * ({@code encodedSolidusHandling=reject}); Undertow 1.3 reichte es durch. Mit
 * {@code passthrough} bleibt {@code %2F} im Pfad kodiert, RESTEasy matcht das rohe
 * Segment und dekodiert erst den {@code @PathParam}. Spring Boot 4.1.2 bietet dafuer
 * keine {@code server.tomcat.*}-Property (geprueft in der Konfigurations-Metadaten von
 * spring-boot-tomcat 4.1.2), deshalb ein Connector-Customizer. Gepinnt durch
 * {@code WebSurfaceIT#encodedSlashInAgentNameIsNotRejected}.
 */
@Configuration
public class TomcatConfiguration {

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> encodedSolidusPassthrough() {
		return factory -> factory.addConnectorCustomizers(connector ->
				connector.setEncodedSolidusHandling(EncodedSolidusHandling.PASS_THROUGH.getValue()));
	}
}
