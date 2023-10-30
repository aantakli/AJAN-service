package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
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
        HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/belief/create/init-belief", stateParams, this.getObject().getLogger(), this.getClass());
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +"SUCCEEDED");
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
        return "InitializePOMDP (" + getLabel() + ")";
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
