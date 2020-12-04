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

import de.dfki.asr.ajan.common.TripleDataBase;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;

public class AgentBeliefBase extends AbstractBeliefBase implements TripleDataBase {
	private final TripleDataBase beliefTripleDataBase;

	public AgentBeliefBase(final TripleDataBase backend) {
		beliefTripleDataBase = backend;

	}

	@Override
	public Repository initialize() {
		return beliefTripleDataBase.getInitializedRepository();
	}

	@Override
	public void add(final Model model) {
		beliefTripleDataBase.add(model);
	}

	@Override
	public void add(final InputStream stream, final RDFFormat format) throws IOException {
		beliefTripleDataBase.add(stream, format);
	}

	@Override
	public void clear() {
		beliefTripleDataBase.clear();
	}

	@Override
	public Repository getInitializedRepository() {
		return beliefTripleDataBase.getInitializedRepository();
	}

	@Override
	public URL getSparqlEndpoint() {
		return beliefTripleDataBase.getSparqlEndpoint();
	}

	@Override
	public URL getSparqlUpdateEndpoint() {
		return beliefTripleDataBase.getSparqlUpdateEndpoint();
	}

	@Override
	public String getId() {
		return beliefTripleDataBase.getId();
	}
}
