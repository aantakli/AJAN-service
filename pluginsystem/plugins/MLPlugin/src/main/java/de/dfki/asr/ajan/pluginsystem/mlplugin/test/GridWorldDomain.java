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
package de.dfki.asr.ajan.pluginsystem.mlplugin.test;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * A domain generator for basic grid worlds. This domain generator allows for the creation
 * of arbitrarily sized grid worlds with user defined layouts. The grid world supports
 * classic north, south, east, west movement actions that may be either deterministic
 * or stochastic with user defined stochastic failures.
 * <p>
 * The domain is an {@link OODomain} that consists of only
 * two object classes: an agent class and a location class, each of which is defined by
 * and x and y position. Locations also have an attribute defining which type of location it is.
 * <p> 
 * Walls are not considered objects. Instead walls are
 * considered part of the transition dynamics. There are 2 types of walls supported. Walls that are more like obstacles
 * and occupy an entire cell of the map and 1D walls. 1D walls are specified as either a horizontal wall on the north side
 * of a cell or a vertical wall on the east side of the wall. A cell may also have a 1D north and east wall in it. The type of wall
 * for each cell is specified by a 2D int matrix provided to the constructor. Cells in the matrix with a 0 are clear of any walls
 * and obstacle; 1s indicate a full cell obstacle; 2s a 1D north wall; 3s a 1D east wall; and 4s indicate that the cell has both
 * a 1D north wall and 1D east wall.
 * <p>
 * Note that if you change the stochastic transition dynamics or the map of the domain generator *after* generating
 * a domain with {@link #generateDomain()}, the previously generated domain will use the settings prior to its
 * generation. To use the new settings, you will need to generate a new domain object.
 * <p>
 * There are five propositional functions
 * supported: atLocation(agent, location), wallToNorth(agent), wallToSouth(agent),
 * wallToEast(agent), and wallToWest(agent). 
 * @author James MacGlashan
 *
 */
public class GridWorldDomain implements DomainGenerator {


	/**
	 * Constant for x variable key
	 */
	public static final String VAR_X = "x";

	/**
	 * Constant for y variable key
	 */
	public static final String VAR_Y = "y";

	/**
	 * Constant for location type variable key
	 */
	public static final String VAR_TYPE = "type";


	/**
	 * Constant OO-MDP class name for agent
	 */
	public static final String CLASS_AGENT = "agent";

	/**
	 * Constant OO-MDP class name for a location
	 */
	public static final String CLASS_LOCATION = "location";

	/**
	 * Constant for the name of the north action
	 */
	public static final String ACTION_NORTH = "north";
	
	/**
	 * Constant for the name of the south action
	 */
	public static final String ACTION_SOUTH = "south";
	
	/**
	 * Constant for the name of the east action
	 */
	public static final String ACTION_EAST = "east";
	
	/**
	 * Constant for the name of the west action
	 */
	public static final String ACTION_WEST = "west";
	
	
	
	/**
	 * Constant for the name of the at location propositional function
	 */
	public static final String PF_AT_LOCATION = "atLocation";
	
	/**
	 * Constant for the name of the wall to north propositional function
	 */
	public static final String PF_WALL_NORTH = "wallToNorth";
	
	/**
	 * Constant for the name of the wall to south propositional function
	 */
	public static final String PF_WALL_SOUTH = "wallToSouth";
	
	/**
	 * Constant for the name of the wall to east propositional function
	 */
	public static final String PF_WALL_EAST = "wallToEast";
	
	/**
	 * Constant for the name of the wall to west propositional function
	 */
	public static final String PF_WALL_WEST = "wallToWest";
	
	
	
	/**
	 * The width of the grid world
	 */
	protected int										width;
	
	/**
	 * The height of grid world
	 */
	protected int										height;
	
	
	/**
	 * The number of possible location types
	 */
	protected int										numLocationTypes = 1;
	
	
	/**
	 * The wall map where the first index is the x position and the second index is the y position.
	 * Values of 1 indicate a wall is there, values of 0 indicate an empty cell
	 */
	protected int [][]									map;
	
	/**
	 * Matrix specifying the transition dynamics in terms of movement directions. The first index
	 * indicates the action direction attempted (ordered north, south, east, west) the second index
	 * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
	 * The value is the probability of that outcome. The existence of walls does not affect the probability
	 * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
	 * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
	 * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
	 */
	protected double[][]								transitionDynamics;


	protected RewardFunction rf;
	protected TerminalFunction tf;
	
	
	/**
	 * Constructs an empty map with deterministic transitions
	 * @param width width of the map
	 * @param height height of the map
	 */
	public GridWorldDomain(int width, int height){
		this.width = width;
		this.height = height;
		this.setDeterministicTransitionDynamics();
		this.makeEmptyMap();
	}
	
	
	/**
	 * Constructs a deterministic world based on the provided map.
	 * @param map the first index is the x index, the second the y; 1 entries indicate a wall
	 */
	public GridWorldDomain(int [][] map){
		this.setMap(map);
		this.setDeterministicTransitionDynamics();
	}
	
	
	/**
	 * Sets the number of possible location types to which a location object can belong. The default is 1.
	 * @param numLocationTypes the number of possible location types to which a location object can belong.
	 */
	public void setNumberOfLocationTypes(int numLocationTypes){
		this.numLocationTypes = numLocationTypes;
	}
	
	
	/**
	 * Will set the domain to use deterministic action transitions.
	 */
	public void setDeterministicTransitionDynamics(){
		int na = 4;
		transitionDynamics = new double[na][na];
		for(int i = 0; i < na; i++){
			for(int j = 0; j < na; j++){
				if(i != j){
					transitionDynamics[i][j] = 0.;
				}
				else{
					transitionDynamics[i][j] = 1.;
				}
			}
		}
	}
	
	/**
	 * Sets the domain to use probabilistic transitions. Agent will move in the intended direction with probability probSucceed. Agent
	 * will move in a random direction with probability 1 - probSucceed
	 * @param probSucceed probability to move the in intended direction
	 */
	public void setProbSucceedTransitionDynamics(double probSucceed){
		int na = 4;
		double pAlt = (1.-probSucceed)/3.;
		transitionDynamics = new double[na][na];
		for(int i = 0; i < na; i++){
			for(int j = 0; j < na; j++){
				if(i != j){
					transitionDynamics[i][j] = pAlt;
				}
				else{
					transitionDynamics[i][j] = probSucceed;
				}
			}
		}
	}
	
	/**
	 * Will set the movement direction probabilities based on the action chosen. The index (0,1,2,3) indicates the
	 * direction north,south,east,west, respectively and the matrix is organized by transitionDynamics[selectedDirection][actualDirection].
	 * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
	 * 
	 * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
	 */
	public void setTransitionDynamics(double [][] transitionDynamics){
		this.transitionDynamics = transitionDynamics.clone();
	}

	public double [][] getTransitionDynamics(){
		double [][] copy = new double[transitionDynamics.length][transitionDynamics[0].length];
		for(int i = 0; i < transitionDynamics.length; i++){
			for(int j = 0; j < transitionDynamics[0].length; j++){
				copy[i][j] = transitionDynamics[i][j];
			}
		}
		return copy;
	}
	
	
	/**
	 * Makes the map empty
	 */
	public void makeEmptyMap(){
		this.map = new int[this.width][this.height];
		for(int i = 0; i < this.width; i++){
			for(int j = 0; j < this.height; j++){
				this.map[i][j] = 0;
			}
		}
	}
	
	/**
	 * Set the map of the world.
	 * @param map the first index is the x index, the second the y; 1 entries indicate a wall
	 */
	public void setMap(int [][] map){
		this.width = map.length;
		this.height = map[0].length;
		this.map = map.clone();
	}
	
	
	/**
	 * Will set the map of the world to the classic Four Rooms map used the original options work (Sutton, R.S. and Precup, D. and Singh, S., 1999).
	 */
	public void setMapToFourRooms(){
		this.width = 11;
		this.height = 11;
		this.makeEmptyMap();
		
		horizontalWall(0, 0, 5);
		horizontalWall(2, 4, 5);
		horizontalWall(6, 7, 4);
		horizontalWall(9, 10, 4);
		
		verticalWall(0, 0, 5);
		verticalWall(2, 7, 5);
		verticalWall(9, 10, 5);
		
		
	}
	
	
	/**
	 * Creates a sequence of complete cell walls spanning the specified start and end x coordinates.
	 * @param xi The starting x coordinate of the wall
	 * @param xf The ending x coordinate of the wall
	 * @param y The y coordinate of the wall
	 */
	public void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++){
			this.map[x][y] = 1;
		}
	}
	
	/**
	 * Creates a sequence of complete cell walls spanning the specified start and end y coordinates
	 * @param yi The stating y coordinate of the wall
	 * @param yf The ending y coordinate of the wall
	 * @param x	The x coordinate of the wall
	 */
	public void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++){
			this.map[x][y] = 1;
		}
	}
	
	/**
	 * Creates a sequence of 1D north walls spanning the specified start and end x coordinates.
	 * If any of the cells spanned already have a east wall set in that location, then the cell
	 * is set to have both an east wall and a north wall.
	 * @param xi The starting x coordinate of the wall
	 * @param xf The ending x coordinate of the wall
	 * @param y The y coordinate of the wall
	 */
	public void horizontal1DNorthWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++){
			int cur = this.map[x][y];
			if(cur != 3 && cur != 4){
				this.map[x][y] = 2;
			}
			else{
				this.map[x][y] = 4;
			}
		}
	}
	
	/**
	 * Creates a sequence of 1D east walls spanning the specified start and end y coordinates.
	 * If any of the cells spanned already have a 1D north wall set in that location, then the cell
	 * is set to have both a north wall and an east wall.
	 * @param yi The stating y coordinate of the wall
	 * @param yf The ending y coordinate of the wall
	 * @param x	The x coordinate of the wall
	 */
	public void vertical1DEastWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++){
			int cur = this.map[x][y];
			if(cur != 2 && cur != 4){
				this.map[x][y] = 3;
			}
			else{
				this.map[x][y] = 4;
			}
		}
	}
	
	
	/**
	 * Sets a complete cell obstacle in the designated location.
	 * @param x the x coordinate of the obstacle
	 * @param y the y coordinate of the obstacle
	 */
	public void setObstacleInCell(int x, int y){
		this.map[x][y] = 1;
	}
	
	
	/**
	 * Sets a specified location to have a 1D north wall.
	 * If the specified cell already has a 1D east wall set in that location, then the cell
	 * is set to have both an east wall and a north wall.
	 * @param x the x coordinate of the location to have the north wall
	 * @param y the y coordinate of the location to have the north wall
	 */
	public void set1DNorthWall(int x, int y){
		int cur = this.map[x][y];
		if(cur != 3 && cur != 4){
			this.map[x][y] = 2;
		}
		else{
			this.map[x][y] = 4;
		}
	}
	
	/**
	 * Sets a specified location to have a 1D east wall.
	 * If the specified cell already has a 1D north wall set in that location, then the cell
	 * is set to have both a north wall and an east wall.
	 * @param x the x coordinate of the location to have the east wall
	 * @param y the y coordinate of the location to have the east wall
	 */
	public void set1DEastWall(int x, int y){
		int cur = this.map[x][y];
		if(cur != 2 && cur != 4){
			this.map[x][y] = 3;
		}
		else{
			this.map[x][y] = 4;
		}
	}
	
	/**
	 * Removes any obstacles or walls at the specified location.
	 * @param x the x coordinate of the location
	 * @param y the y coordinate of the location
	 */
	public void clearLocationOfWalls(int x, int y){
		this.map[x][y] = 0;
	}
	
	/**
	 * Sets the map at the specified location to have the specified wall configuration.
	 * @param x the x coordinate of the location
	 * @param y the y coordinate of the location
	 * @param wallType the wall configuration for this location. 0 = no walls; 1 = complete cell wall/obstacle; 2 = 1D north wall; 3 = 1D east wall; 4 = 1D north *and* east wall
	 */
	public void setCellWallState(int x, int y, int wallType){
		this.map[x][y] = wallType;
	}
	
	
	/**
	 * Returns a deep copy of the map being used for the domain
	 * @return a deep copy of the map being used in the domain
	 */
	public int [][] getMap(){
		int [][] cmap = new int[this.map.length][this.map[0].length];
		for(int i = 0; i < this.map.length; i++){
			for(int j = 0; j < this.map[0].length; j++){
				cmap[i][j] = this.map[i][j];
			}
		}
		return cmap;
	}
	

	/**
	 * Returns this grid world's width
	 * @return this grid world's width
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns this grid world's height
	 * @return this grid world's height
	 */
	public int getHeight() {
		return this.height;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public List<PropositionalFunction> generatePfs(){
		List<PropositionalFunction> pfs = Arrays.asList(
				new AtLocationPF(PF_AT_LOCATION, new String[]{CLASS_AGENT, CLASS_LOCATION}),
			new WallToPF(PF_WALL_NORTH, new String[]{CLASS_AGENT}, 0),
			new WallToPF(PF_WALL_SOUTH, new String[]{CLASS_AGENT}, 1),
			new WallToPF(PF_WALL_EAST, new String[]{CLASS_AGENT}, 2),
			new WallToPF(PF_WALL_WEST, new String[]{CLASS_AGENT}, 3));

		return pfs;
	}

	@Override
	public OOSADomain generateDomain() {

		OOSADomain domain = new OOSADomain();

		int [][] cmap = this.getMap();

		domain.addStateClass(CLASS_AGENT, GridAgent.class).addStateClass(CLASS_LOCATION, GridLocation.class);

		GridWorldModel smodel = new GridWorldModel(cmap, getTransitionDynamics());
		RewardFunction rf = this.rf;
		TerminalFunction tf = this.tf;

		if(rf == null){
			rf = new UniformCostRF();
		}
		if(tf == null){
			tf = new NullTermination();
		}

		FactoredModel model = new FactoredModel(smodel, rf, tf);
		domain.setModel(model);

		domain.addActionTypes(
				new UniversalActionType(ACTION_NORTH),
				new UniversalActionType(ACTION_SOUTH),
				new UniversalActionType(ACTION_EAST),
				new UniversalActionType(ACTION_WEST));

		
		OODomain.Helper.addPfsToDomain(domain, this.generatePfs());
		
		return domain;
	}

	
	




	/**
	 * Creates and returns a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI}
	 * object for a grid world. The value of states
	 * will be represented by colored cells from red (lowest value) to blue (highest value). North-south-east-west
	 * actions will be rendered with arrows using {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph}
	 * objects. The GUI will not be launched by default; call the
	 * {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI#initGUI()}
	 * on the returned object to start it.
	 * @param states the states whose value should be rendered.
	 * @param maxX the maximum value in the x dimension
	 * @param maxY the maximum value in the y dimension
	 * @param valueFunction the value Function that can return the state values.
	 * @param p the policy to render
	 * @return a gridworld-based {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI} object.
	 */
	public static ValueFunctionVisualizerGUI getGridWorldValueFunctionVisualization(List <State> states, int maxX, int maxY, ValueFunction valueFunction, Policy p){
		return ValueFunctionVisualizerGUI.createGridWorldBasedValueFunctionVisualizerGUI(states, valueFunction, p,
				new OOVariableKey(CLASS_AGENT, VAR_X), new OOVariableKey(CLASS_AGENT, VAR_Y), new VariableDomain(0, maxX), new VariableDomain(0, maxY), 1, 1,
				ACTION_NORTH, ACTION_SOUTH, ACTION_EAST, ACTION_WEST);
	}


	/**
	 * Returns the change in x and y position for a given direction number.
	 * @param i the direction number (0,1,2,3 indicates north,south,east,west, respectively)
	 * @return the change in direction for x and y; the first index of the returned double is change in x, the second index is change in y.
	 */
	protected static int [] movementDirectionFromIndex(int i){

		int [] result = null;

		switch (i) {
			case 0:
				result = new int[]{0,1};
				break;

			case 1:
				result = new int[]{0,-1};
				break;

			case 2:
				result = new int[]{1,0};
				break;

			case 3:
				result = new int[]{-1,0};
				break;

			default:
				break;
		}

		return result;
	}


	public static class GridWorldModel implements FullStateModel{


		/**
		 * The map of the world
		 */
		int [][] map;


		/**
		 * Matrix specifying the transition dynamics in terms of movement directions. The first index
		 * indicates the action direction attempted (ordered north, south, east, west) the second index
		 * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
		 * The value is the probability of that outcome. The existence of walls does not affect the probability
		 * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
		 * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
		 * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
		 */
		protected double[][] transitionDynamics;

		protected Random rand = RandomFactory.getMapped(0);


		public GridWorldModel(int[][] map, double[][] transitionDynamics) {
			this.map = map;
			this.transitionDynamics = transitionDynamics;
		}

		@Override
		public List<StateTransitionProb> stateTransitions(State s, Action a) {

			double [] directionProbs = transitionDynamics[actionInd(a.actionName())];

			List <StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
			for(int i = 0; i < directionProbs.length; i++){
				double p = directionProbs[i];
				if(p == 0.){
					continue; //cannot transition in this direction
				}
				State ns = s.copy();
				int [] dcomps = movementDirectionFromIndex(i);
				ns = move(ns, dcomps[0], dcomps[1]);

				//make sure this direction doesn't actually stay in the same place and replicate another no-op
				boolean isNew = true;
				for(StateTransitionProb tp : transitions){
					if(tp.s.equals(ns)){
						isNew = false;
						tp.p += p;
						break;
					}
				}

				if(isNew){
					StateTransitionProb tp = new StateTransitionProb(ns, p);
					transitions.add(tp);
				}


			}


			return transitions;
		}

		@Override
		public State sample(State s, Action a) {

			s = s.copy();

			double [] directionProbs = transitionDynamics[actionInd(a.actionName())];
			double roll = rand.nextDouble();
			double curSum = 0.;
			int dir = 0;
			for(int i = 0; i < directionProbs.length; i++){
				curSum += directionProbs[i];
				if(roll < curSum){
					dir = i;
					break;
				}
			}

			int [] dcomps = movementDirectionFromIndex(dir);
			return move(s, dcomps[0], dcomps[1]);

		}

		/**
		 * Attempts to move the agent into the given position, taking into account walls and blocks
		 * @param s the current state
		 * @param xd the attempted new X position of the agent
		 * @param yd the attempted new Y position of the agent
		 * @return input state s, after modification
		 */
		protected State move(State s, int xd, int yd){

			GridWorldState gws = (GridWorldState)s;

			int ax = gws.agent.x;
			int ay = gws.agent.y;

			int nx = ax+xd;
			int ny = ay+yd;

			//hit wall, so do not change position
			if(nx < 0 || nx >= map.length || ny < 0 || ny >= map[0].length || map[nx][ny] == 1 ||
					(xd > 0 && (map[ax][ay] == 3 || map[ax][ay] == 4)) || (xd < 0 && (map[nx][ny] == 3 || map[nx][ny] == 4)) ||
					(yd > 0 && (map[ax][ay] == 2 || map[ax][ay] == 4)) || (yd < 0 && (map[nx][ny] == 2 || map[nx][ny] == 4)) ){
				nx = ax;
				ny = ay;
			}

			GridAgent nagent = gws.touchAgent();
			nagent.x = nx;
			nagent.y = ny;

			return s;
		}


		protected int actionInd(String name){
			if(name.equals(ACTION_NORTH)){
				return 0;
			}
			else if(name.equals(ACTION_SOUTH)){
				return 1;
			}
			else if(name.equals(ACTION_EAST)){
				return 2;
			}
			else if(name.equals(ACTION_WEST)){
				return 3;
			}
			throw new RuntimeException("Unknown action " + name);
		}

	}

	
	/**
	 * Propositional function for determining if the agent is at the same position as a given location object
	 * @author James MacGlashan
	 *
	 */
	public class AtLocationPF extends PropositionalFunction {

		
		/**
		 * Initializes with given name domain and parameter object class types
		 * @param name name of function
		 * @param parameterClasses the object class types for the parameters
		 */
		public AtLocationPF(String name, String[] parameterClasses) {
			super(name, parameterClasses);
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			
			ObjectInstance agent = st.object(params[0]);
			ObjectInstance location = st.object(params[1]);
			
			int ax = (Integer)agent.get("x");
			int ay = (Integer)agent.get("y");
			
			int lx = (Integer)location.get("x");
			int ly = (Integer)location.get("y");
			
			if(ax == lx && ay == ly){
				return true;
			}
			
			return false;
		}
		

	}
	
	
	
	/**
	 * Propositional function for indicating if a wall is in a given position relative to the agent position
	 * @author James MacGlashan
	 *
	 */
	public class WallToPF extends PropositionalFunction{

		/**
		 * The relative x distance from the agent of the cell to check
		 */
		protected int xdelta;
		
		/**
		 * The relative y distance from the agent of the cell to check
		 */
		protected int ydelta;
		
		
		
		/**
		 * Initializes the function.
		 * @param name the name of the function
		 * @param parameterClasses the object class parameter types
		 * @param direction the unit distance direction from the agent to check for a wall (0,1,2,3 corresponds to north,south,east,west).
		 */
		public WallToPF(String name, String[] parameterClasses, int direction) {
			super(name, parameterClasses);
			int [] dcomps = GridWorldDomain.this.movementDirectionFromIndex(direction);
			xdelta = dcomps[0];
			ydelta = dcomps[1];
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			
			ObjectInstance agent = st.object(params[0]);
			
			int ax = (Integer)agent.get("x");
			int ay = (Integer)agent.get("y");
			
			int cx = ax + xdelta;
			int cy = ay + ydelta;
			
			if(cx < 0 || cx >= GridWorldDomain.this.width || cy < 0 || cy >= GridWorldDomain.this.height || GridWorldDomain.this.map[cx][cy] == 1 || 
					(xdelta > 0 && (GridWorldDomain.this.map[ax][ay] == 3 || GridWorldDomain.this.map[ax][ay] == 4)) || (xdelta < 0 && (GridWorldDomain.this.map[cx][cy] == 3 || GridWorldDomain.this.map[cx][cy] == 4)) ||
					(ydelta > 0 && (GridWorldDomain.this.map[ax][ay] == 2 || GridWorldDomain.this.map[ax][ay] == 4)) || (ydelta < 0 && (GridWorldDomain.this.map[cx][cy] == 2 || GridWorldDomain.this.map[cx][cy] == 4)) ){
				return true;
			}
			
			return false;
		}
	}
}
