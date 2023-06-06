package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP;

public abstract class State {
    public int state_id;
    public int scenario_id;
    public double weight;

    public State(int _state_id, double weight) {
        this.weight = weight;
        this.state_id = _state_id;
    }

    public abstract String text();

    State operator(int state_id, double weight){
        this.state_id = state_id;
        this.weight = weight;
        return this;
    }
};

//abstract class StateIndexer {
//  public abstract int NumStates();
//  public abstract int GetIndex(final State state);
//  public abstract State GetState(int index);
//};
//
//abstract class StatePolicy {
//  public abstract int GetAction(State state);
//};


