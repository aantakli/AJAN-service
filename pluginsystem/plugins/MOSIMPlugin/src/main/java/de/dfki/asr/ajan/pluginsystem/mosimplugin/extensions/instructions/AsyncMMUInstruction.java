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
import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.core.MBoolResponse;
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
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:AsyncMMUBinding")
public class AsyncMMUInstruction extends AbstractAsyncInstruction {

    private String mmu = "";
	private String finalEvent = "end";
	private String actionName;

	private ArrayList<Value> properties;
	private Map<String,String> instProps;

	private ArrayList<Value> constraints;
	private List<MConstraint> mConstraints = null;

	private String startCond = "";
	private String endCond = "";
	private Resource instRoot = null;
	private String timeStamp = "";

	private String instructionDef = "";
	
	@Getter @Setter
    private String cosimHost;
	@Getter @Setter
    private int cosimPort;
	
	protected static final Logger LOG = LoggerFactory.getLogger(AsyncMMUInstruction.class);

    private final static String CONSUME = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?instruction mosim:mmu ?mmu .\n" +
			"	OPTIONAL { \n" +
			"		?instruction mosim:finalEvent ?event .\n" +
			"		?instruction mosim:mmuProperty ?property .\n" +
			"		?instruction mosim:actionName ?actionName .\n" +
			"		?instruction mosim:constraint ?constraint .\n" +
			"		?instruction mosim:startCondition ?startCond .\n" +
			"		?instruction mosim:endCondition ?endCond .\n" +
			"	} \n" +
			"	?cosim rdf:type mosim:CoSimulator .\n" +
			"	?cosim mosim:host ?host .\n" +
			"	?cosim mosim:port ?port .\n" +
			"}";

    private final static String PRODUCE = 
			"ASK\n" +
			"WHERE {\n" +
			"	?s ?p ?o .\n" +
			"}";

    @Override
    public IRI getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#AsyncMMUBinding"); // Is not counted either
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
        return vars;
    }

    @Override
    public String getLable() {
        return "AsyncMMUInstruction"; // to trigger this action AJAN places it under services:<getLable()>
    }

    @Override
    public String getDescription() {
        return "MOSIM Async MMU Istruction";
    }

	@Override
    protected void readInput(final InputModel inputModel, final AgentTaskInformation info) {
		try {
			mmu = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_MMU);
			finalEvent = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_FINAL_EVENT);
			if (finalEvent.isEmpty())
				finalEvent = "end";
			actionName = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_ACTION_NAME);
			properties = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_MMU_PROPERTY);
			instProps = MOSIMUtil.createGeneralProperties(properties, inputModel);
			constraints = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_CONSTRAINT);
			if (!constraints.isEmpty())
				mConstraints = MOSIMUtil.createConstraints(MOSIMUtil.getConstraintObj64(constraints, inputModel));
			startCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_START_CONDITION);
			endCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_END_CONDITION);
			cosimHost = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_HOST);
			cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_PORT));
			LOG.info(mmu);
		} catch (URISyntaxException ex) {

		}
    }

	@Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID) throws TException {
		if (mmu.isEmpty()) 
			return false;
		MInstruction instruction = MOSIMUtil.createMInstruction(instID, actionID, mmu, instProps, mConstraints, startCond, endCond);
		instructionDef = MOSIMUtil.getInstructionDef(instruction);
		return client.AssignInstruction(instruction, new HashMap<>()).Successful;
    }

	@Override
	public void setResponse(String id, Object response) {
		if (response instanceof MSimulationEvent) {
			MSimulationEvent event = (MSimulationEvent)response;
			if (event.Type.equals(finalEvent) && event.Reference.equals(instID)) {
				ResultModel model = new ResultModel();
				model.add(instRoot, RDF.TYPE, ACTNVocabulary.SUCCESS);
				getEvent().setEventInformation(id, model);
				instID = "";
			} else if (event.Type.equals("initError") && event.Reference.equals(instID)) {
				ResultModel model = new ResultModel();
				model.add(instRoot, RDF.TYPE, ACTNVocabulary.FAULT);
				getEvent().setEventInformation(id, model);
				instID = "";
			}
		}
	}

	@Override
	protected Model getAddModel(final UUID id) {
		instRoot = vf.createBNode();
		timeStamp = MOSIMUtil.getTimeStamp();
		return getUpdateModel(id);
	}

	@Override
	protected Model getRemoveModel(final UUID id) {
		return getUpdateModel(id);
	}

	private Model getUpdateModel(final UUID id) {
		ResultModel model = new ResultModel();
		if (!actionName.equals("")) {
			model.add(instRoot, MOSIMVocabulary.HAS_ACTION_NAME, vf.createLiteral(actionName));
		}
		model.add(instRoot, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
		model.add(instRoot, MOSIMVocabulary.HAS_INSTRUCTION_ID, vf.createLiteral(instID));
		model.add(instRoot, MOSIMVocabulary.HAS_ACTION_URL, vf.createLiteral(url));
		model.add(instRoot, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(id.toString()));
		model.add(instRoot, MOSIMVocabulary.HAS_TIMESTAMP, vf.createLiteral(timeStamp));
		model.add(instRoot, MOSIMVocabulary.HAS_MMU, vf.createLiteral(mmu));
		if (!instructionDef.isEmpty())
				model.add(instRoot, MOSIMVocabulary.HAS_JSON_INSTRUCTION, vf.createLiteral(instructionDef));
		return model;
	}

	@Override
    public ResultModel abort(InputModel inputModel) {
        abortInstruction();
        return super.abort(inputModel);
    }

	private void abortInstruction() {
		try {
			try (TTransport transport = new TSocket(cosimHost, cosimPort)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
				client.AbortInstruction(instID);
				transport.close();
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MSceneObject>", ex);
		}
	}
}
