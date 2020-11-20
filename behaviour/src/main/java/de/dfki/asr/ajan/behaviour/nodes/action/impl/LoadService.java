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

package de.dfki.asr.ajan.behaviour.nodes.action.impl;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.rdfbeans.BehaviorBeanManager;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ServiceActionDefinition;

public class LoadService extends AbstractChainStep {
	private static final Logger LOG = LoggerFactory.getLogger(LoadService.class);

	public LoadService(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		try {
			Action action = context.get(Action.class);
			AgentTaskInformation taskInfo = action.getObject();
			Repository repo = taskInfo.getServiceTDB().getInitializedRepository();
			ServiceActionDefinition service;
			try (RepositoryConnection conn = repo.getConnection()) {
				RDFBeanManager manager = new BehaviorBeanManager(conn);
				String rootIRI = action.getDefinition().toString();
				service = manager.get(repo.getValueFactory().createIRI(rootIRI), ServiceActionDefinition.class);
			}
			context.put(service);
		} catch (RDFBeanException | RDF4JException ex) {
			LOG.error("Failed to load Service", ex);
			return Task.Status.FAILED;
		}
		return executeNext(context);
	}
}
