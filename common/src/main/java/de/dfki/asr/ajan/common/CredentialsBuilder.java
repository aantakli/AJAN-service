/*
 * Copyright (C) 2022 anan02-admin.
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

import de.dfki.asr.ajan.common.exceptions.CredentialsException;

/**
 *
 * @author anan02-admin
 */
@SuppressWarnings({"PMD.CyclomaticComplexity","PMD.ConfusingTernary"})
public class CredentialsBuilder {

	private String usersUrl = "";
	private String constraintUrl = "";
	private String loginUrl = "";
	private String user = "";
	private String role = "";
	private String password = "";
	private Token accessToken;
	private Token refreshToken;

	public CredentialsBuilder setUsersUrl(final String url) {
		this.usersUrl = url;
		return this;
	}

	public CredentialsBuilder setConstraintUrl(final String url) {
		this.constraintUrl = url;
		return this;
	}

	public CredentialsBuilder setLoginUrl(final String url) {
		this.loginUrl = url;
		return this;
	}

	public CredentialsBuilder setUser(final String user) {
		this.user = user;
		return this;
	}

	public CredentialsBuilder setRole(final String role) {
		this.role = role;
		return this;
	}

	public CredentialsBuilder setPassword(final String password) {
		this.password = password;
		return this;
	}

	public CredentialsBuilder setAccessToken(final Token accessToken) {
		this.accessToken = accessToken;
		return this;
	}

	public CredentialsBuilder setRefreshToken(final Token refreshToken) {
		this.refreshToken = refreshToken;
		return this;
	}

	public Credentials build() {
		if (checkCredentialsFields()) {
			return new Credentials(usersUrl, constraintUrl, loginUrl, user, role, password, accessToken, refreshToken);
		}
		return null;
	}

	private boolean checkCredentialsFields() {
		if (checkCredentialsCreationFields()) {
			return true;
		} else if (checkLoginFields()) {
			return true;
		} else if (accessToken != null && refreshToken != null) {
			return true;
		}
		return false;
	}

	private boolean checkCredentialsCreationFields() {
		checkMissingCredentials();
		checkMissingServerInformation();
		return !usersUrl.isEmpty() && !constraintUrl.isEmpty() && !loginUrl.isEmpty()
						&& !user.isEmpty() && !role.isEmpty() && !password.isEmpty();
	}

	private void checkMissingCredentials() {
		if (!usersUrl.isEmpty() && !constraintUrl.isEmpty() && (loginUrl.isEmpty()
						|| user.isEmpty() || role.isEmpty() || password.isEmpty())) {
			throw new CredentialsException("Server information for agent registration are set, but agent credentails are missing!");
		}
	}

	private void checkMissingServerInformation() {
		if (usersUrl.isEmpty() && !constraintUrl.isEmpty()
						|| !usersUrl.isEmpty() && constraintUrl.isEmpty()) {
			throw new CredentialsException("Server information for agent registration are incomplete!");
		}
	}

	private boolean checkLoginFields() {
		checkSingleMissingfield();
		checkDoubleMissingfield();
		return !loginUrl.isEmpty() && !user.isEmpty() && !password.isEmpty();
	}

	private void checkSingleMissingfield() {
		if (loginUrl.isEmpty() && !user.isEmpty() && !password.isEmpty()
						|| !loginUrl.isEmpty() && user.isEmpty() && !password.isEmpty()
						|| !loginUrl.isEmpty() && !user.isEmpty() && password.isEmpty()) {
			throw new CredentialsException("Agent login information is incomplete!");
		}
	}

	private void checkDoubleMissingfield() {
		if (loginUrl.isEmpty() && user.isEmpty() && !password.isEmpty()
						|| !loginUrl.isEmpty() && user.isEmpty() && password.isEmpty()
						|| loginUrl.isEmpty() && !user.isEmpty() && password.isEmpty()) {
			throw new CredentialsException("Agent login information is incomplete!");
		}
	}
}
