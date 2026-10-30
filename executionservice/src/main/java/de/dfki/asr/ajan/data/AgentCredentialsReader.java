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

package de.dfki.asr.ajan.data;

import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.CredentialsBuilder;
import de.dfki.asr.ajan.common.Token;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;

/**
 * Internal helper; the public facade is {@link AgentModelManager}. It reads the login credentials and tokens of a managed triple store from an agent model.
 */
final class AgentCredentialsReader {

	private final AgentModelManager modelManager;

	public AgentCredentialsReader(final AgentModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void setCredentials(final CredentialsBuilder auth, final Model model, final Resource resource) {
		Model loginModel = model.filter(resource, AJANVocabulary.AGENT_HAS_LOGIN_URL, null);
		String loginUrl = modelManager.getAnyURI(loginModel, AJANVocabulary.AGENT_HAS_LOGIN_URL);
		if (loginUrl != null && !loginUrl.isEmpty()) {
			auth.setLoginUrl(loginUrl);
		}
		Model userModel = model.filter(resource, AJANVocabulary.AGENT_HAS_USER, null);
		String user = modelManager.getString(userModel);
		if (user != null) {
			auth.setUser(user);
		}
		Model roleModel = model.filter(resource, AJANVocabulary.AGENT_HAS_ROLE, null);
		String role = modelManager.getString(roleModel);
		if (role != null) {
			auth.setRole(role);
		}
		Model pswdModel = model.filter(resource, AJANVocabulary.AGENT_HAS_PASSWORD, null);
		String pswd = modelManager.getString(pswdModel);
		if (pswd != null) {
			auth.setPassword(pswd);
		}
	}

	public void setTokens(final CredentialsBuilder auth, final Model model, final Resource resource) {
		Model tokenModel = model.filter(resource, AJANVocabulary.AGENT_HAS_ACCESS_TOKEN, null);
		Optional<Resource> accessToken = Models.objectResource(tokenModel);
		if (!accessToken.isEmpty()) {
			auth.setAccessToken(getToken(model, accessToken.get()));
		}
		tokenModel = model.filter(resource, AJANVocabulary.AGENT_HAS_REFRESH_TOKEN, null);
		Optional<Resource> refreshToken = Models.objectResource(tokenModel);
		if (!refreshToken.isEmpty()) {
			auth.setRefreshToken(getToken(model, refreshToken.get()));
		}
	}

	private Token getToken(final Model model, final Resource resource) {
		String value = modelManager.getString(model.filter(resource, AJANVocabulary.TOKEN_HAS_VALUE, null));
		String jsonField = modelManager.getString(model.filter(resource, AJANVocabulary.TOKEN_HAS_JSON_FIELD, null));
		String header = modelManager.getString(model.filter(resource, AJANVocabulary.TOKEN_HAS_HEADER, null));
		if (value == null) {
			value = "";
		}
		if (jsonField == null || header == null) {
			return null;
		}
		Token token = new Token();
		token.setValue(value);
		token.setJsonField(jsonField);
		token.setHeader(header);
		return token;
	}
}
