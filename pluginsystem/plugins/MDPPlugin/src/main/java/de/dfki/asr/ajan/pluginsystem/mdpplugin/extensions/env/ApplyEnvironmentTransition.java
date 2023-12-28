package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.env;

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
import org.json.JSONObject;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getConstructResult;

@Extension
@Component
@RDFBean("bt-mdp:applyEnvironmentTransition")
public class ApplyEnvironmentTransition extends AbstractTDBLeafTask implements NodeExtension {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyEnvironmentTransition.class);

    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:stateId")
    @Getter @Setter
    private int stateId;

    @RDF("bt-mdp:env-data")
    @Getter @Setter
    private BehaviorConstructQuery data;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("pomdp_id", pomdpId);
        params.put("state_id", stateId);
        String dataString = null;
        try {
            dataString = getConstructResult(getObject(), data.getSparql());
        } catch (URISyntaxException e) {
            LOG.error("Error while executing construct query: " + data.getSparql(), e);
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this + " FAILED");
        }
        params.put("data", dataString);
        if(HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/env/apply-transition",
                params,
                this.getObject().getLogger(),
                this.getClass(), Boolean.class) || dataString == null) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(),this.getClass(), this + "FAILED");
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "ApplyEnvironmentTransition (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#applyEnvironmentTransition");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
