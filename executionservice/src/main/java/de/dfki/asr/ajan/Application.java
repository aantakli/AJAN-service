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

package de.dfki.asr.ajan;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Application.class);
    /**
     * RESTEasy 6 registriert per Default einen eingebauten ExceptionMapper&lt;Throwable&gt;
     * (DefaultExceptionMapper). Er faengt die Ursache einer ReaderException (z. B. eine
     * RDFParseException aus RDFConsumer#readFrom) und liefert 500 statt - wie RESTEasy 3 -
     * 400 + text/html + Exception.toString(); unbehandelte Fehler landen dort statt auf der
     * Boot-Fehlerseite. Abschalten stellt das RESTEasy-3-Verhalten wieder her. Muss eine
     * System-Property sein: der Starter baut die ProviderFactory beim Spring-Start, ausserhalb
     * eines Servlet-Kontexts, sodass Context-/Init-Parameter dort nicht gelesen werden.
     */
    private static final String RESTEASY_DEFAULT_EXCEPTION_MAPPER = "dev.resteasy.exception.mapper";

    static {
        // Static initializer instead of main(): every launcher that loads this class (main(),
        // @SpringBootTest, IDE runners) gets the same RESTEasy behaviour, and it runs before
        // Spring builds the ProviderFactory.
        if (System.getProperty(RESTEASY_DEFAULT_EXCEPTION_MAPPER) == null) {
            System.setProperty(RESTEASY_DEFAULT_EXCEPTION_MAPPER, "false");
        }
    }

    public static void main(final String... args) {
        printVersion();
        SpringApplication.run(Application.class, args);
    }

    private static void printVersion(){
        String version = Optional.ofNullable(System.getenv().get("VERSION")).orElse("local");
        String startMessage = "| Starting AJAN Execution Service version " + version + " |";
        String border = "=".repeat(startMessage.length());
        LOG.info(border);
        LOG.info(startMessage);
        LOG.info(border);
    }
}
