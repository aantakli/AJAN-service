package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.common.AJANVocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class POMDPUtil {
    protected final static ValueFactory vf = SimpleValueFactory.getInstance();

    private POMDPUtil() {

    }

    public static void writeInput(final Model model, final String repository, final AgentTaskInformation info){
        if(repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())){
            info.getExecutionBeliefs().update(model);
        } else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())){
            info.getAgentBeliefs().update(model);
        }
    }

    public static void removeInput(final Model model, final String repository, final AgentTaskInformation info) {
        if (repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            info.getExecutionBeliefs().update(new LinkedHashModel(), model, false);
        } else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            info.getAgentBeliefs().update(new LinkedHashModel(), model, false);
        }
    }

}
