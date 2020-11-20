/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package org.cyberborean.rdfbeans.datatype;

import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MultipleTypeTest {
	private SailRepository repo;
	protected List<IRI> triedTypes = new ArrayList<>();
	private RDFBeanManager manager;

	private class CheckBeanManager extends RDFBeanManager {
		public CheckBeanManager(RepositoryConnection conn) {
			super(conn);
		}

		@Override
		protected Class<?> getBindingClassForType(IRI rdfType) throws RDFBeanException, RepositoryException {
			triedTypes.add(rdfType);
			return super.getBindingClassForType(rdfType);
		}
	}

	@Before
	public void setupManager() throws Exception {
		repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection initialFillConn = repo.getConnection();
		initialFillConn.add(getClass().getResourceAsStream("listUnmarshal.ttl"), "", RDFFormat.TURTLE);
		initialFillConn.close();
		manager = new CheckBeanManager(repo.getConnection());
	}

	@Test
	public void shouldTryAllTypes() {
		triedTypes.clear();
		try {
			manager.get(repo.getValueFactory().createIRI("http://example.com/list/dataClass"));
			throw new AssertionError("Should have thrown by now");
		} catch (RDFBeanException e) {
			assert(e.getMessage().startsWith("Cannot detect a binding class"));
		}
		assertThat(triedTypes.size(), is(2));
	}
}
