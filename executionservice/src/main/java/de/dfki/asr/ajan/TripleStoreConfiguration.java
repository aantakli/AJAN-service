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

import de.dfki.asr.ajan.common.Credentials;
import de.dfki.asr.ajan.common.RDF4JTripleStoreManager;
import de.dfki.asr.ajan.common.TripleStoreManager;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TripleStoreConfiguration {
	@Value("${triplestore.url:http://localhost:8090/rdf4j}")
	private String tripleStoreURL;

        @Value("${triplestore.user:}")
	private String user;

        @Value("${triplestore.pswd:}")
	private String pswd;

	@Bean
	public TripleStoreManager createManager() throws MalformedURLException {
            if (user != null && !user.isEmpty() && pswd != null && !pswd.isEmpty()) {
                return new RDF4JTripleStoreManager(new URL(tripleStoreURL), new Credentials(user, pswd));
            }
            return new RDF4JTripleStoreManager(new URL(tripleStoreURL));
	}
}
