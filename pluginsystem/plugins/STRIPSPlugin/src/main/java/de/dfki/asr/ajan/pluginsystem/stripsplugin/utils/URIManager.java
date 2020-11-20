/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.utils;

import java.util.HashMap;
import java.util.Map;

public class URIManager {

	private static final String OBJ = "obj_";
	private static final String VAR = "Var_";
	private static final String ACT = "act_";

	private final Map<String, String> uriMap = new HashMap();

	public URIManager() {}

	public String getActSignatureHash(Object o) {
		String hash = generateHashCode(ACT, o);
		return hash;
	}

	public String setActSignatureHash(Object o) {
		String hash = generateHashCode(ACT, o);
		addHashURIPair(hash, o);
		return hash;
	}

	public String setObjTermHash(Object o) {
		String hash = generateHashCode(OBJ, o);
		addHashURIPair(hash, o);
		return hash;
	}

	public String getObjTermHash(Object o) {
		String hash = generateHashCode(OBJ, o);
		return hash;
	}

	public String setVarTermHash(Object o) {
		String hash = generateHashCode(VAR, o);
		addHashURIPair(hash, o);
		return hash;
	}

	public String getVarTermHash(Object o) {
		String hash = generateHashCode(VAR, o);
		return hash;
	}

	public void addHashURIPair(String hash, Object o) {
		uriMap.put(hash, o.toString());
	}

	public String getURIFromHash(String hash) {
		return uriMap.get(hash);
	}

	public String generateHashCode(String var, Object o) {
		int intHash = o.toString().hashCode();
		String stringHash = Integer.toString(intHash);
		String hash = "";
		for(int i = 0; i<stringHash.length(); i++){
			hash += Character.toString((char)(Character.getNumericValue(stringHash.charAt(i))+98));
		}
		return var + hash;
	}
}
