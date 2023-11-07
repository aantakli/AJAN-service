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
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDPUtil;
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
import org.eclipse.rdf4j.repository.Repository;

import java.net.URISyntaxException;
import java.util.List;

@Getter
@Extension
@Component
@RDFBean("bt-mdp:ObservationModel")
public class ObservationModel extends AbstractTDBLeafTask implements NodeExtension {

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

    @RDF("bt-mdp:observation-model-attributes")
    @Getter @Setter
    private List<Attribute> attributes;

    @RDF("bt-mdp:observation-data")
    @Getter @Setter
    private BehaviorConstructQuery data;

    @RDF("bt-mdp:observation-probability")
    @Getter @Setter
    private BehaviorSelectQuery probability;

    @RDF("bt-mdp:observation-sample")
    @Getter @Setter
    private BehaviorSelectQuery sample;

    @RDF("bt-mdp:observation-argmax")
    @Getter @Setter
    private BehaviorSelectQuery argmax;


    @Override
    public NodeStatus executeLeaf() {
        return POMDPUtil.sendProbabilisticDataToEndpoint(getObject(), data.getOriginBase(), pomdpId, null,
                "http://127.0.0.1:8000/AJAN/pomdp/observation_model/create/init-model",
                RDFFormat.TURTLE, data, probability, sample, argmax,
                this.getObject().getLogger(), this.getClass(), toString());

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
        return "ObservationModel (" + getLabel() + ")";
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#ObservationModel");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}
