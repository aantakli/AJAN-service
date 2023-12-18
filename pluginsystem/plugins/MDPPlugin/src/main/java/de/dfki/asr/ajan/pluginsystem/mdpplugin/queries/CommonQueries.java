package de.dfki.asr.ajan.pluginsystem.mdpplugin.queries;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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

    public static final String CONSTRUCT_CURRENT_OBSERVATION = "PREFIX pomdp-ns:<http://www.dfki.de/pomdp-ns#>\n" +
            "PREFIX pomdp-ns1:<http://www.dfki.de/pomdp-ns/>\n" +
            "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "CONSTRUCT {?s ?p ?o}\n" +
            "WHERE{\n" +
            "    pomdp-ns:CurrentObservation rdf:value ?key .\n" +
            "    ?key (pomdp-ns:|!pomdp-ns:)* ?o .\n" +
            "    ?s ?p ?o .\n" +
            "}";

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

    public static String getConstructResult(AgentTaskInformation info, String constructQuery) throws URISyntaxException {
        BehaviorConstructQuery queryAll = new BehaviorConstructQuery();
        queryAll.setSparql(constructQuery);
        return getConstructResult(info, queryAll);
//        try( RepositoryConnection connection = repo.getConnection()){
//            TupleQuery tupleQuery = connection.prepareTupleQuery(constructQuery);
//            TupleQueryResult result = tupleQuery.evaluate();
//            return result.toString();
//        }
    }

    public static String getConstructResult(AgentTaskInformation info, BehaviorConstructQuery constructQuery) throws URISyntaxException {
        Model model = getConstructResultModel(info, constructQuery);
        OutputStream out = new ByteArrayOutputStream();
        Rio.write(model, out, RDFFormat.TURTLE);
        return out.toString();
    }

    public static Model getConstructResultModel(AgentTaskInformation info, BehaviorConstructQuery constructQuery) throws URISyntaxException {
        URI originBase = new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString());
        Repository repo = BTUtil.getInitializedRepository(info, originBase);
        return constructQuery.getResult(repo);
    }
}


