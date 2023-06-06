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

public class DESPOTVocabulary {
    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();


    public final static IRI OBSERVATION_FUNCTION = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#observationFunction");
    public final static IRI STEP_FUNCTION = FACTORY.createIRI("http://www.ajan.de/behavior/pomdp-ns#StepFunction");

}
