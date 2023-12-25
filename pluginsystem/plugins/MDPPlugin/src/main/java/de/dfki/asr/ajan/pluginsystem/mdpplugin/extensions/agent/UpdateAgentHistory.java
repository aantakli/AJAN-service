package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.agent;


import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries;
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

import java.net.URISyntaxException;

import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getCurrentPOMDPId;
import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getConstructResult;


@Extension
@Component
@RDFBean("bt-mdp:updateAgentHistory")
public class UpdateAgentHistory extends AbstractTDBLeafTask implements NodeExtension {

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
        try {
            String ttlString = getConstructResult(this.getObject(), CommonQueries.CONSTRUCT_OBSERVATION);
            sendToEndpoint(ttlString);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private void sendToEndpoint(String ttlString){
        JSONObject params = new JSONObject();
        try {
            params.put("pomdp_id", getCurrentPOMDPId(this.getObject()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        params.put("data", ttlString);
        if(HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/agent/update-history", params, this.getObject().getLogger(),this.getClass(), Boolean.class)){
            throw new RuntimeException("Error while sending observation to endpoint");
        }
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }
    @Override
    public String toString() {
        return "UpdateAgentHistory (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#updateAgentHistory");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}


