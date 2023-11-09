package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.agent;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.datamodels.Belief;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;



@Extension
@Component
@RDFBean("bt-mdp:InitializeBelief")
public class InitializeBelief extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:representation")
    @Setter
    private String representation;

    @RDF("bt-mdp:beliefs")
    @Setter
    private List<Belief> beliefs;



    @Override
    public NodeStatus executeLeaf() {

        JSONObject stateParams = new JSONObject();
        stateParams.put("pomdp_id", pomdpId);
        stateParams.put("representation", representation);
        List<JSONObject> beliefDict = getJsonBeliefDict();
        stateParams.put("belief_dict", beliefDict);

        Model inputModel = getInputModel(pomdpId, representation);
        POMDPUtil.writeInput(inputModel, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), true); // Store in agent knowledge

//        stateParams.put("repo_url", repoUrl);


        int responseCode = HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/belief/create/init-belief", stateParams, this.getObject().getLogger(), this.getClass());
        if(responseCode >= 300){
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+"FAILED");
        }

        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +"SUCCEEDED");
    }
    private Model getInputModel(final int pomdpId, final String representation) {

        Model model = new LinkedHashModel();

        model.add(POMDPVocabulary.POMDP, POMDPVocabulary.HAS_ID,vf.createLiteral(pomdpId));

        BNode emptyNode = vf.createBNode();
        model.add(POMDPVocabulary.POMDP, POMDPVocabulary.HAS_INITIAL_BELIEF, emptyNode);
        model.add(emptyNode, POMDPVocabulary.TYPE, vf.createLiteral(representation));
        model.add(emptyNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, org.eclipse.rdf4j.model.vocabulary.RDF.LIST);

        BNode emptyBeliefNode = null;
        for (Belief belief : beliefs) {
            if(emptyBeliefNode == null){
             emptyBeliefNode = vf.createBNode();
             model.add(emptyNode, org.eclipse.rdf4j.model.vocabulary.RDF.FIRST, emptyBeliefNode);
            } else {
                BNode empty = vf.createBNode();
                model.add(emptyBeliefNode, org.eclipse.rdf4j.model.vocabulary.RDF.REST, empty);
                emptyBeliefNode = empty;
            }

            model.add(emptyBeliefNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, POMDPVocabulary.BELIEF);

            BNode emptyStateNode = vf.createBNode();
            model.add(emptyStateNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, POMDPVocabulary.STATE);
            model.add(emptyStateNode, POMDPVocabulary.HAS_ID, vf.createLiteral(belief.getStateId()));
            model.add(emptyStateNode, POMDPVocabulary.NAME, vf.createLiteral(belief.getStateName()));
            model.add(emptyStateNode, POMDPVocabulary.TYPE, vf.createLiteral(belief.getStateType()));

            model.add(emptyBeliefNode, org.eclipse.rdf4j.model.vocabulary.RDF.FIRST, emptyStateNode);
            model.add(emptyBeliefNode, POMDPVocabulary.WITH_PROBABILITY, vf.createLiteral(belief.getStateProbability()));
        }

        model.add(emptyNode, org.eclipse.rdf4j.model.vocabulary.RDF.REST, org.eclipse.rdf4j.model.vocabulary.RDF.NIL);

        return model;
    }



    private List<JSONObject> getJsonBeliefDict() {
        List<JSONObject> beliefDict = new ArrayList<>();
        for (Belief belief:beliefs) {
            JSONObject beliefParams = new JSONObject();
            JSONObject state = new JSONObject();
            state.put("id", belief.getStateId());
            state.put("name", belief.getStateName());
            state.put("type", belief.getStateType());
            beliefParams.put("state",state);
            beliefParams.put("probability", belief.getStateProbability());
            beliefDict.add(beliefParams);
        }
        return beliefDict;
    }


    @Override
    public String toString() {
        return "InitializeBelief (" + getLabel() + ")";
    }


    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#InitializeBelief");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
