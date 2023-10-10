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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:AsyncMMUBinding")
public class AsyncMMUInstruction extends AbstractAsyncInstruction {

	private Resource instRoot = null;
	private String timeStamp = "";

	private InstructionParameters parameters;
    private String actionID;
	private String instructionDef = "";

    private final static String CONSUME = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?instruction mosim:mmu ?mmu .\n" +
			"	OPTIONAL { \n" +
			"		?instruction mosim:avatarID ?avatarID .\n" +
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
    protected InstructionParameters readInput(final InputModel inputModel, final AgentTaskInformation info) {
		parameters = new InstructionParameters(inputModel);
		instID = parameters.getInstID();
		return parameters;
    }

	@Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID, final InstructionParameters parameters) throws TException {
        this.actionID = actionID;
        if (parameters.getMmu().isEmpty()) 
            return false;
        return assignInstruction(client);
    }
   
    private boolean assignInstruction(final MCoSimulationAccess.Client client) throws TException {
        MInstruction instruction = MOSIMUtil.createMInstruction(instID, actionID, parameters);
		instructionDef = MOSIMUtil.getInstructionDef(instruction);
		LOG.info(instructionDef);
		//logInstruction(instructionDef);
		Map<String, String> coSimProperties = new HashMap<>();
        boolean result = client.AssignInstruction(instruction, coSimProperties).Successful;
        this.actionID = "";
        return result;
    }

	private void logInstruction(String instructionDef) {
		File logs = new File(getClass().getClassLoader().getResource("instructions.txt").getFile());
		try {
			try (FileWriter fr = new FileWriter(logs, true); 
					BufferedWriter br = new BufferedWriter(fr); 
					PrintWriter pr = new PrintWriter(br)) {
				pr.println(instructionDef);
			}
		} catch (IOException ex) {
			return;
		}
	}

	@Override
	public void setResponse(String id, Object response) {
		if (response instanceof MSimulationEvent) {
			MSimulationEvent event = (MSimulationEvent)response;
			if ("PositioningFinished".equals(event.Type) && instID.equals(event.Reference)) {
				setResult(id, ACTNVocabulary.SUCCESS);
			} else if ("initError".equals(event.Type) && instID.equals(event.Reference)) {
				setResult(id, ACTNVocabulary.FAULT);
			} else if (parameters.getFinalEvent().equals(event.Type) && instID.equals(event.Reference)) {
				setResult(id, ACTNVocabulary.SUCCESS);
			}
		}
	}

	private void setResult(String id, IRI object) {
		ResultModel model = new ResultModel();
		model.add(this.instRoot, RDF.TYPE, object);
		getEvent().setEventInformation(id, model);
		this.instID = "";
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
		if (!parameters.getActionName().equals("")) {
			model.add(instRoot, MOSIMVocabulary.HAS_ACTION_NAME, vf.createLiteral(parameters.getActionName()));
		}
		model.add(instRoot, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
		model.add(instRoot, MOSIMVocabulary.HAS_INSTRUCTION_ID, vf.createLiteral(instID));
		model.add(instRoot, MOSIMVocabulary.HAS_ACTION_URL, vf.createLiteral(url));
		model.add(instRoot, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(id.toString()));
		model.add(instRoot, MOSIMVocabulary.HAS_TIMESTAMP, vf.createLiteral(timeStamp));
		model.add(instRoot, MOSIMVocabulary.HAS_MMU, vf.createLiteral(parameters.getMmu()));
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
			try (TTransport transport = new TSocket(parameters.getCosimHost(), parameters.getCosimPort())) {
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
