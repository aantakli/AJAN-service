package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mqttplugin.utils.MQTTUtil;
import de.dfki.asr.ajan.pluginsystem.mqttplugin.utils.MessageService;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;

@Extension
@Component
@RDFBean("bt-mqtt:PublishMessageRDF")
public class PublishMessageRDF extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mqtt:serverUrlCallback")
    @Getter @Setter
    private BehaviorSelectQuery serverUrlCallback;

    @RDF("bt-mqtt:publishDetails")
    @Getter @Setter
    private BehaviorSelectQuery publishDetails;

    @RDF("bt-mqtt:contentType")
    @Getter @Setter
    private String contentType;

    @RDF("bt-mqtt:rdfMessage")
    @Getter @Setter
    private BehaviorConstructQuery constructQuery;

	private final String clientId = UUID.randomUUID().toString();

    @Override
    public NodeStatus executeLeaf() {
        String report;
        Status stat;
        try {
            String serverUrl= MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
            String topic = MQTTUtil.getTopic(publishDetails, this.getObject());
            // execute the query
            Repository repo = BTUtil.getInitializedRepository(getObject(), constructQuery.getOriginBase());
            Model model = constructQuery.getResult(repo);
            String messagePayload = ACTNUtil.getModelPayload(model,contentType);
            publishMessage(serverUrl, topic, messagePayload);
            report = toString() + "SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            report = toString() + "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }

    private void publishMessage(String serverUrl, String topic, String message) {
        MessageService messageService = MessageService.getMessageService(clientId, serverUrl);
        if(messageService.publish(topic, message)){
			this.getObject().getLogger().info(this.getClass(), "Published the message to topic : "+topic);
        } else {
			this.getObject().getLogger().info(this.getClass(), "Error in publishing message to the topic: "+topic);
        }
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public String toString(){
        return "PublishMessageRDF ("+getStatus()+")";
    }

    @Override
    public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
        return SimulationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mqtt-ns#PublishMessageRDF");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
