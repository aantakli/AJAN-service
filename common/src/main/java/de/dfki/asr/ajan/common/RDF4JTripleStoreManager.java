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

import de.dfki.asr.ajan.common.exceptions.TripleStoreException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.inferencer.fc.config.DedupingInferencerConfig;
import org.eclipse.rdf4j.sail.inferencer.fc.config.SchemaCachingRDFSInferencerConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.eclipse.rdf4j.sail.spin.config.SpinSailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("PMD.NullAssignment")
public class RDF4JTripleStoreManager implements TripleStoreManager {
	private static final Logger LOG = LoggerFactory.getLogger(RDF4JTripleStoreManager.class);
	private static final String ERROR_REPO_SETUP = "Error setting up the repository: ";
	private final AJANRepositoryManager repoManager;
	private final Credentials auth;

	public RDF4JTripleStoreManager(final URL url) {
		repoManager = new AJANRepositoryManager(url.toString());
		this.auth = null;
		repoManager.init();
	}

	public RDF4JTripleStoreManager(final URL url, final Credentials auth) {
		repoManager = new AJANRepositoryManager(url.toString(), auth);
		this.auth = auth;
		repoManager.init();
	}

	/**
	 * Creates a new repository (TripleDataBase) in the triple store. The configuration of the repository to be created consists of the following parts:<p>
	 * <b>Backend configuration: </b> A Sail implementation to be provided to the constructor of the repository, determining whether and where
	 *		to persist the repository. We use a non-persistent in memory repository here. <p>
	 * <b> Sail Repository configuration: </b> The actual type of repository to use. We use a sail repository, RDF4J's implementation for RDF repositories. <p>
	 * <b> Repository config: </b> A configuration object consisting of the configuration created so far and the ID for the database to be created.
	 *		Adding this final config object to the repository manager will instantiate a new database with the given config and ID. <p>
	 * For further information, refer to the official Repository API documentation:
	 * http://rdf4j.org/doc/programming-with-rdf4j/the-repository-api/
	 * @param tdbId The id of the database to be created
	 * @param overwrite
	 * @return	The triple database representing the new repository
	 * @throws TripleStoreException
	 */
	@Override
	public TripleDataBase createTripleDataBase(final String tdbId, final boolean overwrite) {
		return createTripleDataBase(tdbId,overwrite,Inferencing.NONE);
	}

	@Override
	public TripleDataBase createSecuredTripleDataBase(final String tdbId, final boolean overwrite) throws TripleStoreException {
		try {
			return convertToTripleDataBase(getInfos(tdbId, overwrite, Inferencing.NONE, true), auth);
		} catch (RepositoryConfigException | RepositoryException ex) {
			throw new TripleStoreException(ERROR_REPO_SETUP + ex.toString(), ex);
		}
	}

	@Override
	public TripleDataBase createTripleDataBase(final String tdbId, final boolean overwrite, final Inferencing useInferencing) throws TripleStoreException {
		try {
			return convertToTripleDataBase(getInfos(tdbId, overwrite, useInferencing, false), null);
		} catch (RepositoryConfigException | RepositoryException ex) {
			throw new TripleStoreException(ERROR_REPO_SETUP + ex.toString(), ex);
		}
	}

	@Override
	public TripleDataBase createSecuredTripleDataBase(final String tdbId, final boolean overwrite, final Inferencing useInferencing) throws TripleStoreException {
		try {
			return convertToTripleDataBase(getInfos(tdbId, overwrite, useInferencing, true), auth);
		} catch (RepositoryConfigException | RepositoryException ex) {
			throw new TripleStoreException(ERROR_REPO_SETUP + ex.toString(), ex);
		}
	}

	@Override
	public TripleDataBase createSecuredAgentTripleDataBase(final String tdbId, final String managedTDB, final Inferencing useInferencing, final Credentials agentAuth) throws TripleStoreException {
		try {
			RepositoryInfo info;
			if (managedTDB.isEmpty()) {
				info = getInfos(tdbId, true, useInferencing, true);
				repoManager.setupAgentSecurityConfiguration(agentAuth);
			} else {
				String idStr = managedTDB.substring(managedTDB.lastIndexOf('/') + 1);
				info = new RepositoryInfo();
				info.setId(idStr);
				info.setLocation(new URL(managedTDB));
			}
			return convertToTripleDataBase(info, agentAuth);
		} catch (MalformedURLException | URISyntaxException | RepositoryConfigException | RepositoryException ex) {
			throw new TripleStoreException(ERROR_REPO_SETUP + ex.toString(), ex);
		}
	}

