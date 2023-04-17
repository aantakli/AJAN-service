package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.MDPVocabulary;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.*;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.RDFContainers;
import org.eclipse.rdf4j.model.util.Statements;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rddl.RDDL;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;
import static de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil.getTriplesMaps;

public class KnowledgeBaseHelper {
    static Logger LOG = LoggerFactory.getLogger(KnowledgeBaseHelper.class);
    protected final ValueFactory vf = SimpleValueFactory.getInstance();
    protected static final String TURTLE = "http://www.ajan.de/ajan-mapping-ns#TextTurtle";
    protected static final String JSON = "http://www.ajan.de/ajan-mapping-ns#JsonLD";
    protected static final String XML = "http://www.ajan.de/ajan-mapping-ns#RDFxml";

    public static AbstractBeliefBase getBeliefs(final AgentTaskInformation info, final URI targetBase) {
        if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            return info.getExecutionBeliefs();
        } else if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            return info.getAgentBeliefs();
        } else {
            return null;
        }
    }

    public void storeInKnowledgeBase(String s, URI mapping, Repository repo, AbstractBeliefBase beliefs) throws RMLMapperException, IOException, URISyntaxException, TransformerException {
        Model model = parseMessageAndGetModel(s, mapping, repo);
        beliefs.update(model);
    }
    public ArrayList<Object> initializeModel(){
        ArrayList<Object> returnValues = new ArrayList<>();
        IRI subject = MDPVocabulary.MDP_RESULTS;
        IRI actionsPredicate = MDPVocabulary.HAS_ACTION;
        IRI rewardPredicate = MDPVocabulary.HAS_REWARD;
        Resource actionResults = vf.createBNode();
        Resource rewardsResults = vf.createBNode();
        Model model = new LinkedHashModel();
        model.add(subject, actionsPredicate, actionResults);
        model.add(subject, rewardPredicate, rewardsResults);
        returnValues.add(model);
        returnValues.add(actionResults);
        returnValues.add(rewardsResults);
        return returnValues;
    }
//    public ArrayList<Object> initializeModel(IRI s, Model model) {
//        ArrayList<Object> returnValues = new ArrayList<>();
//        if(model == null){
//            model = new LinkedHashModel();
//        }
//        IRI subject = MDPVocabulary.MDP_RESULTS;
//        IRI predicate = MDPVocabulary.HAS_ROUND;
////        IRI actionsSubject = vf.createIRI(BASE_URI +"/" +subjectName);
//        Resource actionResults = vf.createBNode();
//        model.add(subject, predicate, actionResults);
//        returnValues.add(model);
//        returnValues.add(actionResults);
//        return returnValues;
//    }
    public ArrayList<Object> initializeRound(int roundNumber, Model model, Resource actionResults) {
        ArrayList<Object> returnValues = new ArrayList<>();
        Resource roundResults = vf.createBNode();
        model.add(actionResults, MDPVocabulary.HAS_ROUND, roundResults);
        model.add(roundResults, RDFS.LABEL, vf.createLiteral(roundNumber));
        returnValues.add(model);
        returnValues.add(roundResults);
        return returnValues;
    }
    public Model updateModel(int turnNumber, ArrayList<RDDL.PVAR_INST_DEF> actions,double rewardForThisRound, Resource roundResults, Model model){
        ArrayList<Object> returnValues = new ArrayList<>();
//        Model actionsModel = RDFContainers.toRDF(RDF.SEQ, actions,MDPVocabulary.ACTIONS, new TreeModel());
//        TreeModel treeModel = (TreeModel)actionsModel;
        Resource turnResults = vf.createBNode();
        model.add(roundResults, MDPVocabulary.HAS_TURN, turnResults);
        model.add(turnResults, RDFS.LABEL, vf.createLiteral(turnNumber));
        model.add(turnResults, MDPVocabulary.HAS_REWARD, vf.createLiteral(rewardForThisRound));
        Resource actionResults = vf.createBNode();
        model.add(turnResults, MDPVocabulary.HAS_ACTION, actionResults);
//        IRI turnSubject = vf.createIRI(BASE_URI + "/turn" + turnNumber);
//        model.add(roundResults, turnSubject, vf.createLiteral(actions.toString()));
        return asRDF(actions,actionResults, model);
    }

    private Model asRDF(ArrayList<RDDL.PVAR_INST_DEF> actions, Resource turnResults, Model model) {
        model.add(turnResults, RDF.TYPE, RDF.LIST);
        if (actions.size()>1)
        {
            for (RDDL.PVAR_INST_DEF action : actions.subList(0, actions.size() - 1)) {
                Resource actionNode = vf.createBNode();
                model.add(turnResults, RDF.FIRST, vf.createLiteral(action.toString()));
                model.add(turnResults, RDF.REST, actionNode);
                turnResults = actionNode;
            }
            model.add(turnResults, RDF.FIRST, vf.createLiteral(actions.get(actions.size() - 1).toString()));
        } else if (actions.size()==1){
            model.add(turnResults, RDF.FIRST, vf.createLiteral(actions.get(0).toString()));
        }
            model.add(turnResults, RDF.REST, RDF.NIL);

        return model;
    }
    public Model updateModelWithRewards(int turnNumber, double rewardForThisTurn, Resource roundResults, Model model){
        Resource turnResults = vf.createBNode();
        model.add(roundResults, MDPVocabulary.HAS_TURN, turnResults);
        model.add(turnResults, RDFS.LABEL, vf.createLiteral(turnNumber));
        model.add(turnResults, MDPVocabulary.HAS_REWARD, vf.createLiteral(rewardForThisTurn));
        return model;
    }

    public Model updateModel(double roundReward, Resource roundResults, Model model){
//        ArrayList<Object> returnValues = new ArrayList<>();
        model.add(roundResults, MDPVocabulary.HAS_REWARD, vf.createLiteral(roundReward));
//        IRI turnSubject = vf.createIRI(BASE_URI + "/roundReward");
//        model.add(roundResults, turnSubject, vf.createLiteral(roundReward));
        return model;
    }
    public void storeInKnowledgeBase(ArrayList<ArrayList<RDDL.PVAR_INST_DEF>> roundActions, String subjectName) throws RMLMapperException, IOException, URISyntaxException, TransformerException {
        Model model = new LinkedHashModel();
        IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
        IRI actionsSubject = vf.createIRI(BASE_URI + subjectName);
        Resource actionResults = vf.createBNode();
        int round = 0;
        for (ArrayList<RDDL.PVAR_INST_DEF> roundAction : roundActions) {
            round+=1;
            IRI roundSubject = vf.createIRI(BASE_URI + "round"+round);
            Resource roundResults = vf.createBNode();
            model.add(actionsSubject, roundSubject, roundResults);
            int turnNumber=0;
//            IRI actionsList = vf.createIRI(BASE_URI + "actions");
//            Model actionsModel = RDFContainers.toRDF(RDF.LIST,roundAction,actionsList, new LinkedHashModel());
//            model.add(roundResults, actionsList, (Resource) actionsModel);
            for (RDDL.PVAR_INST_DEF action : roundAction) {
                turnNumber+=1;
                IRI turnSubject = vf.createIRI(BASE_URI + "turn"+turnNumber);
                model.add(roundResults, turnSubject, vf.createLiteral(action.toString()));
            }
        }
    }



    private Model parseMessageAndGetModel(String s, URI mapping, Repository repo) throws IOException, TransformerException, URISyntaxException, RMLMapperException {
        // check if the message is CSV, JSON or XML
        InputStream parsedMessage = getInputStream(s);
        Model model = getModel(parsedMessage, repo, mapping);
        return model;
    }

