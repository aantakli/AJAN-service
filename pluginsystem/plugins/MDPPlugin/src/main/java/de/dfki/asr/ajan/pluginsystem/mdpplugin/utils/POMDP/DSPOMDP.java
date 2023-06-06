package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP;

import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.implementation.AJAN_Agent_State;

import java.util.Vector;

public abstract class DSPOMDP {

    public abstract boolean Step(AJAN_Agent_State state, double random_num, int action, double reward, int obs);

    public abstract int NumActions();

    public abstract int NumStates();

    public abstract double Reward(State state, int action);
    public abstract double ObsProb(int obs,AJAN_Agent_State state, int action);

    public abstract State CreateStartState(String type); // initialize the type to DEFAULT
    public abstract Vector<AJAN_Agent_State> getInitialBeliefParticles(State start, String type); // initialize the type to DEFAULT
    public abstract double GetMaxReward();

    public abstract ValuedAction GetBestAction();

    public abstract void PrintState(State state);

    public abstract void PrintObs(State state, int obs);

    public abstract void PrintAction(int action);

    public abstract void PrintBelief(Belief belief);

//    public abstract State Copy(State state);
//    public abstract int NumActiveParticles();

}

