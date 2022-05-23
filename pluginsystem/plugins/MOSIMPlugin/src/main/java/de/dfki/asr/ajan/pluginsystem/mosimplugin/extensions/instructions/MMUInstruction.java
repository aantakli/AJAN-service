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
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:MMUBinding")
public class MMUInstruction extends AbstractInstruction {

    private String mmu = "";
    private String actionName;

    private ArrayList<Value> properties;
    private Map<String, String> instProps;

    private ArrayList<Value> constraints;
    private List<MConstraint> mConstraints = null;
	private String avatarID = "";
    private String startCond = "";
    private String endCond = "";

    private String instructionDef = "";

    @Getter
    @Setter
    private String cosimHost;
    @Getter
    @Setter
    private int cosimPort;

    protected static final Logger LOG = LoggerFactory.getLogger(AsyncMMUInstruction.class);

    private final static String CONSUME
        = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n"
        + "PREFIX actn: <http://www.ajan.de/actn#>\n"
        + "ASK\n"
        + "WHERE {\n"
        + "	?instruction mosim:mmu ?mmu .\n"
        + "	OPTIONAL { \n"
		+ "		?instruction mosim:avatarID ?avatarID .\n"
        + "		?instruction mosim:mmuProperty ?property .\n"
        + "		?instruction mosim:actionName ?actionName .\n"
        + "		?instruction mosim:constraint ?constraint .\n"
        + "		?instruction mosim:startCondition ?startCond .\n"
        + "		?instruction mosim:endCondition ?endCond .\n"
        + "	} \n"
        + "	?cosim rdf:type mosim:CoSimulator .\n"
        + "	?cosim mosim:host ?host .\n"
        + "	?cosim mosim:port ?port .\n"
        + "}";

    private final static String PRODUCE
        = "ASK\n"
        + "WHERE {\n"
        + "	?s ?p ?o .\n"
        + "}";

    @Override
    public IRI getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#MMUBinding"); // Is not counted either
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
        return "MMUInstruction"; // to trigger this action AJAN places it under services:<getLable()>
    }

    @Override
    public String getDescription() {
        return "MOSIM MMU Istruction";
    }

    @Override
    protected void readInput(final InputModel inputModel, final AgentTaskInformation info) {
        try {
            mmu = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_MMU);
			avatarID = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_AVATAR_ID);
			if (avatarID.isEmpty())
				avatarID = "";
            actionName = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_ACTION_NAME);
            properties = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_MMU_PROPERTY);
            instProps = MOSIMUtil.createGeneralProperties(properties, inputModel);
            constraints = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_CONSTRAINT);
            if (!constraints.isEmpty()) {
                mConstraints = MOSIMUtil.createConstraints(MOSIMUtil.getConstraintObj64(constraints, inputModel));
            }
            startCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_START_CONDITION);
            endCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_END_CONDITION);
            cosimHost = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_HOST);
            cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_PORT));
        } catch (URISyntaxException | NumberFormatException ex) {

        }
    }

    @Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID) throws TException {
        if (mmu.isEmpty()) {
            return false;
        }
        MInstruction instruction = MOSIMUtil.createMInstruction(instID, actionID, mmu, instProps, mConstraints, startCond, endCond);
        instructionDef = MOSIMUtil.getInstructionDef(instruction);
		Map<String, String> coSimProperties = new HashMap<>();
		coSimProperties.put("AvatarID", avatarID);
		return client.AssignInstruction(instruction, coSimProperties).Successful;
    }

    @Override
    public void setResponse(String id, Object response) {
        if (response instanceof ResultModel) {
            ResultModel model = (ResultModel) response;
            Resource root = vf.createBNode();
            model.add(root, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
            if (!actionName.equals("")) {
                model.add(root, MOSIMVocabulary.HAS_ACTION_NAME, vf.createLiteral(actionName));
            }
            model.add(root, MOSIMVocabulary.HAS_ACTION_URL, vf.createLiteral(url));
            model.add(root, MOSIMVocabulary.HAS_INSTRUCTION_ID, vf.createLiteral(instID));
            model.add(root, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(id));
            model.add(root, MOSIMVocabulary.HAS_MMU, vf.createLiteral(mmu));
            model.add(root, MOSIMVocabulary.HAS_TIMESTAMP, vf.createLiteral(MOSIMUtil.getTimeStamp()));
            if (!instructionDef.isEmpty()) {
                model.add(root, MOSIMVocabulary.HAS_JSON_INSTRUCTION, vf.createLiteral(instructionDef));
            }
        }
    }
}
