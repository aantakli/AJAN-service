/*
 * Copyright (C) 2021 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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
package de.dfki.asr.ajan.pluginsystem.mlplugin.utils;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
@DeepCopyState
public class AgentInstance implements ObjectInstance {

	protected final String name = "agent";
	protected final String className = "CLASS_AGENT";
	protected final HashMap<String, Object> variables;
	
	public AgentInstance(HashMap<String, Object> variables) {
		this.variables = variables;
	}
	
	@Override
	public String className() {
		return this.className;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		AgentInstance nagent = this.copy();
		return nagent;
	}

	@Override
	public List<Object> variableKeys() {
		return new ArrayList<>(variables.keySet());
	}

	@Override
	public Object get(Object variableKey) {
		if(!(variableKey instanceof String)){
			throw new RuntimeException("Variable key must be a string");
		}
		
		String key = (String)variableKey;
		
		if (variables.containsKey(key)) {
			throw new RuntimeException("Unknown key " + key);
		}
		return variables.get(key);
	}

	@Override
	public AgentInstance copy() {
		return new AgentInstance((HashMap)variables.clone());
	}

	public HashMap<String, Object> getVariables() {
		return this.variables;
	}
	
}
