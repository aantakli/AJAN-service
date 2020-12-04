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

package de.dfki.asr.ajan.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.Data;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class SparqlTripleDataBase implements TripleDataBase {

	private final String id;

	private final URL sparqlEndpoint;

	private final URL sparqlUpdateEndpoint;

	private static final Logger LOG = LoggerFactory.getLogger(SparqlTripleDataBase.class);

	@Override
	public void add(final Model model) {
		Repository repo = getInitializedRepository();
		Repositories.consume(repo, conn -> conn.add(model));
	}

	@Override
	public void add(final InputStream stream, final RDFFormat format) throws IOException {
		Repository repo = getInitializedRepository();
		Repositories.consume(repo, conn -> {
			try {
				conn.add(stream, "http://www.ajan.de/", format);
			} catch (IOException | RDFParseException | RepositoryException ex) {
				LOG.debug("No known RDFFormat is used!", ex);
			}
		});
	}

	@Override
	public void clear() {
		Repository repo = getInitializedRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.clear();
		}
	}

	@Override
	public Repository getInitializedRepository() {
		Repository repository = new SPARQLRepository(sparqlEndpoint.toString(), sparqlUpdateEndpoint.toString());
		repository.initialize();
		return repository;
	}
}
