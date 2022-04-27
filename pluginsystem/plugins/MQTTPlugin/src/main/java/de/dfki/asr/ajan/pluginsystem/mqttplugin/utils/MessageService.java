package de.dfki.asr.ajan.pluginsystem.mqttplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.CsvResolver;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.Event;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.events.ModelEventInformation;
import de.dfki.asr.ajan.behaviour.events.ModelQueueEvent;
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.exception.EventEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import de.dfki.asr.ajan.common.CSVInput;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
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
import java.util.Map;
import java.util.Set;
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

    public String subscribe(String topic, boolean keepAlive, URI goalURI, AgentTaskInformation thisObject, URI mapping, URI targetBase, Repository repo, AgentBeliefBase agentBeliefs, Object eventInformation, Event event){
        final String[] message = new String[1];
        try{
            if(keepAlive) {
                _mqttClient.subscribe(topic, client_qos, (s, mqttMessage) -> {
                    message[0] = new String(mqttMessage.getPayload());
                    if(goalURI!=null){ //false
                        event.setEventInformation(parseMessage(message[0]));
//                        Model model = parseMessageAndGetModel(message[0],mapping, repo, eventInformation);
//                        event.setEventInformation(model);
//                        handleEvent(model, goalURI,targetBase,thisObject);//fire an event
                        }
                    else
                        storeInKnowledgeBase(message[0],thisObject,mapping, repo, agentBeliefs, eventInformation); // store it to agent knowledge
                });
            } else {
                _mqttClient.subscribe(topic, client_qos, (s, mqttMessage) -> {
                    message[0] = new String(mqttMessage.getPayload());
                    storeInKnowledgeBase(message[0],thisObject,mapping, repo, agentBeliefs, eventInformation); // store it to agent knowledge
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
    private void storeInKnowledgeBase(String s, AgentTaskInformation thisObject, URI mapping, Repository repo, AgentBeliefBase agentBeliefs, Object eventInformation) throws RMLMapperException, IOException, URISyntaxException, TransformerException {
        Model model = parseMessageAndGetModel(s, mapping, repo, eventInformation);
        agentBeliefs.update(model);
//                    evaluateMessage(message[0], thisObject, context, targetBase);
        getEventModel(thisObject);
    }

    private Model parseMessageAndGetModel(String s, URI mapping, Repository repo, Object eventInformation) throws IOException, TransformerException, URISyntaxException, RMLMapperException {
        // check if the message is RDF, JSON or XML
        InputStream parsedMessage = getInputStream(parseMessage(s));
        Model model = getModel(parsedMessage, repo, mapping, eventInformation);
        return model;
    }

    private Model getModel(InputStream parsedMessage, Repository repo, URI mapping, Object eventInformation) throws IOException, TransformerException, URISyntaxException, RMLMapperException {
//        InputStream resourceStream1 = MappingUtil.getResourceStream(eventInformation);
//        InputStream resourceStream = MappingUtil.getResourceStream(parsedMessage);
        InputStream resourceStream = parsedMessage;
        if (resourceStream != null) {
            if (mapping != null) {
//                String messageString = IOUtils.toString(resourceStream,StandardCharsets.UTF_8);
//                return getTriplesMaps(repo, mapping);
                return getMappedModel(getTriplesMaps(repo, mapping), resourceStream);
            }
            else {
                throw new RMLMapperException("no mapping file selected!");
            }
        }
        return new LinkedHashModel();
    }

    protected Model getTriplesMaps(Repository repo, URI mapping) {
        StringBuilder context = new StringBuilder();
        context.append("CONSTRUCT {?s ?p ?o} WHERE {GRAPH ");
        context.append("<").append(mapping.toString()).append("> {?s ?p ?o}}");
        return SPARQLUtil.queryRepository(repo, context.toString());
    }

    protected Model getTriplesMaps(Repository repo, String mapping) {
        StringBuilder context = new StringBuilder();
        context.append("CONSTRUCT {?s ?p ?o} WHERE {GRAPH ");
        context.append("<").append(mapping).append("> {?s ?p ?o}}");
        return SPARQLUtil.queryRepository(repo, context.toString());
    }

    public static Model getMappedModel(final Model mapping, final InputStream resourceStream) {
        Set<TriplesMap> mappingInput;
        mappingInput = RmlMappingLoader.build().load(mapping);
        RmlMapper mapper = RmlMapper.newBuilder()
                .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                .setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
                .setLogicalSourceResolver(Rdf.Ql.Csv, new CsvResolver())
                .build();

        mapper.bindInputStream(resourceStream);
        return mapper.map(mappingInput);
    }

    private void evaluateMessage(String s, AgentTaskInformation thisObject, URI context, URI targetBase) {
        Model init = new LinkedHashModel();
        EvaluationResult result = new EvaluationResult(init, thisObject);
        Model model = AgentUtil.setNamedGraph(result.getEvaluationModel(),context);
        writeToTarget(model, thisObject,targetBase);
    }

    protected Model getEventModel(AgentTaskInformation thisObject) {
        Object info = thisObject.getEventInformation();
        Model model = new LinkedHashModel();
        if (info instanceof ModelEventInformation) {
            ModelEventInformation eventInfo = (ModelEventInformation) info;
            model = eventInfo.getModel();
            return model;
        }
        return model;
    }

    private void setModelEvent(URI eventURI, AgentTaskInformation thisObject, Model model) throws ConditionEvaluationException, EventEvaluationException {
        Map<URI, Event> events = thisObject.getEvents();
        if (events.containsKey(eventURI)) {
            if (events.get(eventURI) instanceof ModelEvent || events.get(eventURI) instanceof ModelQueueEvent) {
                Event event = events.get(eventURI);
                event.setEventInformation(model);
            } else {
                throw new EventEvaluationException("Event is no ModelEvent");
            }
        } else {
            throw new EventEvaluationException("No such Event defined");
        }
    }

    private void writeToTarget(Model model,AgentTaskInformation thisObject, URI targetBase) {
        if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            thisObject.getAgentBeliefs().update(model);
        } else if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            thisObject.getExecutionBeliefs().update(model);
        }
    }

    private Object parseMessage(String s) {
        MultivaluedMap<String,String> map = new MultivaluedHashMap<>();
        try {
            InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
//            MappingUtil.getMappedModel(null,in);
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


    private void handleEvent(Model eventModel, URI goalURI, URI targetBase, AgentTaskInformation thisObject) throws URISyntaxException {
        if(checkEventGoalMatching(goalURI, thisObject)){
            if (targetBase.equals(new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString()))) {
                thisObject.getExecutionBeliefs().update(eventModel);
            } else if (targetBase.equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
                thisObject.getAgentBeliefs().update(eventModel);
            }
        }
    }

    protected boolean checkEventGoalMatching(URI event, AgentTaskInformation thisObject) {
        if (thisObject.getEventInformation() instanceof ModelEventInformation) {
            ModelEventInformation info = (ModelEventInformation)thisObject.getEventInformation();
            boolean eventMatching = event != null && event.toString().equals(((ModelEventInformation) info).getEvent());
            boolean allEvents = event != null && event.toString().equals(AJANVocabulary.ALL.toString());
            return event == null || eventMatching || allEvents;
        }
        return false;
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
