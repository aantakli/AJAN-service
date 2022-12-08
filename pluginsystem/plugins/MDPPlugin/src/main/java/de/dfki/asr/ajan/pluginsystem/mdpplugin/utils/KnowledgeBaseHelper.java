package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;
import static de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil.getTriplesMaps;

public class KnowledgeBaseHelper {
    static Logger LOG = LoggerFactory.getLogger(KnowledgeBaseHelper.class);

    protected static final String TURTLE = "http://www.ajan.de/ajan-mapping-ns#TextTurtle";
    protected static final String JSON = "http://www.ajan.de/ajan-mapping-ns#JsonLD";
    protected static final String XML = "http://www.ajan.de/ajan-mapping-ns#RDFxml";

    public static AbstractBeliefBase getBeliefs(final AgentTaskInformation info, final URI targetBase) {
        if (targetBase.toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            return info.getExecutionBeliefs();
        } else if (targetBase.toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            return info.getAgentBeliefs();
        } else {
            return null;
        }
    }

    public void storeInKnowledgeBase(String s, URI mapping, Repository repo, AbstractBeliefBase beliefs) throws RMLMapperException, IOException, URISyntaxException, TransformerException {
        Model model = parseMessageAndGetModel(s, mapping, repo);
        beliefs.update(model);
    }

    private Model parseMessageAndGetModel(String s, URI mapping, Repository repo) throws IOException, TransformerException, URISyntaxException, RMLMapperException {
        // check if the message is CSV, JSON or XML
        InputStream parsedMessage = getInputStream(s);
        Model model = getModel(parsedMessage, repo, mapping);
        return model;
    }

//    public Model getModel(InputStream parsedMessage, Repository repo, URI mapping) throws IOException, URISyntaxException, RMLMapperException {
//        if (parsedMessage != null) {
//            if (mapping != null) {
//                switch (mapping.toString()) {
//                    case TURTLE:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.TURTLE);
//                    case JSON:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.JSONLD);
//                    case XML:
//                        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.RDFXML);
//                    default:
//                        return MappingUtil.getMappedModel(getTriplesMaps(repo, mapping), parsedMessage);
//                }
//            }
//            else {
//                throw new RMLMapperException("no mapping file selected!");
//            }
//        }
//        return new LinkedHashModel();
//    }

    private Model getModel(InputStream s, Repository repo, URI mapping){
        try {
            return Rio.parse(s, BASE_URI, RDFFormat.TURTLE);
        } catch (IOException e) {
            LOG.info("Message received is not Text/Turtle, trying to parse as RDF/XML");
        }

        try {
            return Rio.parse(s, BASE_URI, RDFFormat.RDFXML);
        } catch (IOException e) {
            LOG.info("Message received is not RDF/XML, trying to parse as JSON-LD");
        }

        try {
            return Rio.parse(s, BASE_URI, RDFFormat.JSONLD);
        } catch (IOException e) {
            LOG.info("Message received is not JSON-LD, trying to parse as InputStream");
        }

        try {
            return MappingUtil.getMappedModel(getTriplesMaps(repo, mapping), s);
        } catch (URISyntaxException e) {
            LOG.error("Parsing not possible!", e);
        }
        return new LinkedHashModel();
    }

    protected Object parseMessage(String s) {
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
