package de.dfki.asr.ajan.pluginsystem.mdpplugin.queries;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary.pomdp_ns;

public class CommonQueries {

    public static final String SELECT_CURRENT_POMDP_ID = "PREFIX pomdp-ns:<http://www.dfki.de/pomdp-ns#>\n" +
            "\n" +
            "SELECT ?currentPOMDP\n" +
            "WHERE{\n" +
            "    pomdp-ns:POMDP pomdp-ns:current ?currentPOMDP .\n" +
            "} ";

    public static int getCurrentPOMDPId(AgentTaskInformation info) throws URISyntaxException {
        URI originBase = new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString());
        Repository repo = BTUtil.getInitializedRepository(info, originBase);
        try(RepositoryConnection connection = repo.getConnection()) {
            TupleQuery query = connection.prepareTupleQuery(SELECT_CURRENT_POMDP_ID);
            TupleQueryResult result = query.evaluate();
            List<BindingSet> bindings = new ArrayList<>();
            while(result.hasNext()) {
                bindings.add(result.next());
            }
            String idString = bindings.get(0).getValue("currentPOMDP").stringValue().replace(pomdp_ns.stringValue(),"");
            return Integer.parseInt(idString);
        }
    }
}


