package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
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
import ro.fortsoft.pf4j.Extension;
import sun.util.resources.cldr.sg.CurrencyNames_sg;

import java.net.URISyntaxException;
import java.util.Map;

@Extension
@Component
@RDFBean("bt-mqtt:CreateMQTTClient")
public class CreateMQTTClient extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("bt-mqtt:clientHost")
    @Getter @Setter
    private BehaviorSelectQuery clientHost;
    private String host;
    private int port;

    protected static final Logger LOG = LoggerFactory.getLogger(CreateMQTTClient.class);
    public static MessageService mqttBroker;
    @Override
    public LeafStatus executeLeaf() {
        CreateClient();
        String report = toString() + "SUCCEEDED";
        LOG.info(report);
        return new LeafStatus(Status.SUCCEEDED, report);
    }

    public void CreateClient(){
        try {
            Map<String,String> hostMap = MQTTUtil.getHostInfos(clientHost,this.getObject());
            if(!hostMap.isEmpty()){
                Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
                host = entry.getKey();
                port = Integer.parseInt(entry.getValue());
                mqttBroker = MessageService.getMessageService("tcp://"+host+":"+port);
                mqttBroker.publish("/exit","Testing connection");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void end() {
        LOG.info("Status ("+ getStatus() + ")");
    }

    @Override
    public String toString() {
        return "CreateMQTTClient (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mqtt-ns#CreateMQTTClient");
    }


}