//    public Model getModel(InputStream parsedMessage, Repository repo, URI mapping) throws IOException, URISyntaxException, RMLMapperException {
//        if (parsedMessage != null) {
//            if (mapping != null) {
//                switch (mapping.toString()) {
//                    case TURTLE:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.TURTLE);
//                    case JSON:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.JSONLD);
//                    case XML:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.RDFXML);
//                    default:
//                        return MappingUtil.getMappedModel(getTriplesMaps(repo, mapping), parsedMessage);
//                }
//            }
//            else {
//                throw new RMLMapperException("no mapping file selected!");
//            }
//        }
//        return new LinkedHashModel();
//    }

    private Model getModel(InputStream s, Repository repo, URI mapping){
        try {
            return Rio.parse(s, BASE_URI, RDFFormat.TURTLE);
        } catch (IOException e) {
            LOG.info("Message received is not Text/Turtle, trying to parse as RDF/XML");
        }

        try {
            return Rio.parse(s, BASE_URI, RDFFormat.RDFXML);
        } catch (IOException e) {
            LOG.info("Message received is not RDF/XML, trying to parse as JSON-LD");
        }

        try {
            return Rio.parse(s, BASE_URI, RDFFormat.JSONLD);
        } catch (IOException e) {
            LOG.info("Message received is not JSON-LD, trying to parse as InputStream");
        }

        try {
            return MappingUtil.getMappedModel(getTriplesMaps(repo, mapping), s);
        } catch (URISyntaxException e) {
            LOG.error("Parsing not possible!", e);
        }
        return new LinkedHashModel();
    }

    protected Object parseMessage(String s) {
        try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.TURTLE);
        } catch (IOException e) {
            LOG.info("Message received is not Text/Turtle, trying to parse as RDF/XML");
        }

        try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.RDFXML);
        } catch (IOException e) {
            LOG.info("Message received is not RDF/XML, trying to parse as JSON-LD");
        }

        try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.JSONLD);
        } catch (IOException e) {
            LOG.info("Message received is not JSON-LD, trying to parse as InputStream");
        }

        try {
            return getInputStream(s);
        } catch (IOException e) {
            LOG.error("Pasing not possible!", e);
        }

        return null;
    }

    private ByteArrayInputStream getInputStream(String input) throws JsonProcessingException {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }
}
