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

    private static final String POMDP_NS = "http://www.dfki.de/pomdp-ns#";
    private static final String POMDP_NS1 = "http://www.dfki.de/pomdp-ns/POMDP/";
    private static final String POMDP_DATA_NS = "http://www.dfki.de/pomdp-ns/data/";
    private static final String _POMDP = "http://www.dfki.de/pomdp-ns#POMDP";
    private static final String _rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static IRI pomdp_ns = FACTORY.createIRI(POMDP_NS1);



    public final static IRI POMDP = FACTORY.createIRI(POMDP_NS+ "POMDP");

    public final static IRI IS_CURRENT = FACTORY.createIRI(POMDP_NS + "current");
    public final static IRI TYPE= FACTORY.createIRI(POMDP_NS + "type");
    public final static IRI HAS_INITIAL_BELIEF = FACTORY.createIRI(POMDP_NS + "initialBelief");

    public final static IRI ENVIRONMENT = FACTORY.createIRI(POMDP_NS + "Environment");
    // OOState is not needed.
    public final static IRI STATE = FACTORY.createIRI(POMDP_NS + "State");
    public final static IRI ACTION = FACTORY.createIRI(POMDP_NS + "Action");
    public final static IRI OBSERVATION = FACTORY.createIRI(POMDP_NS + "Observation");
    // state1 --hasReward--> <some Number>
    public final static IRI REWARD = FACTORY.createIRI(POMDP_NS + "Reward");

    // Transition Function is not needed.
    // Observation Function is not needed.
    // Reward Function is not needed.
    // Policy is not needed.
    public final static IRI BELIEF = FACTORY.createIRI(POMDP_NS + "Belief");

    // BeliefState is not needed.
    public final static IRI PLANNED_ACTION = FACTORY.createIRI(POMDP_NS + "plannedAction");


    // Type is not needed.
    public final static IRI ID = FACTORY.createIRI(POMDP_NS + "id");
    public final static IRI NAME = FACTORY.createIRI(POMDP_NS + "name");
    public final static IRI ATTRIBUTES = FACTORY.createIRI(POMDP_NS + "attributes");
    // next_state is not needed.
    // current_state is not needed.
    // current_action is not needed.
    // current_reward is not needed.
    // :emptyNode --with-probability--> 0.5
    public final static IRI WITH_PROBABILITY = FACTORY.createIRI(POMDP_NS + "probability");
    public final static IRI CURRENT_OBSERVATION = FACTORY.createIRI(POMDP_NS + "CurrentObservation");

    public final static IRI TO_PRINT = FACTORY.createIRI(POMDP_NS + "to_print");
    public final static IRI FOR_HASH = FACTORY.createIRI(POMDP_NS + "for_hash");

    // ObservationModel is not needed.
    // TransitionModel is not needed.
    // RewardModel is not needed.
    // PolicyModel is not needed.

    public final static IRI PANDAS_DATA_FRAME = FACTORY.createIRI(POMDP_DATA_NS + "pandasDataFrame");
    public final static IRI VECTOR2D = FACTORY.createIRI(POMDP_DATA_NS + "2dvector");
    public final static IRI VECTOR3D = FACTORY.createIRI(POMDP_DATA_NS + "3dvector");
    public final static IRI POINT = FACTORY.createIRI(POMDP_DATA_NS + "Point");


    public final static IRI ACTION_SELECTOR = FACTORY.createIRI(POMDP_NS + "ActionSelector");

    public static IRI createIRI(int pomdpId) {
        return FACTORY.createIRI(pomdp_ns.toString(), String.valueOf(pomdpId));
    }

    public static IRI createIRI(IRI nameSpace, int id) {
        return FACTORY.createIRI(nameSpace.toString().replace('#', '/'), "_" + id);
    }

    public static IRI createIRI(IRI nameSpace, String name) {
        return FACTORY.createIRI(nameSpace.toString().replace('#', '/'), "_" + name);
    }

}
