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

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class AJANPropositionalFunction extends PropositionalFunction {

	public AJANPropositionalFunction(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	public boolean isTrue(OOState oos, String... params) {
		ObjectInstance obj1 = oos.object(params[0]);
		ObjectInstance obj2 = oos.object(params[1]);
		
		if (obj1 instanceof AgentInstance && obj2 instanceof OOInstance) {
			AgentInstance agt = (AgentInstance)obj1;
			OOInstance ooi = (OOInstance)obj2;
			HashMap<String, Object> agtVars = agt.getVariables();
			HashMap<String, Object> ooiVars = ooi.getVariables();
			
			if (!agtVars.entrySet().stream().noneMatch((entry) -> (!ooiVars.get(entry.getKey()).equals(entry.getValue())))) {
				return false;
			}
		}
		return true;
	}
	
}
