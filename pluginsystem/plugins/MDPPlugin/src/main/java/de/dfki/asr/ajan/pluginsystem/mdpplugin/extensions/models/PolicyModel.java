package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.models;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.datamodels.Attribute;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.HTTPHelper;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.KnowledgeBaseHelper;
import de.dfki.asr.poser.Namespace.JSON;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;
import org.eclipse.rdf4j.repository.Repository;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

@Getter
@Extension
@Component
@RDFBean("bt-mdp:PolicyModel")
public class PolicyModel extends AbstractTDBLeafTask implements NodeExtension {

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

    @RDF("bt-mdp:policy-model-attributes")
    @Getter @Setter
    private List<Attribute> attributes;

    @RDF("bt-mdp:policy-data")
    @Getter @Setter
    private BehaviorConstructQuery data;

    @RDF("bt-mdp:policy-sample")
    @Getter @Setter
    private BehaviorSelectQuery sample;

    @RDF("bt-mdp:policy-rollout")
    @Getter @Setter
    private BehaviorSelectQuery rollout;

    @RDF("bt-mdp:policy-getAllActions")
    @Getter @Setter
    private BehaviorSelectQuery get_all_actions;


    @Override
    public NodeStatus executeLeaf() {
        try {
            Repository repository = BTUtil.getInitializedRepository(getObject(),data.getOriginBase());
            Model model = data.getResult(repository);
            String data = KnowledgeBaseHelper.getString(model, RDFFormat.TURTLE);
            JSONObject params = getParams(data);
            int responseCode = HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/policy_model/create/init-model", params, this.getObject().getLogger(), this.getClass());
            if(responseCode >= 300){
                return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+"FAILED");
            }
            return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject getParams(String data) {
        JSONObject params = new JSONObject();
        params.put("data", data);
        params.put("pomdp_id", pomdpId);
        params.put("sample_query", sample.getSparql());
        params.put("get_all_action_query", get_all_actions.getSparql());
        params.put("rollout_query", rollout.getSparql());
        return params;
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
        return "PolicyModel (" + getLabel() + ")";
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#PolicyModel");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
