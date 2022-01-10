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

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class AJANStateModel implements FullStateModel {
	
	protected int[][] map;
	protected Random rand = RandomFactory.getMapped(0);
	protected Map<String,double[][]> actions;

	public AJANStateModel(int[][] map, Map<String,double[][]> actions) {
		this.map = map;
		this.actions = actions;
	}
	
	public void setActions(Map<String,double[][]> actions) {
		this.actions = actions;
	}
	
	@Override
	public List<StateTransitionProb> stateTransitions(State state, Action action) {
		double[][] probs = actions.get(action.actionName());
		
		List<StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
		return transitions;
	}

	@Override
	public State sample(State state, Action action) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
