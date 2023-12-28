package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.env;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Extension
@Component
@RDFBean("bt-mdp:getEnvironmentState")
public class GetEnvironmentState extends AbstractTDBLeafTask implements NodeExtension {
    private static final Logger LOG = LoggerFactory.getLogger(GetEnvironmentState.class);


    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:type")
    @Setter
    private String state_type; // next or current

    @RDF("bt-mdp:stateId")
    @Getter @Setter
    private int stateId;


    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("pomdp_id", pomdpId);
        params.put("type", state_type);
        params.put("state_id", stateId);
        JSONObject returnJson = HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/env/get-env-state", params, this.getObject().getLogger(),this.getClass(), JSONObject.class);
        String ttlDataString = (String) returnJson.get("data");
        if(ttlDataString == null){
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+" FAILED");
        }
        updateInExecutionKnowledge(ttlDataString);
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private void updateInExecutionKnowledge(String data){
        Model model = POMDPUtil.getModel(AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject());
        try{
            model.addAll(POMDPUtil.parseTurtleString(data));
        } catch (Exception e){
            LOG.error("Error while parsing turtle string", e);
        }
        POMDPUtil.writeInput(model, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), false);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }


    @Override
    public String toString() {
        return "SampleFromTransitionModel (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.dfki.de/ajan/behavior/mdp-ns#sampleFromTransitionModel");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }


}
