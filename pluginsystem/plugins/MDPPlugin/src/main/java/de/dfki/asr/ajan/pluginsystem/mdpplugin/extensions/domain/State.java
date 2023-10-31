package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.domain;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.datamodels.Attribute;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

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

    @RDF("bt-mdp:state-attributes")
    @Getter @Setter
    private List<Attribute> attributes;

    @RDF("bt-mdp:state-print-values")
    @Getter @Setter
    private List<String> printValues;

    @Override
    public NodeStatus executeLeaf() {

        for (Attribute attribute :
                attributes) {
            String name = attribute.getName();
            String value = attribute.getValue();
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8000/AJAN/pomdp/state/create/agent");
        httpPost.setHeader("Content-Type", "application/json");
        JSONObject stateParams = new JSONObject();
        stateParams.put("pomdp_id", pomdpId);
        stateParams.put("id", stateId);
        stateParams.put("name","");
        stateParams.put("attributes",new JSONObject());
        stateParams.put("to_print",new Object[]{"id"});
        HttpEntity postParams = new StringEntity(stateParams.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(postParams);
        CloseableHttpResponse httpResponse;

        try {
            httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            if(statusLine.getStatusCode() >= 300) {
                this.getObject().getLogger().info(this.getClass(),"POST Response Status: " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), toString()+" SUCCEEDED");
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
