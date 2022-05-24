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
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.net.URI;
import java.net.URISyntaxException;

@Extension
@Component
@RDFBean("bt-mqtt:SubscribeTopic")
public class SubscribeTopic extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt:context")
    @Getter @Setter
    private URI mapping;

    @RDF("bt-mqtt:serverUrlCallback")
    @Getter @Setter
    private BehaviorSelectQuery serverUrlCallback;

    @RDF("bt-mqtt:subscribeDetails")
    @Getter @Setter
    private BehaviorSelectQuery subscribeDetails;

//    @RDF("bt:targetBase")
//    @Getter @Setter
//    private URI targetBase;

    protected static final Logger LOG = LoggerFactory.getLogger(SubscribeTopic.class);

    @Override
    public NodeStatus executeLeaf() {

        String report;
        Status stat;

        String returnMessage;
        try {
            String serverUrl = MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
            String topic = MQTTUtil.getTopic(subscribeDetails, this.getObject());
            Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
            returnMessage = subscribeToTopic(serverUrl, topic, repo);
            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            LOG.error("Error while fetching info"+e.getMessage());
            returnMessage = null;
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }

//        if(returnMessage !=null){
//            report = toString()+ " SUCCEEDED";
//            stat = Status.SUCCEEDED;
//        } else {
//          report = toString()+ "FAILED";
//          stat = Status.FAILED;
//        }

        LOG.info(report);
        return new NodeStatus(stat, report);
    }

    private String subscribeToTopic(String serverUrl, String topic, Repository repo) {
        MessageService messageService = MessageService.getMessageService(serverUrl);
        URI targetBase = null;
//        URI mapping = null;
        return messageService.subscribe(topic, false, null, this.getObject(), mapping, targetBase,repo,
                this.getObject().getAgentBeliefs(),this.getObject().getEventInformation(), null);
    }





    @Override
    public void end() {
        LOG.info("Status ("+getStatus()+")");
    }

    @Override
    public String toString(){
        return "SubscribeTopic ("+getStatus()+")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
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
