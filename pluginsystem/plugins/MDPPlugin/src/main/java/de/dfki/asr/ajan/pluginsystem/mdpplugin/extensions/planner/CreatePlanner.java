package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.planner;


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


@Extension
@Component
@RDFBean("bt-mdp:createPlanner")
public class CreatePlanner extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:planner-name")
    @Setter
    private String plannerName;

    @RDF("bt-mdp:planner-maxdepth")
    @Setter
    private int max_depth;

    @RDF("bt-mdp:planner-planningtime")
    @Setter
    private float planningtime;

    @RDF("bt-mdp:planner-exploration-const")
    @Setter
    private int exploration_const;

    @RDF("bt-mdp:planner-numsims")
    @Setter
    private int num_sims;

    @RDF("bt-mdp:planner-discount-factor")
    @Setter
    private float discount_factor;

    @RDF("bt-mdp:planner-update-interval")
    @Setter
    private int update_interval;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = getParams();
        int responseCode = HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/planner/create", params, this.getObject().getLogger(),this.getClass());
        if(responseCode >= 300) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+"FAILED");
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private JSONObject getParams() {
        JSONObject params = new JSONObject();
        params.put("pomdp_id", pomdpId);
        params.put("name",plannerName);
        params.put("max_depth", max_depth);
        params.put("planning_time",planningtime);
        params.put("exploration_const",exploration_const);
        params.put("num_sims", num_sims);
        params.put("discount_factor",discount_factor);
        params.put("pbar_update_interval",update_interval);
        return params;
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }
    @Override
    public String toString() {
        return "CreatePlanner (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#createPlanner");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}


