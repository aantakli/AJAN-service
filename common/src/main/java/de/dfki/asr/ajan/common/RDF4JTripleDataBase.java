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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;

public class RDF4JTripleDataBase extends SparqlTripleDataBase {
	private final HTTPRepository repo;
	private final Credentials auth;

	public RDF4JTripleDataBase(final RepositoryInfo info) throws MalformedURLException {
		super(info.getId(), info.getLocation(), new URL(info.getLocation().toString() + "/statements"));
		repo = new HTTPRepository(info.getLocation().toString());
		auth = null;
	}

	public RDF4JTripleDataBase(final RepositoryInfo info, final Credentials auth) throws MalformedURLException {
		super(info.getId(), info.getLocation(), new URL(info.getLocation().toString() + "/statements"));
		repo = new HTTPRepository(info.getLocation().toString());
		this.auth = auth;
	}

	@Override
	public Repository getInitializedRepository() {
		if (auth != null) {
			repo.setAdditionalHttpHeaders(getToken());
		}
		if (!repo.isInitialized()) {
			repo.init();
		}
		return repo;
	}

	private Map<String,String> getToken() {
		return auth.getJwtHeader();
	}
}
