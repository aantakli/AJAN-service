package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.events.MappingEvent;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.events.ModelQueueEvent;
import de.dfki.asr.ajan.behaviour.exception.EventSimulationException;
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
import org.eclipse.rdf4j.repository.Repository;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

@Extension
@Component
@RDFBean("bt-mqtt:SubscribeTopicProduceEvent")
public class SubscribeTopicProduceEvent extends AbstractTDBLeafTask implements NodeExtension {
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

    @RDF("bt-mqtt:subscribeDetails")
    @Getter @Setter
    private BehaviorSelectQuery subscribeDetails;

    @RDF("ajan:event")
    @Getter @Setter
    private URI goalEventURI;

    @Override
    public NodeStatus executeLeaf() {
        String report;
        Status stat;
        try {
            String serverUrl = MQTTUtil.getServerUrlInfo(serverUrlCallback, this.getObject());
            String topic = MQTTUtil.getTopic(subscribeDetails, this.getObject());
            Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
            subscribeToTopic(this.getObject().getAgentBeliefs().getSparqlEndpoint().toString(), serverUrl, topic, repo);
            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException | EventSimulationException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }

    private String subscribeToTopic(String clientId, String serverUrl, String topic, Repository repo) throws EventSimulationException {
        MessageService messageService = MessageService.getMessageService(clientId, serverUrl);
        return messageService.subscribe(topic, true, goalEventURI, null, repo, null, getEvent());
    }

    private Event getEvent() throws EventSimulationException {
        Map<URI,Event> events = this.getObject().getEvents();
        if (events.containsKey(goalEventURI)) {
            if (events.get(goalEventURI) instanceof ModelEvent || events.get(goalEventURI) instanceof ModelQueueEvent
                    || events.get(goalEventURI) instanceof MappingEvent) {
                return events.get(goalEventURI);
            } else {
                throw new EventSimulationException("Event is no ModelEvent or Mapping Event");
            }
        } else {
            throw new EventSimulationException("No such Event defined");
        }
    }
    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public String toString(){
        return "SubscribeTopicEventHandled ("+getStatus()+")";
    }

    @Override
    public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
        return SimulationResult.Result.UNCLEAR;
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mqtt-ns#SubscribeTopicEventHandled");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
