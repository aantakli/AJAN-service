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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions.specificnodes;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions.AbstractInstruction;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import de.mosim.mmi.mmu.MSimulationEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:IdleBinding")
public class IdleInstruction extends AbstractInstruction {

	@Getter @Setter
    private Resource avatar;
	@Getter @Setter
    private String mmu = "Pose/Idle";

	@Getter @Setter
    private String cosimHost;
	@Getter @Setter
    private int cosimPort;

    private final static String CONSUME = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?avatar rdf:type mosim:Avatar .\n" +
			"	?cosim rdf:type mosim:CoSimulator .\n" +
			"	?cosim mosim:host ?host .\n" +
			"	?cosim mosim:port ?port .\n" +
			"}";

    private final static String PRODUCE = 
            "PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"PREFIX dct: <http://purl.org/dc/terms/>\n" +
			"ASK \n" +
			"WHERE {\n" +
			"	?avatar mosim:idled ?done .\n" +
			"	OPTIONAL {\n" +
			"		?response rdf:type actn:FAULT .\n" +
			"		?response dct:description ?message }\n" +
			"}";

    @Override
    public IRI getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#IdleBinding"); // Is not counted either
    }

	@Override
	public IRI getActionType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#Idle");
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
		vars.add(new ActionVariable(vf.createBNode().getID(), "avatar"));
        return vars;
    }

    @Override
    public String getLable() {
        return "IdleInstruction"; // to trigger this action AJAN places it under services:<getLable()>
    }

    @Override
    public String getDescription() {
        return "MOSIM MMU Idle Istruction";
    }

	@Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID) throws TException {
		MInstruction idle = MOSIMUtil.createMInstruction(instID, actionID, mmu, null, null, null, null);
		return client.AssignInstruction(idle, new HashMap<>()).Successful;
    }

	@Override
    protected void readInput(final InputModel inputModel, final AgentTaskInformation info) {
		avatar = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.AVATAR);
		Resource cosim = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.CO_SIMULATOR);
		cosimHost = MOSIMUtil.getObject(inputModel, cosim, MOSIMVocabulary.HAS_HOST);
		cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, cosim, MOSIMVocabulary.HAS_PORT));
    }

	@Override
	public void setResponse(String id, Object response) {
		if (response instanceof ResultModel) {
			Resource idle = vf.createBNode();
			ResultModel model = (ResultModel)response;
			model.add(avatar, vf.createIRI("http://www.dfki.de/mosim-ns#idled"), vf.createLiteral("done"));
			model.add(avatar, MOSIMVocabulary.HAS_INSTRUCTION, idle);
			model.add(idle, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.INSTRUCTION);
			model.add(idle, MOSIMVocabulary.HAS_ACTION_ID, vf.createLiteral(instID));
			model.add(idle, MOSIMVocabulary.HAS_MMU, vf.createLiteral(mmu));
		}
	}
}
