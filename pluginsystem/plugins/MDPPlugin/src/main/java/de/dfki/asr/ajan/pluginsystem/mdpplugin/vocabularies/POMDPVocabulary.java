/*
 * Copyright (c) 2023. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class POMDPVocabulary {
    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

    public final static IRI POMDP = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#POMDP");
    public final static IRI STATE = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#State");
    public final static IRI ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#Action");
    public final static IRI OBSERVATION = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#Observation");

    // state1 --hasReward--> <some Number>
    public final static IRI HAS_REWARD = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#hasReward");

    //pomdp --solver--> "DESPOT"
    public final static IRI SOLVER_NAME = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#solver");

    // state1 --changes to--> emptyNode
    public final static IRI CHANGES_TO = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#changesTo");

    // state/action/observation --label--> LEFT/RIGHT/LISTEN
    public final static IRI HAS_LABEL = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#label");

    // :emptyNode --with-action--> state 2
    public final static IRI WITH_ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#with");

    // :emptyNode --with-probability--> 0.5
    public final static IRI WITH_PROBABILITY = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#probability");

}
