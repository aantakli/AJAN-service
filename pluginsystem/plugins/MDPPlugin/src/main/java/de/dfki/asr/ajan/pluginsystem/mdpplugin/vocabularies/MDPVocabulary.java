package de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class MDPVocabulary {

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
    // Subjects
    public final static IRI MDP_RESULTS = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#MDPResults");
    public final static IRI ACTIONS= FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#actions");
    public final static IRI REWARD = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#reward");
    public final static IRI ROUND = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#round");
    public final static IRI TURN = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#turn");

    // Predicates
    public final static IRI HAS_ACTION = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#hasAction");
    public final static IRI HAS_REWARD = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#hasReward");
    public final static IRI HAS_TURN = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#hasTurn");
    public final static IRI HAS_ROUND = FACTORY.createIRI("http://www.ajan.de/behavior/mdp-ns#hasRound");

}
