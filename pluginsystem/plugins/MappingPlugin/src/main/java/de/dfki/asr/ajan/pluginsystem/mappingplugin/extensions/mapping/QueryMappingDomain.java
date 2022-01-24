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
package de.dfki.asr.ajan.pluginsystem.mappingplugin.extensions.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.messages.QueryDomain;
import de.dfki.asr.ajan.behaviour.nodes.messages.SyncMessage;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.behaviour.service.impl.HttpHeader;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.AgentUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.RMLMapperException;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.vocabularies.MappingVocabulary;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:QueryMappingDomain")
public class QueryMappingDomain extends SyncMessage implements NodeExtension {

    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter
    @Setter
    private String label;

    @RDF("bt:mapping")
    @Getter
    @Setter
    private URI mapping;

    @RDF("bt:targetBase")
    @Getter
    @Setter
    private URI targetBase;

    @RDF("bt:queryUri")
    @Getter
    @Setter
    private BehaviorSelectQuery queryURI;

    private Model domainResponse;
	private Model mappingFile;

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mapping#QueryMappingDomain");
    }

    @Override
    public Status execute() {
        HttpBinding bng = new HttpBinding();
        try {
            bng.setMethod(new URI(AJANVocabulary.HTTP_METHOD_GET.toString()));
			setMappingFile();
            bng.setHeaders(getHeaders());
            super.setUrl(url);
            super.setQueryURI(queryURI);
            super.setBinding(bng);
        } catch (URISyntaxException | RMLMapperException ex) {
            Logger.getLogger(QueryDomain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.execute();
    }

	protected void setMappingFile() throws URISyntaxException, RMLMapperException {
		if (mapping != null) {
			Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
			mappingFile = MappingUtil.getTriplesMaps(repo, mapping);
			if (mappingFile == null) {
				throw new RMLMapperException("Mapping file could not be read!");
			}
		}
		else {
			throw new RMLMapperException("no mapping file selected!");
		}
	}

    private List<HttpHeader> getHeaders() throws URISyntaxException {
        List list = new ArrayList();
        HttpHeader accept = new HttpHeader();
        accept.setHeaderName(new URI(AJANVocabulary.HTTP_HEADER_ACCEPT.toString()));
        accept.setHeaderValue(readePossibleAcceptHeaders());
        list.add(accept);
        return list;
    }

	private String readePossibleAcceptHeaders() {
		StringBuilder accepts = new StringBuilder();
		Model filtered = mappingFile.filter(null, MappingVocabulary.RML_REFERENCE_FORMULATION, null);
		for (Value objects: filtered.objects()) {
			switch(objects.stringValue()) {
				case "http://semweb.mmlab.be/ns/ql#JSONPath":
					accepts.append("application/json");
					break;
				case "http://semweb.mmlab.be/ns/ql#XPath":
					accepts.append("application/xml, text/xml");
					break;
				case "http://semweb.mmlab.be/ns/ql#CSV":
					accepts.append("text/csv");
					break;
				default: 
					accepts.append("text/turtle");
					break;
			}
		}
		return accepts.toString();
	}

    @Override
    protected boolean checkResponse(final Object response) {
        if (response instanceof Model) {
            domainResponse = (Model) response;
        } else {
			try {
                domainResponse = getModel(response);
            } catch (URISyntaxException | TransformerException | IOException ex) {
                LOG.error("Malformed response!");
				LOG.error("Mime Type is not supported!");
                return false;
            } catch (RuntimeException ex) {
                LOG.error("CARML Mapping Error!");
				LOG.error("Malformed mapping file!");
                return false;
            }
        }
        return updateBeliefs(modifyResponse(domainResponse), targetBase);
    }

    protected Model getModel(final Object response) throws URISyntaxException, IOException, RuntimeException, JsonProcessingException, TransformerException {
		InputStream resourceStream = MappingUtil.getResourceStream(response);
        return MappingUtil.getMappedModel(mappingFile, resourceStream);
    }

    @Override
    protected Model modifyResponse(final Model model) {
        return AgentUtil.setNamedGraph(model.iterator(), super.requestURI);
    }

    @Override
    public void end() {
        LOG.info("Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "QueryMappingDomain (" + getLabel() + ")";
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        if (mode.equals(BTUtil.ModelMode.DETAIL)) {
            Resource resource = getInstance(root.getInstance());
            queryURI.setResultModel(resource, BTVocabulary.QUERY_URI_RESULT, model);
            if (domainResponse != null && !domainResponse.isEmpty()) {
                Resource resultNode = vf.createBNode();
                model.add(resource, BTVocabulary.VALIDATE_RESULT, resultNode);
                model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.RESPONSE_RESULT);
                model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.GRAPH_RESULT);
                Resource resultGraph = BTUtil.setGraphResultModel(model, domainResponse);
                model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
            }
        }
        return super.getModel(model, root, mode);
    }
}
