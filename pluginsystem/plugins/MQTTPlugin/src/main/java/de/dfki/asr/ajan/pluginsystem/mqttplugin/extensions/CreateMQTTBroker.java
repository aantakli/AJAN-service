package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mqttplugin.endpoint.MQTTPluginServer;
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

@Extension
@Component
@RDFBean("bt-mqtt:CreateMQTTBroker")
public class CreateMQTTBroker extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

//    @RDF("bt-mqtt:callback")
//    @Getter @Setter
//    private BehaviorSelectQuery callback;
//    private int clPort;

    protected static final Logger LOG = LoggerFactory.getLogger(CreateMQTTBroker.class);

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mqtt-ns#CreateMQTTBroker");
    }

    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        try {
            LOG.info("Starting the MQTT Server");
            startBroker();
        } catch (Exception e){
            report += "FAILED";
            LOG.info(report);
            return new NodeStatus(Status.FAILED, report);
        }
//        System.out.println("Port"+clPort);
        report += "SUCCEEDED";
        LOG.info(report);
        return new NodeStatus(Status.SUCCEEDED, report);
    }

    private void startBroker() throws URISyntaxException {
        MQTTPluginServer.initServer();
        MQTTPluginServer.testConnection();
    }

    @Override
    public void end() {
    LOG.info("Status ("+ getStatus() + ")");
    }

    @Override
    public String toString() {
        return "CreateMQTTBroker (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }

}