	private RepositoryInfo getInfos(final String tdbId, final boolean overwrite, final Inferencing useInferencing, final boolean nativeStore) {
		String com = "Accessed";
		if (overwrite) {
			SailImplConfig config;
			if (nativeStore) {
				config = new NativeStoreConfig();
			} else {
				config = new MemoryStoreConfig();
			}
			createRemoteRepositiories(tdbId,useInferencing, config);
			com = "Created";
		}
		RepositoryInfo info = repoManager.getRepositoryInfo(tdbId);
		LOG.info(com + " TDB with ID {} at {}", tdbId, getSparqlEndpoint(info, ""));
		return info;
	}

	private void createRemoteRepositiories(final String tdbId, final Inferencing useInferencing, final SailImplConfig cofig) {
		switch (useInferencing) {
			case RDFS:
				createRemoteRDFSRepository(tdbId, cofig);
				break;
			case SPIN:
				createRemoteSPINRepository(tdbId, cofig);
				break;
			case RDFS_SPIN:
				createRemoteRDFSSPINRepository(tdbId, cofig);
				break;
			default:
				createRemoteRepository(tdbId, cofig);
				break;
		}
	}

	private void createRemoteRepository(final String tdbId, final SailImplConfig cofig) throws RepositoryConfigException, RepositoryException {
		SailRepositoryConfig repositoryTypeSpec = new SailRepositoryConfig(cofig);
		addRepositoryConfig(tdbId, repositoryTypeSpec);
	}

	private void createRemoteRDFSRepository(final String tdbId, final SailImplConfig cofig) throws RepositoryConfigException, RepositoryException {
		SchemaCachingRDFSInferencerConfig rdfsConfig = new SchemaCachingRDFSInferencerConfig(cofig);
		addRepositoryConfig(tdbId, new SailRepositoryConfig(rdfsConfig));
	}

	private void createRemoteSPINRepository(final String tdbId, final SailImplConfig cofig) throws RepositoryConfigException, RepositoryException {
		SailImplConfig spinConfig = new SpinSailConfig(cofig);
		addRepositoryConfig(tdbId, new SailRepositoryConfig(spinConfig));
	}

	private void createRemoteRDFSSPINRepository(final String tdbId, final SailImplConfig cofig) throws RepositoryConfigException, RepositoryException {
		SailImplConfig spinSailConfig = new SpinSailConfig(
						new SchemaCachingRDFSInferencerConfig(
								new DedupingInferencerConfig(cofig)
			)
		);
		addRepositoryConfig(tdbId, new SailRepositoryConfig(spinSailConfig));
	}

	private void addRepositoryConfig(final String tdbId, final SailRepositoryConfig repositoryTypeSpec) {
		RepositoryConfig config = new RepositoryConfig(tdbId, repositoryTypeSpec);
		config.setID(tdbId);
		repoManager.addRepositoryConfig(config);
	}

	private TripleDataBase convertToTripleDataBase(final RepositoryInfo info, final Credentials auth) {
		try {
			if (auth != null) {
				return new RDF4JTripleDataBase(info, auth);
			}
			return new RDF4JTripleDataBase(info);
		} catch (MalformedURLException ex) {
			throw new TripleStoreException("RDF4J is returning invalid URLs", ex);
		}
	}

	private URL getSparqlEndpoint(final RepositoryInfo info, final String postfix) {
		URL repoLocation = info.getLocation();
		try {
			return new URL(repoLocation.toString() + postfix);
		} catch (MalformedURLException ex) {
			throw new TripleStoreException("The location of the repository should have been valid.", ex);
		}
	}

	@Override
	public Set<TripleDataBase> getAllTripleDataBases() {
		Collection<RepositoryInfo> repoInfos = repoManager.getAllRepositoryInfos(true);
		return repoInfos.stream()
			.map(info -> convertToTripleDataBase(info, null))
			.collect(Collectors.toSet());
	}

	@Override
	public void removeTripleDataBase(final TripleDataBase db) throws TripleStoreException {
		String id = db.getId();
		LOG.info("Removing TDB with ID {}", id);
		if (!repoManager.isInitialized()) {
			repoManager.initialize();
		}
		repoManager.removeAgentSecurityConfiguration(id);
		repoManager.removeRepository(id);
	}
}
