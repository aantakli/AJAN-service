/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.data;

import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.exceptions.InitializationRDFValidationException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.repository.Repository;

public class AgentModelManager {

	@Getter
	private final Repository agentRepo;

	private final AgentResourceManager resourceManager;

	public AgentModelManager(final Repository repo) {
		this.agentRepo = repo;
		this.resourceManager = new AgentResourceManager();
	}

	public Model getTemplateFromTDB(final Repository repo, final Resource resource) {
		QueryBuilder builder = QueryBuilderFactory.describe(resource);
		ParsedQuery query = builder.query();
		Model template = SPARQLUtil.queryRepository(repo, query);
		if (template == null) {
			throw new InitializationRDFValidationException("Template " + resource.stringValue() + " not found");
		}
		if (template.isEmpty()) {
			throw new InitializationRDFValidationException("Template " + resource.stringValue() + " was empty");
		}
		return template;
	}

        @SuppressWarnings("PMD.ExcessiveParameterList")
	public Model getAgentInitKnowledge(final Resource agentURI, final Resource agentResource, final Model initAgentModel, final boolean repo) {
            Iterator<Resource> resourcesIter = resourceManager.getResources(agentResource, initAgentModel, AJANVocabulary.AGENT_HAS_INITKNOWLEDGE);
            if (resourcesIter.hasNext()){
                Model initKnowledge;
                ParsedGraphQuery query = SPARQLUtil.getDescribeQuery(resourcesIter);
                if (repo) {
                    initKnowledge = SPARQLUtil.queryRepository(agentRepo, query);
                } else {
                    initKnowledge = SPARQLUtil.queryModel(initAgentModel, query);
                }
                return setAgentURI(agentURI, agentResource, initKnowledge);
            } else {
                return new LinkedHashModel();
            }
	}

        public String getReportURI(final Model initAgentModel) {
            String report = "";
            Optional<Literal> reportLiteral = Models.objectLiteral(initAgentModel.filter(null, AJANVocabulary.AGENT_HAS_REPORT_URI, null));
            if (reportLiteral.isPresent()) {
                Literal uri = reportLiteral.get();
                if (uri.getDatatype().equals(XMLSchema.ANYURI)) {
                    report = uri.stringValue();
                }
            }
            return report;
        }

        public Model setAgentURI(final Resource agentURI, final Resource agentResource, final Model initAgentModel) {
            Set<Value> initStmts = initAgentModel.filter(agentResource, AJANVocabulary.AGENT_HAS_INITKNOWLEDGE, null).objects();
            initStmts.forEach((obj) -> {
                initAgentModel.add(agentURI, AJANVocabulary.AGENT_HAS_INITKNOWLEDGE, obj);
            });
            initAgentModel.remove(agentResource, AJANVocabulary.AGENT_HAS_INITKNOWLEDGE, null);
            return initAgentModel;
        }

	public String getLabel(final Model model, final String errorText) {
		Optional<Literal> name = Models.objectLiteral(model);
		if (!name.isPresent()) {
			throw new InitializationRDFValidationException(errorText);
		}
		Literal nameLiteral = name.get();
		if (nameLiteral.getLabel().isEmpty()) {
			throw new InitializationRDFValidationException(errorText);
		}
		return nameLiteral.getLabel();
	}

	public Optional<IRI> getTypeIRI(final Model model, final String context) {
		Optional<IRI> type = Models.objectIRI(model);
		if (!type.isPresent()) {
			throw new InitializationRDFValidationException("No " + context + "-literal for " + RDF.TYPE + " defined");
		}
		return type;
	}
}
