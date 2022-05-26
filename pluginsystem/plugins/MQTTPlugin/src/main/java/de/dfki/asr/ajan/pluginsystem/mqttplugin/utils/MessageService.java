package de.dfki.asr.ajan.pluginsystem.mqttplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.Event;
import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import de.dfki.asr.ajan.common.CSVInput;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import static de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil.getTriplesMaps;
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
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class MessageService {
    private static final MemoryPersistence persistence = new MemoryPersistence();
    private static IMqttAsyncClient _mqttClient;
    private static final Integer CLIENT_QOS = 2;
    private static MessageService messageServiceInstance;
    private String clientId = UUID.randomUUID().toString();

    protected static final Logger LOG = LoggerFactory.getLogger(MessageService.class);
	private static final String TURTLE = "http://www.ajan.de/ajan-mapping-ns#TextTurtle";
	private static final String JSON = "http://www.ajan.de/ajan-mapping-ns#JsonLD";
	private static final String XML = "http://www.ajan.de/ajan-mapping-ns#RDFxml";

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
        InputStream parsedMessage = getInputStream(s);
        Model model = getModel(parsedMessage, repo, mapping);
        return model;
    }
	
		public Model getModel(InputStream parsedMessage, Repository repo, URI mapping) throws IOException, URISyntaxException, RMLMapperException {
		if (parsedMessage != null) {
            if (mapping != null) {
				switch (mapping.toString()) {
					case TURTLE:
						return Rio.parse(parsedMessage, BASE_URI, RDFFormat.TURTLE);
					case JSON:
						return Rio.parse(parsedMessage, BASE_URI, RDFFormat.JSONLD);
					case XML:
						return Rio.parse(parsedMessage, BASE_URI, RDFFormat.RDFXML);
					default:
						return MappingUtil.getMappedModel(getTriplesMaps(repo, mapping), parsedMessage);
				}
            }
            else {
                throw new RMLMapperException("no mapping file selected!");
            }
        }
        return new LinkedHashModel();
	}

    private Object parseMessage(String s) {
        try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.TURTLE);
        } catch (IOException e) {
            LOG.info("Message received is not Text/Turtle, trying to parse as RDF/XML");
        }

		try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.RDFXML);
        } catch (IOException e) {
            LOG.info("Message received is not RDF/XML, trying to parse as JSON-LD");
        }

        try {
            return Rio.parse(getInputStream(s), BASE_URI, RDFFormat.JSONLD);
        } catch (IOException e) {
            LOG.info("Message received is not JSON-LD, trying to parse as InputStream");
        }
		
		try {
            return getInputStream(s);
        } catch (IOException e) {
			LOG.error("Pasing not possible!", e);
        }

        return null;
    }

    private ByteArrayInputStream getInputStream(String input) throws JsonProcessingException {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }
}
