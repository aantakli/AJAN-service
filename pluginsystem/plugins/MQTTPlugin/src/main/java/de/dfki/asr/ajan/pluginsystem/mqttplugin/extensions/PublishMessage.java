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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
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
            } else {
				report = toString()+ "FAILED";
				stat = Status.FAILED;
				return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
			}
			if (message.contains("<UTF-8>")) {
				message = getUtf8Message(message);
			}
            publishMessage(serverUrl, topic, message);
//            MQTTPluginServer.publishMessage(topic, message);
            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException | NullPointerException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }
	
	private String getUtf8Message(String message) {
		String[] utf8_1 = message.split("<UTF-8>");
		String[] utf8_2 = utf8_1[1].split("</UTF-8>");

		String utf8EncodedString = utf8_2[0].replaceAll("\\s+","");
		if (utf8EncodedString.startsWith("\"")) {
			utf8EncodedString = utf8EncodedString.substring(1);
		}
		if (utf8EncodedString.endsWith("\"")) {
			utf8EncodedString = utf8EncodedString.substring(0, utf8EncodedString.length() - 1);
		}
		utf8EncodedString = utf8EncodedString.replaceAll("'", "\"");
		utf8EncodedString = utf8EncodedString.replaceAll("\"", "\\\\\"");
		return utf8_1[0] + '"' + utf8EncodedString + '"' + utf8_2[1];
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
        return "PublishMessage ("+getStatus()+")";
    }

    @Override
    public SimulationResult.Result simulateNodeLogic(final SimulationResult result,final Resource root) {
        return SimulationResult.Result.UNCLEAR;
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
