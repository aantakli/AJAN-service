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
import de.dfki.asr.ajan.behaviour.events.MappingEventInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.exceptions.*;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.vocabularies.MappingVocabulary;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerException;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.pf4j.Extension;

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

	@RDF("ajan:event")
	@Getter @Setter
	private URI event;

    @RDF("bt:validate")
    @Getter
    @Setter
    private BehaviorConstructQuery constructQuery;

    @RDF("rdfs:label")
    @Getter
    @Setter
    private String label;

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mapping#HandleMappingEvent");
    }

    @Override
    public NodeStatus executeLeaf() {
        try {
            if (handleEvent()) {
                String report = toString() + " SUCCEEDED";
                return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
            } else {
                String report = toString() + " FAILED";
                return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
            }
        } catch (InputMappingException ex) {
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to mapping errors", ex);
        }
    }

    protected boolean handleEvent() throws InputMappingException {
        boolean result = false;
        try {
			if (!checkEventGoalMatching()) {
				return result;
			}
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
            throw new InputMappingException(ex);
        }catch (RuntimeException ex) {
			this.getObject().getLogger().info(this.getClass(), "CARML Mapping Error!");
			this.getObject().getLogger().info(this.getClass(), "Malformed mapping file!");
            throw new InputMappingException(ex);
        }
        return result;
    }

    protected Model getModel() throws RMLMapperException, URISyntaxException, JsonProcessingException, RuntimeException, IOException, TransformerException {
		if (this.getObject().getEventInformation() instanceof MappingEventInformation) {
			MappingEventInformation mappingEvent = (MappingEventInformation)this.getObject().getEventInformation();
			InputStream resourceStream = MappingUtil.getResourceStream(mappingEvent.getObject());
			if (resourceStream != null) {
				if (mapping != null) {
					if (mapping.equals(new URI(MappingVocabulary.MIME_JSON.stringValue()))) {
						return MappingUtil.getJSONStatementModel(resourceStream);
					} else {
						Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
					return MappingUtil.getMappedModel(MappingUtil.getTriplesMaps(repo, mapping), resourceStream);
					}
				}
				else {
					throw new RMLMapperException("no mapping file selected!");
				}
			}
			return new LinkedHashModel();
		}
		else {
			throw new RMLMapperException("Event not a MappingEvent!");
		}
	}

	protected boolean checkEventGoalMatching() {
		if (this.getObject().getEventInformation() instanceof MappingEventInformation) {
			MappingEventInformation info = (MappingEventInformation)this.getObject().getEventInformation();
			boolean eventMatching = event != null && event.toString().equals(((MappingEventInformation) info).getEvent());
			boolean allEvents = event != null && event.toString().equals(AJANVocabulary.ALL.toString());
			return event == null || eventMatching || allEvents;
		}
		return false;
	}

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
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
