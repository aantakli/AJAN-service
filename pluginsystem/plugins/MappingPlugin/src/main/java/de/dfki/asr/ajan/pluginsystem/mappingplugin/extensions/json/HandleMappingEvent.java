/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.dfki.asr.ajan.pluginsystem.mappingplugin.extensions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.logical_source_resolver.CsvResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.*;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.xerces.dom.DeferredDocumentImpl;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:HandleMappingEvent")
public class HandleMappingEvent extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("bt:mapping")
    @Getter
    @Setter
    private URI mapping;

    @RDF("bt:mappings")
    @Getter
    @Setter
    private List<URI> mappings;

    @RDF("bt:validate")
    @Getter
    @Setter
    private BehaviorConstructQuery constructQuery;

    @RDF("rdfs:label")
    @Getter
    @Setter
    private String label;

    protected static final Logger LOG = LoggerFactory.getLogger(HandleMappingEvent.class);

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mapping#ValidateJsonEvent");
    }

    @Override
    public LeafStatus executeLeaf() {
        try {
            if (handleEvent()) {
                String report = toString() + " SUCCEEDED";
                LOG.info(report);
                return new LeafStatus(Status.SUCCEEDED, report);
            } else {
                String report = toString() + " FAILED";
                LOG.info(report);
                return new LeafStatus(Status.FAILED, report);
            }
        } catch (JSONMappingException ex) {
            LOG.debug(toString() + ex);
            LOG.info(toString() + " FAILED due to mapping errors");
            return new LeafStatus(Status.FAILED, toString() + " FAILED due to mapping errors");
        }
    }

    protected boolean handleEvent() throws JSONMappingException {
        boolean result = false;
        try {
            Model model = getModel();
            if (!model.isEmpty()) {
                if (constructQuery.getTargetBase().equals(new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString()))) {
                    this.getObject().getExecutionBeliefs().update(model);
                } else if (constructQuery.getTargetBase().equals(new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString()))) {
                    this.getObject().getAgentBeliefs().update(model);
                }
                result = true;
            }
        } catch (URISyntaxException | RMLMapperException | TransformerException | IOException ex) {
            throw new JSONMappingException(ex);
        }
        return result;
    }

    protected Model getModel() throws RMLMapperException, URISyntaxException, JsonProcessingException, IOException, TransformerException {
        InputStream resourceStream = getResourceStream();
        if (resourceStream != null) {
            Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
            Set<TriplesMap> mappingInput;
            if (mapping == null) {
                mappingInput = RmlMappingLoader.build().load(MappingUtil.getTriplesMaps(repo, mappings));
            } else {
                mappingInput = RmlMappingLoader.build().load(MappingUtil.getTriplesMaps(repo, mapping));
            }
            RmlMapper mapper = RmlMapper.newBuilder()
                .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                .setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
                .setLogicalSourceResolver(Rdf.Ql.Csv, new CsvResolver())
                .build();

            mapper.bindInputStream(resourceStream);
            return mapper.map(mappingInput);
        }
        return new LinkedHashModel();
    }

    private InputStream getResourceStream() throws JsonProcessingException, IOException, TransformerException {
        Object eventInfo =  this.getObject().getEventInformation();
        if (eventInfo instanceof JsonNode) {
            return getJsonResourceStream(eventInfo);
        }
		if (eventInfo instanceof DeferredDocumentImpl) {
			return getXMLResourceStream(eventInfo);
        }
        return null;
    }

    private InputStream getJsonResourceStream(final Object eventInfo) throws JsonProcessingException, IOException {
        JsonNode input = (JsonNode) eventInfo;
		ObjectMapper om = new ObjectMapper();
		ObjectWriter writer = om.writer();
        return new ByteArrayInputStream(writer.writeValueAsBytes(input));
    }

    private InputStream getXMLResourceStream(final Object eventInfo) throws IOException, TransformerException {
		DeferredDocumentImpl input = (DeferredDocumentImpl) eventInfo;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(input);
		StreamResult console = new StreamResult(baos);
		transformer.transform(source, console);
		return new ByteArrayInputStream(baos.toByteArray());
    }

    /*private InputStream getCsvResourceStream(final Object eventInfo) throws JsonProcessingException {
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(input));
    }*/

    @Override
    public void end() {
        LOG.info("Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "HandleMappingEvent (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        if (mode.equals(BTUtil.ModelMode.DETAIL) && constructQuery != null) {
            constructQuery.setResultModel(getInstance(root.getInstance()), BTVocabulary.VALIDATE_RESULT, model);
        }
        return super.getModel(model, root, mode);
    }
}
