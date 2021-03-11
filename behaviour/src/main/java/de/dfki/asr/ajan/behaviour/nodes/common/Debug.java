/*
 * Copyright (C) 2020 see André Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes.common;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.DebugMode;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class Debug {
	private DebugMode mode;
	private String btURI;
	private boolean debugging;

	public Debug() {
		this.mode = DebugMode.NONE;
		this.btURI = "";
		this.debugging = false;
	}

	public void setMode(final DebugMode mode) {
		this.mode = mode;
	}

	public void setBtURI(final String uri) {
		this.btURI = uri;
	}

	public void setDebugging(final boolean debugging) {
		this.debugging = debugging;
	}

	public DebugMode getMode() {
		return this.mode;
	}

	public boolean isDebugging() {
		return this.debugging;
	}

	public String getBtURI() {
		return this.btURI;
	}
}
