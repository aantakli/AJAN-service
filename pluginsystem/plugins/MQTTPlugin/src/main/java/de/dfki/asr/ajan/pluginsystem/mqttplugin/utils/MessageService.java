package de.dfki.asr.ajan.pluginsystem.mqttplugin.utils;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MessageService {
    private static final MemoryPersistence persistence = new MemoryPersistence();
    private static IMqttAsyncClient _mqttClient;
    private static final Integer client_qos = 2;
    private static MessageService messageServiceInstance;
    String clientId = UUID.randomUUID().toString();

    protected static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private MessageService(String broker){
        try {
            IMqttAsyncClient client = new MqttAsyncClient(broker, clientId, persistence);
            MqttConnectOptions options = getOptions();

            client.connect(options).waitForCompletion();
            _mqttClient = client;
            LOG.info("Client Connected Successfully");
        } catch (MqttException e) {
            LOG.error(e.getMessage());
        }
    }

    public static MessageService getMessageService(String broker) {

        if(messageServiceInstance == null){
            messageServiceInstance = new MessageService(broker);
        } else if (!_mqttClient.getServerURI().equals(broker)){
            try {
                _mqttClient.disconnect().waitForCompletion();
            } catch (MqttException e) {
                LOG.error("Error while disconnecting existing client:"+e.getMessage());
            }
            messageServiceInstance = new MessageService(broker);
        } else if(!_mqttClient.isConnected()){
            try {
                _mqttClient.connect(getOptions()).waitForCompletion();
            } catch (MqttException e) {
                LOG.error("Error while reconnecting"+e.getMessage());
            }
        }

        return messageServiceInstance;
    }

    private static MqttConnectOptions getOptions() {
        MqttConnectOptions _options = new MqttConnectOptions();
        _options.setCleanSession(true);
        _options.setAutomaticReconnect(true);
        _options.setConnectionTimeout(10);

        return _options;
    }

    public IMqttAsyncClient getMessageClient(){
        return _mqttClient != null?_mqttClient.isConnected()?_mqttClient:null:null;
    }

    public String subscribe(String topic){
        final String[] message = new String[1];
        try{
            _mqttClient.subscribe(topic, client_qos, (s, mqttMessage) -> {
                message[0] = new String(mqttMessage.getPayload());
                LOG.info(message[0]);
                _mqttClient.unsubscribe(topic).waitForCompletion();
            }).waitForCompletion();
        } catch (MqttException e){
            LOG.error(e.getMessage());
        }
        return message[0];
    }

    public String subscribe(String[] topics){
        final String[] message = new String[1];
        final int[] qos = new int[topics.length];
        final IMqttMessageListener[] messageListeners = new IMqttMessageListener[topics.length];
        for (int i = 0; i<topics.length; i++) {
            qos[i] = client_qos;
            messageListeners[i] = (s, mqttMessage) -> {
                message[0] = new String(mqttMessage.getPayload());
                LOG.info(String.format("Received a message from %s:%s",s,message[0]));
            };
        }



        try{
            _mqttClient.subscribe(topics, qos, messageListeners).waitForCompletion();
        } catch (MqttException e){
            LOG.error(e.getMessage());
        }
        return message[0];
    }

    public boolean publish(String topic, String message){
        try {
            _mqttClient.publish(topic, message.getBytes(),1,true);
//            _mqttClient.publish(topic,getMessage(message)).waitForCompletion();
            return true;
        } catch (MqttException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    private MqttMessage getMessage(String message) {
        return new MqttMessage(message.getBytes());
    }

}
