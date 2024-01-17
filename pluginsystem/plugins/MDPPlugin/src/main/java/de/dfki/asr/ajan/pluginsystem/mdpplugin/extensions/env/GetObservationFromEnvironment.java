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
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;
import static de.dfki.asr.ajan.pluginsystem.mdpplugin.queries.CommonQueries.getCurrentPOMDPId;


@Extension
@Component
@RDFBean("bt-mdp:getObservationFromEnvironment")
public class GetObservationFromEnvironment extends AbstractTDBLeafTask implements NodeExtension {
    private static final Logger LOG = LoggerFactory.getLogger(GetObservationFromEnvironment.class);

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mdp:pomdpId")
    @Setter
    private int pomdpId;

    @RDF("bt-mdp:observation-url")
    @Setter
    private String observationUrl;

    @Override
    public NodeStatus executeLeaf() {
        JSONObject params = new JSONObject();
        params.put("id", pomdpId);
        params.put("return_type", "turtle");
        ArrayList<?> response = HTTPHelper.sendPostRequest(observationUrl,
                params,
                this.getObject().getLogger(),
                this.getClass(),
                ArrayList.class);
        int responseCode = Integer.parseInt((String) response.get(0));
        try {
            String ttlString = (String) response.get(1);
            updateRDFInExecutionKnowledge(ttlString);
//            sendToEndpoint(ttlString);
        } catch (Exception e) {
            LOG.error("Error while parsing turtle string: " + e.getMessage());
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+" FAILED");
        }

        // Update the POMDP End point with the received observation

        if(responseCode >= 300 ) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), this+" FAILED");
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

    private void updateRDFInExecutionKnowledge(String ttlString) throws IOException {
        Model model = POMDPUtil.getModel(AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject());
        Model newModel = Rio.parse(new ByteArrayInputStream(ttlString.getBytes(StandardCharsets.UTF_8)), BASE_URI, RDFFormat.TURTLE);
        model.remove(null, null, null, POMDPVocabulary.CURRENT_OBSERVATION);
        model.addAll(newModel);
        POMDPUtil.writeInput(model, AJANVocabulary.EXECUTION_KNOWLEDGE.toString(), this.getObject(), false);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }
    @Override
    public String toString() {
        return "GetObservationFromEnvironment (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("https://ajan.de/behavior/mdp-ns#getObservationFromEnvironment");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode){
        return super.getModel(model, root, mode);
    }
}


