package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

@Extension
@Component
@RDFBean("bt-mqtt:PublishMessage")
public class PublishMessage extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter @Setter
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

    private String topic;
    private String message;

	private final String clientId = UUID.randomUUID().toString();
    protected static final Logger LOG = LoggerFactory.getLogger(PublishMessage.class);

    @Override
    public NodeStatus executeLeaf() {

        String report;
        Status stat;
        try {
            String serverUrl = MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
            Map<String,String> publishDetailsResult = MQTTUtil.getPublishInfo(publishDetails, this.getObject());
            if(!publishDetailsResult.isEmpty()){
                Map.Entry<String,String> entry = publishDetailsResult.entrySet().iterator().next();
                topic = entry.getKey();
                message = entry.getValue();
            }
            publishMessage(serverUrl, topic, message);
//            MQTTPluginServer.publishMessage(topic, message);
            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            LOG.error("Error while fetching info"+e.getMessage());
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }

        LOG.info(report);
        return new NodeStatus(stat, report);
    }

    private void publishMessage(String serverUrl, String topic, String message) {
        MessageService messageService = MessageService.getMessageService(clientId, serverUrl);
        if(messageService.publish(topic, message)){
            LOG.info("Published the message to topic : "+topic);
        } else {
            LOG.error("Error in publishing message to the topic: "+topic);
        }
    }

    @Override
    public void end() {
        LOG.info("Status ("+getStatus()+")");
    }

    @Override
    public String toString(){
        return "PublishMessage ("+getStatus()+")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result,final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mqtt-ns#PublishMessage");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }

}
