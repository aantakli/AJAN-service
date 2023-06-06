package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP;

import java.util.Vector;

public abstract class Belief {
    DSPOMDP model_;
    History history_;

    public Belief(DSPOMDP model) {
        this.model_=model;
    }
    abstract Vector<State> Sample(int num);
    abstract void Update(int action, int obs);

    abstract String text();
    abstract Belief MakeCopy();
  }
