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

import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.common.TripleStoreManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import static de.dfki.asr.ajan.AJANDataBase.Store.*;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;

@Component
public class Repositories {
	@Autowired
	private TripleStoreManager tripleStoreManager;

	private TripleDataBase agentTemplates;
	private TripleDataBase behaviors;
	private TripleDataBase services;
	private TripleDataBase domain;

	@Value("${loadTTLFiles:true}")
	private boolean loadFiles;

        @Value("${triplestore.user:}")
	private String user;

        @Value("${triplestore.role:}")
	private String role;

        @Value("${triplestore.pswd:}")
	private String pswd;

	@Bean
	@AJANDataBase(AGENT_TEMPLATE)
	public TripleDataBase getAgentTemplates() {
		if (agentTemplates == null) {
                    agentTemplates = createStore("agents");
		}
		return agentTemplates;
	}

	@Bean
	@AJANDataBase(BEHAVIOR)
	public TripleDataBase getBehaviors() {
		if (behaviors == null) {
                    behaviors = createStore("behaviors");
                }
		return behaviors;
	}

	@Bean
	@AJANDataBase(DOMAIN)
	public TripleDataBase getDomain() {
		if (domain == null) {
                    domain = createStore("domain");
                }
		return domain;
	}

	@Bean
	@AJANDataBase(ACTION_SERVICE)
	public TripleDataBase getServices() {
		if (services == null) {
                    services = createStore("services");
                }
		return services;
	}

	@PreDestroy
	public void cleanUp() {
		if (services != null) {
			tripleStoreManager.removeTripleDataBase(services);
		}
		if (behaviors != null) {
			tripleStoreManager.removeTripleDataBase(behaviors);
		}
		if (agentTemplates != null) {
			tripleStoreManager.removeTripleDataBase(agentTemplates);
		}
                if (domain != null) {
			tripleStoreManager.removeTripleDataBase(domain);
		}
	}

        private TripleDataBase createStore(final String repoName) {
            if (user == null || user.isEmpty() || role == null || role.isEmpty() || pswd == null || pswd.isEmpty()) {
                return tripleStoreManager.createTripleDataBase(repoName, loadFiles);
            }
            return tripleStoreManager.createSecuredTripleDataBase(repoName, loadFiles);
        }
}
