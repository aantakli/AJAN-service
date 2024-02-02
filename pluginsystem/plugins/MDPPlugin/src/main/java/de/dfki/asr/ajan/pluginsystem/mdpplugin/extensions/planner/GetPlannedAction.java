package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.planner;


import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;


@Extension
@Component
@RDFBean("bt-mdp:get-planner-actions")
public class GetPlannedAction extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("pomdp_id", pomdpId);
        JSONObject returnJson = (JSONObject) HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/planner/get-action", params, this.getObject().getLogger(),this.getClass(), JSONObject.class);
        int responseCode = (int) returnJson.get("statusCode");
        String action = (String) returnJson.get("name");
//        Model model = getOutputModel(action);
        updateInExecutionKnowledge((String) returnJson.get("data"));
//        POMDPUtil.writeInput(model, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), true);
        if(responseCode >= 300 ) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+" FAILED");
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private void updateInExecutionKnowledge(String data) {
        Model model = POMDPUtil.getModel(AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject());
        try {
            IRI pomdp = POMDPVocabulary.createIRI(pomdpId);
            model.remove(pomdp, POMDPVocabulary.PLANNED_ACTION, null); // Remove previous planned actions
            model.filter(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, POMDPVocabulary.PLANNED_ACTION).forEach(triple -> model.remove(triple.getSubject(), null, null)); // Remove previous planned actions
            model.remove(null, null, POMDPVocabulary.PLANNED_ACTION); // Remove previous planned actions
            model.addAll(POMDPUtil.parseTurtleString(data)); // Add new planned actions
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        POMDPUtil.writeInput(model, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), false);
    }

    private Model getOutputModel(String actionName) {
        Model model = POMDPUtil.getModel(AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject());
        IRI pomdp = POMDPVocabulary.createIRI(pomdpId);
        IRI actionSubject = POMDPVocabulary.createIRI(POMDPVocabulary.ACTION, actionName);
        model.add(pomdp, POMDPVocabulary.PLANNED_ACTION, actionSubject);
        model.add(actionSubject, POMDPVocabulary.NAME, vf.createLiteral(actionName));
        return model;
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }
    @Override
    public String toString() {
        return "GetPlannedAction (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#get-planner-actions");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}

