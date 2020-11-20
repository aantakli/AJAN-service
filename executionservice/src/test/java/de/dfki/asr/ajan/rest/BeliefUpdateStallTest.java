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

package de.dfki.asr.ajan.rest;

import de.dfki.asr.ajan.Application;
import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.model.Agent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;


@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class BeliefUpdateStallTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private AgentManager manager;

	private AgentResource resource;
	private Model knowledgeUpdate;
	private static Integer threadCount = 0;
	private static final Object LOCK = new Object();

	@PostConstruct
	public void createAgent() throws IOException, URISyntaxException {
		Model initAgentModel = modelFromResource("agent.ttl");
		Agent ourAgent = manager.createAgent(initAgentModel);
		resource = new AgentResource(ourAgent, manager);
		knowledgeUpdate = new LinkedHashModel();
		ValueFactory vf = SimpleValueFactory.getInstance();
		knowledgeUpdate.add(vf.createIRI("http://example.com"), vf.createIRI("urn:ajan:fact"), vf.createLiteral("boring"));
	}

	@Test(invocationCount = 100, threadPoolSize = 10)
	public void shouldExecuteAgentAsynchronous() {
		assertThat(resource, not(nullValue()));
		resource.stringPostAgent("action", null);
	}

	@Test(invocationCount = 5)
	public void shouldUpdateBeliefsSynchronous() {
		assertThat(resource, not(nullValue()));
		assertThat(knowledgeUpdate, not(nullValue()));
		resource.modelPostAgent("update", knowledgeUpdate);
	}

	@Test(dependsOnMethods = "shouldUpdateBeliefsSynchronous", invocationCount = 100, threadPoolSize = 10)
	public void shouldUpdateBeliefsAsynchronous() {
		Thread.currentThread().setName("Test " + getThreadId());
		assertThat(resource, not(nullValue()));
		assertThat(knowledgeUpdate, not(nullValue()));
		resource.modelPostAgent("update", knowledgeUpdate);
	}

	private Model modelFromResource(final String agentttl) throws IOException {
		InputStream in = getClass().getResourceAsStream(agentttl);
		return Rio.parse(in, agentttl, RDFFormat.TURTLE);
	}

	private static String getThreadId() {
		synchronized (LOCK) {
			threadCount++;
			return threadCount.toString();
		}
	}
}
