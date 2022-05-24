package de.dfki.asr.ajan.pluginsystem.mqttplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import de.dfki.asr.ajan.common.CSVInput;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MessageService {
    private static final MemoryPersistence persistence = new MemoryPersistence();
    private static IMqttAsyncClient _mqttClient;
    private static final Integer CLIENT_QOS = 2;
    private static MessageService messageServiceInstance;
    private String clientId = UUID.randomUUID().toString();

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
        }
        if (!_mqttClient.getServerURI().equals(broker)){
            try {
                _mqttClient.disconnect().waitForCompletion();
            } catch (MqttException e) {
                LOG.error("Error while disconnecting existing client:"+e.getMessage());
            }
            messageServiceInstance = new MessageService(broker);
        }
        if(!_mqttClient.isConnected()){
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

    public String subscribe(String topic, boolean keepAlive, URI eventURI, URI mapping, Repository repo, AbstractBeliefBase beliefs, Event event){
        final String[] message = new String[1];
        try{
            if(keepAlive) {
                _mqttClient.subscribe(topic, CLIENT_QOS, (s, mqttMessage) -> {
                    message[0] = new String(mqttMessage.getPayload());
                    if(eventURI != null){ //false
                        event.setEventInformation(parseMessage(message[0]));
                        }
                    else
                        storeInKnowledgeBase(message[0], mapping, repo, beliefs); // store it to agent knowledge
                });
            } else {
                _mqttClient.subscribe(topic, CLIENT_QOS, (s, mqttMessage) -> {
                    message[0] = new String(mqttMessage.getPayload());
                    storeInKnowledgeBase(message[0], mapping, repo, beliefs); // store it to agent knowledge
                    // update the agent knowledge
                    LOG.info(message[0]); //fire an event
                _mqttClient.unsubscribe(topic).waitForCompletion();
                }).waitForCompletion();
            }
        } catch (MqttException e){
            LOG.error(e.getMessage());
        }
        return message[0];
    }

    public void unsubscribe(String topic) throws MqttException {
        _mqttClient.unsubscribe(topic).waitForCompletion();
    }

    public boolean publish(String topic, String message){
        try {
            _mqttClient.publish(topic, message.getBytes(),1,true);
            return true;
        } catch (MqttException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

	public AbstractBeliefBase getBeliefs(final AgentTaskInformation info, final URI targetBase) {
		if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
			return info.getExecutionBeliefs();
		} else if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
			return info.getAgentBeliefs();
		} else {
			return null;
		}
	}

    // Helper Functions
    private void storeInKnowledgeBase(String s, URI mapping, Repository repo, AbstractBeliefBase beliefs) throws RMLMapperException, IOException, URISyntaxException, TransformerException {
        Model model = parseMessageAndGetModel(s, mapping, repo);
        beliefs.update(model);
    }

    private Model parseMessageAndGetModel(String s, URI mapping, Repository repo) throws IOException, TransformerException, URISyntaxException, RMLMapperException {
        // check if the message is CSV, JSON or XML
        InputStream parsedMessage = getInputStream(parseMessage(s));
        Model model = MappingUtil.getMappedModel(parsedMessage, repo, mapping);
        return model;
    }

    private Object parseMessage(String s) {
        MultivaluedMap<String,String> map = new MultivaluedHashMap<>();
        try {
            InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            JsonNode jsonNode = AgentUtil.setMessageInformation(AgentUtil.getJsonFromStream(in),map);
            return jsonNode;
        } catch (IOException e) {
            LOG.info("Message received is not Json, trying to parse as CSV");
        }

        try {
            InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            Document document = AgentUtil.setMessageInformation(AgentUtil.getXMLFromStream(in), map);
            return document;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            LOG.info("Message received is not XML, discarding it.");
        }

        try {
            InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            CSVInput csvInput = AgentUtil.setMessageInformation(AgentUtil.getCSVFromStream(in), map);
            return csvInput;
        } catch (IOException e) {
            LOG.info("Message received is not CSV, trying to parse as XML");
        }
        return null;
    }

    private ByteArrayInputStream getInputStream(Object jsonNode) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        ObjectWriter writer = om.writer();
        return new ByteArrayInputStream(writer.writeValueAsBytes(jsonNode));
    }
}
