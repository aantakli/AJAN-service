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

package de.dfki.asr.ajan.behaviour.load;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.branch.Executor;
import de.dfki.asr.ajan.behaviour.nodes.common.Debug;
import de.dfki.asr.ajan.common.AbstractSparqlTripleDataBase;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.common.TripleStoreManager.Inferencing;
import de.dfki.asr.ajan.knowledge.*;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SuppressWarnings("PMD.ExcessiveImports")
public class UnmarshalBehaviourTest {
	private TripleDataBase agentTemplatesTDB;
	private TripleDataBase behaviorTDB;
	private TripleDataBase beliefsTDB;
	private TripleDataBase domainTDB;
	private TripleDataBase servicesTDB;
	private BehaviorTree<AgentTaskInformation> root;
	private List<NodeExtension> extensions;

	public static class MockTDB extends AbstractSparqlTripleDataBase {
		private final SailRepository inMem = new SailRepository(new MemoryStore());

		public MockTDB(final String id) throws MalformedURLException {
			super(id, new URL("http://example.com/"), new URL("http://example.com/"));
		}

		@Override
		public Repository getInitializedRepository() {
			inMem.initialize();
			return inMem;
		}
	}

	@BeforeClass
	public void loadBahviour() throws IOException {
		behaviorTDB = new MockTDB("behaviorMock");
		behaviorTDB.add(getClass().getResourceAsStream("behaviors.ttl"), RDFFormat.TURTLE);
	}

	@BeforeClass
	public void loadKnowledge() throws IOException {
		beliefsTDB = new MockTDB("knowledgeMock");
		beliefsTDB.add(getClass().getResourceAsStream("beliefs.ttl"), RDFFormat.TURTLE);
	}

	@BeforeClass
	public void loadDomain() throws IOException {
		domainTDB = new MockTDB("domainMock");
		domainTDB.add(getClass().getResourceAsStream("domain.ttl"), RDFFormat.TURTLE);
	}

	@BeforeClass
	public void loadService() throws IOException {
		servicesTDB = new MockTDB("serviceMock");
		servicesTDB.add(getClass().getResourceAsStream("services.ttl"), RDFFormat.TURTLE);
	}

	@Test
	public void loadRoot() throws RDFBeanException {
		Repository behaviourRepo = behaviorTDB.getInitializedRepository();
		try (RepositoryConnection conn = behaviourRepo.getConnection()) {
			AJANPluginLoader pluginLoader = new AJANPluginLoader();
			extensions = pluginLoader.getNodeExtensions();
			RDFBeanManager manager = new BehaviorBeanManager(conn, extensions);
			String rootIRI = "http://localhost:8090/rdf4j/repositories/behaviors#ConditionalLoggingBehavior";
			root = manager.get(behaviourRepo.getValueFactory().createIRI(rootIRI), BTRoot.class);
		}
		assertThat(root.getChildCount(), is(1));
		assertThat(root.getChild(0), is(instanceOf(Executor.class)));
	}

	@Test(dependsOnMethods = {"loadRoot"})
	public void rootCanStep() {
		Map<URI,Event> events = new ConcurrentHashMap();
		AgentTaskInformation info = new AgentTaskInformation(new AJANLogger("agent_1"), new BTRoot(), true, new AgentBeliefBase(beliefsTDB), new ExecutionBeliefBase(Inferencing.NONE), agentTemplatesTDB, behaviorTDB, domainTDB, servicesTDB, events, new ConcurrentHashMap<>(), extensions, new LinkedHashMap(), "", new Debug());
		root.setObject(info);
		root.step();
	}
}
