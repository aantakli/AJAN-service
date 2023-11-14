package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.domain;

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
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;


@Getter
@Extension
@Component
@RDFBean("bt-mdp:CreateAction")
public class Action extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Setter
    private String label;

    @RDF("bt-mdp:action-name")
    @Setter
    private String actionName;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("action_name", actionName);
        params.put("pomdp_id", pomdpId);
        int responseCode = HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/action/create", params, this.getObject().getLogger(),this.getClass());
        if (responseCode >= 300 ) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this +" FAILED");
        }
        Model inputModel = getInputModel();
        POMDPUtil.writeInput(inputModel, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), true);
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private Model getInputModel(){
        Model model = POMDPUtil.getModel(AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject());
        IRI pomdp = POMDPVocabulary.createIRI(pomdpId);
        IRI actionSubject = POMDPVocabulary.createIRI(POMDPVocabulary.ACTION, actionName);
        model.add(pomdp, POMDPVocabulary.ACTION, actionSubject);
        model.add(actionSubject, POMDPVocabulary.NAME, vf.createLiteral(actionName));
        return model;
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
    public String toString() {
        return "CreateAction (" + getLabel() + ")";
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#CreateAction");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
