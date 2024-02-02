package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.agent;



import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import static de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil.getDataString;

@Extension
@Component
@RDFBean("bt-mdp:createAgent")
public class CreateAgent extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:agentId")
    @Setter
    private int agentId;

    @RDF("bt-mdp:agent-data")
    @Getter @Setter
    private BehaviorConstructQuery data;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("pomdp_id", pomdpId);
        params.put("id", agentId);
        String dataString = getDataString(getObject(), data.getOriginBase(), RDFFormat.TURTLE, data);
        params.put("data",dataString);

        if(HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/agent/create",
                params,
                this.getObject().getLogger(),
                this.getClass(),
                Boolean.class) || dataString == null) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+"FAILED");
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }
    @Override
    public String toString() {
        return "CreateAgent (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#createAgent");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}