/*
 * Copyright (c) 2023. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.implementation;

import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.Belief;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.DSPOMDP;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.State;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.ValuedAction;

import java.util.Random;
import java.util.Vector;


public class AJAN_Agent extends DSPOMDP {

    static AJAN_Agent_State currentState;
    static double currentReward;
    static int currentObservation;
    static int currentAction;
    final int LEFT = 0;
    final int RIGHT = 1;
    final int HOVER = 2;
    final double NOISE = 0.15;
//     private final Logger LOGGER = LoggerFactory.getLogger(AJAN_Agent.class);

    public AJAN_Agent() {
        currentState = new AJAN_Agent_State(-1, 0.5); // Check for memory management here
    }
    @Override
    public boolean Step(AJAN_Agent_State state, double random_num, int action, double reward, int obs) {
        // TODO: Implement this in Knowledge Graphs -  Execute the Transition Function here. Out: AgentInfo,reward,obs
        currentState.state_id = state.state_id;
        currentState.scenario_id = state.scenario_id;
        currentState.agent_position = state.agent_position;
        currentState.weight = state.weight;
        currentAction = action;
        boolean terminal = false;
        // region LOGIC here
        //TODO: Implementation can be in Knowledge Graphs
        if (action == LEFT || action == RIGHT) {
            currentReward = state.agent_position != currentAction ? 10 : -100;
            currentState.agent_position = random_num <= 0.5 ? LEFT : RIGHT;
            currentObservation = 2;
        } else {
            currentReward = -1;
            if (random_num <= (1 - NOISE))
                currentObservation = currentState.agent_position;
            else
                currentObservation = (LEFT + RIGHT - currentState.agent_position);
        }
        //endregion
        return terminal;
    }

    @Override
    public int NumActions() {
        // TODO: KB Implementation
        return 3;
    }
    @Override
    public int NumStates() {
        // TODO: KB Implementation
        return 2;}

    @Override
    public double Reward(State state, int action) {
        // TODO: KB Implementation
        return 0;
    }

    @Override
    public double ObsProb(int obs, AJAN_Agent_State state, int action) {
        // TODO: Implementation needed in Knowledge Graphs - Execute the Observation Probability query here. Out: AgentInfo,reward,obs
        if(action !=HOVER)
            return obs == 2 ? 1 : 0;
        return state.agent_position == obs ? (1-NOISE): NOISE;
    }

    @Override
    public AJAN_Agent_State CreateStartState(String type) {
        // TODO: This also can be implemented in KB
        AJAN_Agent_State agentState = new AJAN_Agent_State(-1,0.5);
        agentState.agent_position = new Random().nextInt(2);
        return agentState;
    }

    @Override
    public Vector<AJAN_Agent_State> getInitialBeliefParticles(State start, String type) {
        System.out.println("Got call to initial belief");
        Vector<AJAN_Agent_State> particles = new Vector<>();
        AJAN_Agent_State left = new AJAN_Agent_State(-1, 0.5);
        left.agent_position = LEFT;
        particles.add(left);
        AJAN_Agent_State right = new AJAN_Agent_State(-1, 0.5);
        right.agent_position = RIGHT;
        particles.add(right);

        return particles;
    }

    @Override
    public double GetMaxReward() {
        return 0;
    }

    @Override
    public ValuedAction GetBestAction() {
        return new ValuedAction(HOVER, -1);
    }

    @Override
    public void PrintState(State state) {
        AJAN_Agent_State agentState = (AJAN_Agent_State) state;
//        LOGGER.info(state.text());
        System.out.println(state.text());
    }

    @Override
    public void PrintObs(State state, int obs) {
        System.out.println(String.valueOf(obs));
    }

    @Override
    public void PrintAction(int action) {

        if(action == LEFT) {
            System.out.println("Fly Left");
        } else if (action == RIGHT) {
            System.out.println("Fly Right");
        } else {
            System.out.println("Listen");
        }

    }

    @Override
    public void PrintBelief(Belief belief) {

    }

    public String CreateScenarioLowerBound(String name, String particle_bound_name){
        return "DEFAULT";
    }

    public AJAN_Agent getParameters(){
        System.out.println("Returning Agent Parameters");
        return this;
    }

    public void PrintMethod() {System.out.println("Received a call");}

//    private native void InitializeObject(AJAN_Agent agent);
}
