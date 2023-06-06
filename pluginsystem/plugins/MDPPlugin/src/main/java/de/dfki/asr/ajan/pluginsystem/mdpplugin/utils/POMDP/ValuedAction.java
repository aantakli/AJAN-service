package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP;

public class ValuedAction {
    int action;
    double value;
    public ValuedAction(int _action, double _value){
        this.action = _action;
        this.value = _value;
    }

}
