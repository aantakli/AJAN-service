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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import de.mosim.mmi.mmu.MSimulationEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:GenericAsyncBinding")
public class GenericAsyncInstruction extends AbstractAsyncInstruction {

    private String objects = "";
    private Map<String,String> objectIDs = new HashMap();
    private String mmu = "";
	@Getter @Setter
    private String cosimHost;
	@Getter @Setter
    private int cosimPort;
	private String properties;
	private Map<String,String> instProps;
	private String actionName;
	private String startCond = "";
	private String endCond = "";
	
	protected static final Logger LOG = LoggerFactory.getLogger(GenericAsyncInstruction.class);

    private final static String CONSUME = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?instruction mosim:mmu ?mmu .\n" +
			"	?instruction mosim:objects ?objects .\n" +
			"	?instruction mosim:mmuProperties ?properties .\n" +
			"	?instruction mosim:actionName ?actionName .\n" +
			"	?instruction mosim:startCondition ?startCond .\n" +
			"	?instruction mosim:endCondition ?endCond .\n" +
			"	?cosim rdf:type mosim:CoSimulator .\n" +
			"	?cosim mosim:host ?host .\n" +
			"	?cosim mosim:port ?port .\n" +
			"}";

    private final static String PRODUCE = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"PREFIX dct: <http://purl.org/dc/terms/>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?s ?p ?o .\n" +
			"}";

    @Override
    public IRI getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GenericAsyncBinding"); // Is not counted either
    }

	@Override
	public IRI getActionType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#Generic");
	}

	@Override
	protected String getSPARQLConsumes() {
		return CONSUME;
	}

	@Override
	protected String getSPARQLProduces() {
		return PRODUCE;
	}

    @Override
    public List<ActionVariable> getVariables() {
        List<ActionVariable> vars = new ArrayList();
        vars.add(new ActionVariable(vf.createBNode().getID(), "mmu"));
        vars.add(new ActionVariable(vf.createBNode().getID(), "object"));
        return vars;
    }

    @Override
    public String getLable() {
        return "GenericAsyncInstruction"; // to trigger this action AJAN places it under services:<getLable()>
    }

    @Override
    public String getDescription() {
        return "MOSIM MMU Generic Async Istruction";
    }

	@Override
    protected void readInput(final InputModel inputModel, final AgentTaskInformation info) {
		try {
			mmu = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_MMU);
			objects = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_OBJECTS);
			properties = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_MMU_PROPERTIES);
			actionName = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_ACTION_NAME);
			startCond = MOSIMUtil.getConditionInput(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_START_CONDITION),info);
			endCond = MOSIMUtil.getConditionInput(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_END_CONDITION),info);
			cosimHost = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_HOST);
			cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_PORT));
			if (!objects.equals("")) {
				objectIDs = MOSIMUtil.getObjectIDs(info,MOSIMUtil.OBJECT,objects);
			}
			instProps = MOSIMUtil.createGeneralProperties(properties, objectIDs, info);
		} catch (URISyntaxException ex) {
			return;
		}
    }

	@Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID) throws TException {
		MInstruction instruction = MOSIMUtil.createMInstruction(instID, actionID, mmu, instProps, null, startCond, endCond);
		return client.AssignInstruction(instruction, new HashMap<>()).Successful;
    }

	@Override
	public void setResponse(String id, Object response) {
		if (response instanceof MSimulationEvent) {
			MSimulationEvent event = (MSimulationEvent)response;
			if ((event.Type.equals("end") || event.Type.equals("PositioningFinished")) && event.Reference.equals(instID)) {
				ResultModel model = new ResultModel();
				Resource root = vf.createIRI(url);
				model.add(root, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(id));
				model.add(root, MOSIMVocabulary.HAS_FINISHED, vf.createLiteral(true));
				getEvent().setEventInformation(id, model);
			} else if (event.Type.equals("initError") && event.Reference.equals(instID)) {
				ResultModel model = new ResultModel();
				model.add(vf.createBNode(), RDF.TYPE, ACTNVocabulary.FAULT);
				getEvent().setEventInformation(id, model);
			}
		}
	}

	@Override
	protected Model getAddModel(final UUID id) {
		return getUpdateModel(id);
	}

	@Override
	protected Model getRemoveModel(final UUID id) {
		return getUpdateModel(id);
	}

	private Model getUpdateModel(final UUID id) {
		ResultModel model = new ResultModel();
		Resource root = vf.createIRI(url);
		if (!actionName.equals("")) {
			model.add(root, MOSIMVocabulary.HAS_ACTION_NAME, vf.createLiteral(actionName));
		}
		model.add(root, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
		model.add(root, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(instID));
		model.add(root, MOSIMVocabulary.HAS_MMU, vf.createLiteral(mmu));
		return model;
	}
}
