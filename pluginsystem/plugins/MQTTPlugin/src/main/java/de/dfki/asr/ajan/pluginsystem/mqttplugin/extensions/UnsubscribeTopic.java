package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.net.URISyntaxException;

@Extension
@Component
@RDFBean("bt-mqtt:UnsubscribeTopic")
public class UnsubscribeTopic extends AbstractTDBLeafTask implements NodeExtension {

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

	@RDF("bt-mqtt:unsubscribeDetails")
    @Getter @Setter
    private BehaviorSelectQuery unsubscribeDetails;

    @Override
    public NodeStatus executeLeaf(){
        String report;
        Status stat;
        try {
            String serverUrl = MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
			String clientId = MQTTUtil.getId(unsubscribeDetails, this.getObject());
            String topic = MQTTUtil.getTopic(unsubscribeDetails, this.getObject());
            unsubscribeTopic(clientId, serverUrl, topic);
            stat = Status.SUCCEEDED;
            report = "SUCCEEDED";
        } catch (URISyntaxException | MqttException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }

        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }

    private void unsubscribeTopic(String clientId, String serverUrl, String topic) throws MqttException {
        MessageService messageService = MessageService.getMessageService(clientId, serverUrl);
        messageService.unsubscribe(topic);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public String toString(){
        return "SubscribeTopic ("+getStatus()+")";
    }

    @Override
    public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
        return SimulationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mqtt-ns#SubscribeTopic");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
