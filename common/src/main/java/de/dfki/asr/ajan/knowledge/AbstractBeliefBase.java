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

package de.dfki.asr.ajan.knowledge;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.util.Repositories;

public abstract class AbstractBeliefBase {
	private final Object BELIEFBASE_LOCK = new Object();

	public abstract Repository initialize();

	public void update(final Model model) {
		update(model, model, false);
	}

	public void update(final Model add, final Model remove, final boolean mode) {
		synchronized (BELIEFBASE_LOCK) {
			Repository repo = initialize();
			try (RepositoryConnection connection = repo.getConnection()) {
				connection.begin(IsolationLevels.SERIALIZABLE);
				removeStatementsSupersededBy(remove, connection, mode);
				addStatementsWith(add,connection);
				connection.commit();
			}
		}
	}

	@SuppressWarnings("PMD.UselessParentheses")
	private void removeStatementsSupersededBy(final Model model, final RepositoryConnection connection, final boolean mode) {
		model.stream().filter((stmt) -> (!(stmt.getSubject() instanceof BNode) || (stmt.getSubject() instanceof BNode) && mode)).forEachOrdered((stmt) -> {
			connection.remove(stmt.getSubject(), stmt.getPredicate(), null, (Resource) stmt.getContext());
		});
	}

	private void addStatementsWith(final Model model, final RepositoryConnection connection) {
		model.forEach((stmt) -> {
			connection.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), (Resource) stmt.getContext());
		});
	}

	public Model asModel() {
		synchronized (BELIEFBASE_LOCK) {
			Model model = new LinkedHashModel();
			Repository repo = initialize();
			Repositories.consume(repo, conn -> {
				RepositoryResult<Statement> results = conn.getStatements(null, null, null);
				Iterations.addAll(results, model);
			});
			return model;
		}
	}
}
