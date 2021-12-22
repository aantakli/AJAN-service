package de.dfki.asr.ajan.pluginsystem.mqttplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mqttplugin.utils.MessageService;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;
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

import java.io.File;
import java.io.IOException;
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
    static Server mqttBroker;
    private String serverUrl;
    private final String configFile = "config/moquette.conf";

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mqtt-ns#CreateMQTTBroker");
    }

    @Override
    public LeafStatus executeLeaf() {
        try {
            startMQTTServer();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        System.out.println("Port"+clPort);
        String report = toString() + "SUCCEEDED";
        LOG.info(report);
        return new LeafStatus(Status.SUCCEEDED, report);
    }

    private void startMQTTServer() throws URISyntaxException {
        // TODO: Check Implemented Mosquitto server initializer
        ClassLoader classLoader = getClass().getClassLoader();
        String filePath = classLoader.getResource(configFile).toString();

        File mqttConfigFile = new File(classLoader.getResource(configFile).toURI());
        String filePathSysProp = System.getProperty(configFile);
//        filePath = "G:/Projects/AJAN-service/pluginsystem/plugins/MQTTPlugin/target/classes/config/moquette.conf";
        IResourceLoader classpathLoader = new ClasspathResourceLoader(filePath);
        final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
        LOG.info("Starting MQTT broker...");
//        List userHandlers = Collections.singletonList(new PublisherListener());
        try{
            mqttBroker = new Server();
            mqttBroker.startServer(mqttConfigFile);
            setServerUrl(classPathConfig);
        } catch (IOException e){
            LOG.error("MQTT Broker start failed");
        }
        testConnection();
    }

    private void testConnection() {
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            LOG.warn("Pausing for publishing topics interrupted");
        }

        LOG.info("Publishing topics");
        publishMessage("/exit", "Testing Connection");
    }

    private void publishMessage(String topic, String message) {
        // TODO: Implement publish message
        MessageService messageService = MessageService.getMessageService(serverUrl);
        if(messageService.publish(topic,message)){
            LOG.info("Message published successfully");
        } else {
            LOG.error("Error in publishing message to server");
        }
    }

    private void setServerUrl(IConfig classPathConfig) {
        String host = classPathConfig.getProperty("host");
        String port = classPathConfig.getProperty("port");
        String protocol = classPathConfig.getProperty("protocol");
        serverUrl = protocol+"://"+host+":"+port;
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
