package de.dfki.asr.ajan.pluginsystem.mqttplugin.endpoint;

import de.dfki.asr.ajan.pluginsystem.mqttplugin.utils.MessageService;
import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.pf4j.ExtensionPoint;

@Slf4j
@Extension
public class MQTTPluginServer implements ExtensionPoint {

    static Server mqttBroker;
    private static String serverUrl;
    private static final String CONFIG = "config/moquette.conf";

    public static void initServer() throws URISyntaxException {
//        AgentManager agentManagerObject = (AgentManager) agentManager;
        setupAndStartServer();
    }

    private static void setupAndStartServer() throws URISyntaxException {
            startAJANMQTTBroker();
    }

    private static void startAJANMQTTBroker() throws URISyntaxException {
        // TODO: Implement handler and processor
            startMQTTServer();
    }

    private static void startMQTTServer() throws URISyntaxException {
        ClassLoader classLoader = MQTTPluginServer.class.getClassLoader();
        String filePath = classLoader.getResource(CONFIG).toString();

        File mqttConfigFile = new File(classLoader.getResource(CONFIG).toURI()); // TODO: change to rdf query later
        IResourceLoader classpathLoader = new ClasspathResourceLoader(filePath);
        final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
        log.info("Starting MQTT broker...");
        try{
            mqttBroker = new Server();
            mqttBroker.startServer(mqttConfigFile);
            setServerUrl(classPathConfig);
        } catch (IOException e){
            log.error("MQTT Broker start failed");
        }
        testConnection();
    }

    public static void testConnection() {
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            log.warn("Pausing for publishing topics interrupted");
        }

        log.info("Publishing topics");
        publishMessage("/exit", "Testing Connection");
        subscribeTopic("/exit");

    }

    private static String subscribeTopic(String topic) {
        MessageService messageService = MessageService.getMessageService(serverUrl);
        String result = messageService.subscribe(topic, true, null, null, null, null, null);
        log.info("Subscribed to " + topic + " and got Message: " + result);
        return result;
    }

    private static void publishMessage(String topic, String message) {
        MessageService messageService = MessageService.getMessageService(serverUrl);
        if(messageService.publish(topic,message)){
            log.info("Message published successfully");
        } else {
            log.error("Error in publishing message to server");
        }
    }

    private static void setServerUrl(IConfig classPathConfig) {
        String host = classPathConfig.getProperty("host");
        String port = classPathConfig.getProperty("port");
        String protocol = classPathConfig.getProperty("protocol");
        serverUrl = protocol+"://"+host+":"+port;
    }

    public static void destroyServer() {
        if(mqttBroker!=null){
            log.info("Destroying Server");
            mqttBroker.stopServer();
        } else {
            log.info("No server Initialized to destroy");
        }
    }
}
