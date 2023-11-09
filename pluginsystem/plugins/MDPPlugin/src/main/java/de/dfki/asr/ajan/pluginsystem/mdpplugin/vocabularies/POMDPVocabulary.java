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

    public static IRI pomdp_ns = FACTORY.createIRI("http://www.dfki.de/pomdp-ns/POMDP/");



    public final static IRI POMDP = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#POMDP");

    public final static IRI HAS_ID = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#id");
    public final static IRI IS_CURRENT = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#current");
    public final static IRI NAME = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#name");
    public final static IRI ATTRIBUTES = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#attributes");
    public final static IRI TYPE= FACTORY.createIRI("http://www.dfki.de/pomdp-ns#type");
    public final static IRI HAS_INITIAL_BELIEF = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#initialBelief");

    public final static IRI STATE = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#State");
    public final static IRI BELIEF = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#Belief");
    public final static IRI ACTION = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#Action");
    public final static IRI OBSERVATION = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#Observation");

    // state1 --hasReward--> <some Number>
    public final static IRI HAS_REWARD = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#hasReward");

    //pomdp --solver--> "DESPOT"
    public final static IRI SOLVER_NAME = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#solver");

    // state1 --changes to--> emptyNode
    public final static IRI CHANGES_TO = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#changesTo");

    // state/action/observation --label--> LEFT/RIGHT/LISTEN
    public final static IRI HAS_LABEL = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#label");

    // :emptyNode --with-action--> state 2
    public final static IRI WITH_ACTION = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#with");

    // :emptyNode --with-probability--> 0.5
    public final static IRI WITH_PROBABILITY = FACTORY.createIRI("http://www.dfki.de/pomdp-ns#probability");

    public static IRI createIRI(int pomdpId) {
        return FACTORY.createIRI(pomdp_ns.toString(), String.valueOf(pomdpId));
    }

    public static IRI createIRI(IRI nameSpace, int id) {
        return FACTORY.createIRI(nameSpace.toString().replace('#', '/'), "_" + id);
    }
}
