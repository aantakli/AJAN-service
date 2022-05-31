package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Extension
@Component
@RDFBean("bt-mqtt:SubscribeTopicAlwaysListen")
public class SubscribeTopicAlwaysListen extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

    @RDF("bt:targetBase")
    @Getter @Setter
    private URI targetBase;

    @RDF("bt-mqtt:serverUrlCallback")
    @Getter @Setter
    private BehaviorSelectQuery serverUrlCallback;

    @RDF("bt-mqtt:subscribeDetails")
    @Getter @Setter
    private BehaviorSelectQuery subscribeDetails;

	private final String clientId = UUID.randomUUID().toString();
    protected static final Logger LOG = LoggerFactory.getLogger(SubscribeTopicAlwaysListen.class);

    @Override
    public NodeStatus executeLeaf(){
        String report;
        Status stat;
        try {
			if (getStatus() != Status.RUNNING) {
				String serverUrl = MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
				String topic = MQTTUtil.getTopic(subscribeDetails, this.getObject());
				Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
				subscribeToTopic(serverUrl, topic, repo);
				report = toString()+ " RUNNING";
				stat = Status.RUNNING;
			} else {
				report = toString()+ " RUNNING";
				stat = Status.RUNNING;
			}
        } catch (URISyntaxException e) {
            LOG.error("Error while fetching info" + e.getMessage());
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }

        LOG.info(report);
        return new NodeStatus(stat, report);
    }

    private String subscribeToTopic(String serverUrl, String topic, Repository repo) {
        MessageService messageService = MessageService.getMessageService(clientId, serverUrl);
		AbstractBeliefBase beliefs = messageService.getBeliefs(this.getObject(), targetBase);
        return messageService.subscribe(topic, true, null, mapping, repo, beliefs, null);
    }

    @Override
    public void end() {
        LOG.info("Status (" + getStatus() + ")");
		if (getStatus() == Status.CANCELLED) {
			BTUtil.sendReport(this.getObject(), toString() + " CANCELLED");
			MessageService.clearClient(clientId);
		}
    }

    @Override
    public String toString(){
        return "SubscribeTopicAlwaysListen (" + getStatus() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mqtt-ns#SubscribeTopicAlwaysListen");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
