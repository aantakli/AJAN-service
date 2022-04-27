package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mqttplugin.endpoint.MQTTPluginServer;
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
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mqtt:DeleteMQTTBroker")
public class DeleteMQTTBroker extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    protected static final Logger LOG = LoggerFactory.getLogger(DeleteMQTTBroker.class);

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mqtt-ns#DeleteMQTTBroker");
    }

    @Override
    public NodeStatus executeLeaf() {
        stopBroker();
        String report = toString() + "SUCCEEDED";
        LOG.info(report);
        return new NodeStatus(Status.SUCCEEDED, report);
    }

    private void stopBroker() {
        MQTTPluginServer.destroyServer();
    }

    @Override
    public void end() {
        LOG.info("Status ("+ getStatus() + ")");
    }

    @Override
    public String toString() {
        return "DeleteMQTTBroker (" + getLabel() + ")";
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
