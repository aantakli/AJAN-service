package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.domain;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
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
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getConstructResult;
import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getConstructResultModel;

@Getter
@Extension
@Component
@RDFBean("bt-mdp:CreateState")
public class State extends AbstractTDBLeafTask implements NodeExtension {


    @RDFSubject
    @Getter @Setter
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

    @RDF("bt-mdp:stateName")
    @Getter @Setter
    private String stateName;

    @RDF("bt-mdp:stateType")
    @Getter @Setter
    private String stateType;

    @RDF("bt-mdp:toPrintQuery")
    @Getter @Setter
    private BehaviorConstructQuery toPrintQuery;

    @RDF("bt-mdp:attributesQuery")
    @Getter @Setter
    private BehaviorConstructQuery attributesQuery;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject stateParams = getParams();
        if(HTTPHelper.sendPostRequest("http://127.0.0.1:8000/AJAN/pomdp/state/create/agent",
                stateParams,
                this.getObject().getLogger(),
                this.getClass(),
                Boolean.class)){
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+"FAILED");
        }
        Model inputModel = getInputModel();
        POMDPUtil.writeInput(inputModel, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), true);
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this +" SUCCEEDED");
    }

    private Model getInputModel() {
        Model model = new LinkedHashModel();
        IRI pomdp = POMDPVocabulary.createIRI(pomdpId);
        IRI stateSubject = POMDPVocabulary.createIRI(POMDPVocabulary.STATE, stateId);
        model.add(pomdp, POMDPVocabulary.STATE, stateSubject);
        model.add(stateSubject, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, POMDPVocabulary.STATE);
        model.add(stateSubject, POMDPVocabulary.ID, vf.createLiteral(stateId));
        model.add(stateSubject, POMDPVocabulary.NAME, vf.createLiteral(stateName));
        model.add(stateSubject, POMDPVocabulary.TYPE, vf.createLiteral(stateType));
        try {
            Model attributesResult = getConstructResultModel(this.getObject(), attributesQuery);
            Model toPrintResult = getConstructResultModel(this.getObject(), toPrintQuery);
            model.addAll(attributesResult);
            model.addAll(toPrintResult);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return model;
    }

    private JSONObject getParams() {
        JSONObject stateParams = new JSONObject();
        stateParams.put("pomdp_id", pomdpId);

        JSONObject state = new JSONObject();
        state.put("id", stateId);
        state.put("name", stateName);
        JSONObject params = new JSONObject();
        try {
            params.put("attributes_data",getConstructResult(this.getObject(), attributesQuery));
            params.put("to_print_data",getConstructResult(this.getObject(), toPrintQuery));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        state.put("params", params);
        state.put("type", stateType);

        stateParams.put("state", state);
        return stateParams;
    }

    @Override
    public void end() {this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");}

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public String toString() {
        return "CreateState (" + getLabel() + ")";
    }


    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#CreateState");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
