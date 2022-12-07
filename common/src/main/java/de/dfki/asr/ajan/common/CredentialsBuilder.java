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

/**
 *
 * @author anan02-admin
 */
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
		return new Credentials(usersUrl, constraintUrl, loginUrl, user, role, password, accessToken, refreshToken);
	}
}
