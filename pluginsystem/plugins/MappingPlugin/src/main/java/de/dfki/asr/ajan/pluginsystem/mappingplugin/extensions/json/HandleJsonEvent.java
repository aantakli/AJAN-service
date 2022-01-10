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

import be.ugent.rml.store.RDF4JStore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.*;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:HandleJsonEvent")
public class HandleJsonEvent extends AbstractTDBLeafTask implements NodeExtension {
    @RDFSubject
    @Getter @Setter
    private String url;

	@RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

    @RDF("bt:mappings")
    @Getter @Setter
    private List<URI> mappings;

    @RDF("bt:validate")
    @Getter @Setter
    private BehaviorConstructQuery constructQuery;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

    protected static final Logger LOG = LoggerFactory.getLogger(HandleJsonEvent.class);

	@Override
	public Resource getType() {
		ValueFactory vf = SimpleValueFactory.getInstance();
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
            } catch (JSONMappingException | IOException ex) {
                    LOG.debug(toString() + ex);
                    LOG.info(toString() + " FAILED due to mapping errors");
                    return  new LeafStatus(Status.FAILED, toString() + " FAILED due to mapping errors");
            }
    }

    protected boolean handleEvent() throws JSONMappingException, IOException {
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
            } catch ( URISyntaxException | RMLMapperException ex) {
                    throw new JSONMappingException(ex);
            }
            return result;
    }

    protected Model getModel() throws RMLMapperException, URISyntaxException {
            Model model = new LinkedHashModel();
            if (this.getObject().getEventInformation() instanceof ObjectNode) {
                    ObjectNode input = (ObjectNode)this.getObject().getEventInformation();
                    Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
					RDF4JStore rmlStore;
					if (mapping == null) {
						rmlStore = new RDF4JStore(MappingUtil.getTriplesMaps(repo, mappings));
					} else {
						rmlStore = new RDF4JStore(MappingUtil.getTriplesMaps(repo, mapping));
					}
                    Model rrmapping = MappingUtil.loadJsonMapping(input, rmlStore);
                    model = SPARQLUtil.queryModel(rrmapping, constructQuery.getSparql());
            }
            return model;
    }

    @Override
    public void end() {
            LOG.info("Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
            return "ValidateJMappingEvent (" + getLabel() + ")";
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
