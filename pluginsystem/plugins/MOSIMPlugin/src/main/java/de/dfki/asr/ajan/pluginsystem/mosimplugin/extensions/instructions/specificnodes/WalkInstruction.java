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
import de.dfki.asr.ajan.common.SPARQLUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions.AbstractAsyncInstruction;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.constraints.MGeometryConstraint;
import de.mosim.mmi.mmu.MInstruction;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import de.mosim.mmi.math.MTransform;
import de.mosim.mmi.mmu.MSimulationEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt-mosim:WalkBinding")
public class WalkInstruction extends AbstractAsyncInstruction {

	@Getter @Setter
    private Resource avatar;
	@Getter @Setter
    private Resource target;
	@Getter @Setter
    private List<MConstraint> constraints;
	@Getter @Setter
    private String targetID;
	@Getter @Setter
    private String mmu = "Locomotion/Walk";

	@Getter @Setter
    private String cosimHost;
	@Getter @Setter
    private int cosimPort;
	
	protected static final Logger LOG = LoggerFactory.getLogger(WalkInstruction.class);

    private final static String CONSUME = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX mosim: <http://www.dfki.de/mosim-ns#>\n" +
			"PREFIX actn: <http://www.ajan.de/actn#>\n" +
			"ASK\n" +
			"WHERE {\n" +
			"	?avatar rdf:type mosim:Avatar .\n" +
			"	?avatar mosim:isLocatedAt ?position .\n" +
			"	OPTIONAL { \n" +
			"		?target rdf:type mosim:MSceneObject .\n" +
			"		?target mosim:id ?id .\n" +
			"	} \n" +
			"	OPTIONAL { \n" +
			"		?transform rdf:type mosim:MTransform .\n" +
			"		?transform mosim:id ?id .\n" +
			"		?transform mosim:posX ?posX .\n" +
			"		?transform mosim:posY ?posY .\n" +
			"		?transform mosim:posZ ?posZ .\n" +
			"		?transform mosim:rotX ?rotX .\n" +
			"		?transform mosim:rotY ?rotY .\n" +
			"		?transform mosim:rotZ ?rotZ .\n" +
			"		?transform mosim:rotW ?rotW .\n" +
			"	} \n" +
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
			"	?avatar mosim:isLocatedAt ?target .\n" +
			"	FILTER NOT EXISTS {\n" +
			"		?avatar mosim:isLocatedAt ?position .\n" +
			"		FILTER (?position != ?target)\n" +
			"	}\n" +
			"	OPTIONAL {\n" +
			"		?response rdf:type actn:FAULT .\n" +
			"		?response dct:description ?message .\n "
			+ "}\n" +
			"}";

    @Override
    public IRI getType() {
        return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#WalkBinding"); // Is not counted either
    }

	@Override
	public IRI getActionType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#Walk");
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
        vars.add(new ActionVariable(vf.createBNode().getID(), "mmu"));
		vars.add(new ActionVariable(vf.createBNode().getID(), "position"));
        vars.add(new ActionVariable(vf.createBNode().getID(), "target"));
        return vars;
    }

    @Override
    public String getLable() {
        return "WalkInstruction"; // to trigger this action AJAN places it under services:<getLable()>
    }

    @Override
    public String getDescription() {
        return "MOSIM MMU Walk Istruction";
    }

	@Override
    protected void readInput(final InputModel inputModel, final AgentTaskInformation info) {
        avatar = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.AVATAR);
		
		target = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.M_SCENE_OBJECT);
		if (target == null) {
			target = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.M_TRANSFORM);
			constraints = getWalkConstraints(inputModel);
			targetID = constraints.get(0).ID;
		} else {
			targetID = MOSIMUtil.getObject(inputModel, target, MOSIMVocabulary.HAS_ID);
		}

		Resource cosim = MOSIMUtil.getResource(inputModel, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.CO_SIMULATOR);
		cosimHost = MOSIMUtil.getObject(inputModel, cosim, MOSIMVocabulary.HAS_HOST);
		cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, cosim, MOSIMVocabulary.HAS_PORT));
    }

	@Override
    protected boolean performOperation(final MCoSimulationAccess.Client client, final String actionID) throws TException {
		MInstruction walk = MOSIMUtil.createMInstruction(instID, actionID, mmu, MOSIMUtil.createWalkProperties(targetID), constraints, null, null);
		return client.AssignInstruction(walk, new HashMap<>()).Successful;
    }

	private List<MConstraint> getWalkConstraints(final InputModel inputModel) {
		List<MConstraint> list = new ArrayList();
		List<String> varNames = MOSIMUtil.getTransformVars();
		ParsedTupleQuery selectQuery = SPARQLUtil.getSelectQuery(MOSIMUtil.MTRANSFORM, varNames);
		List<BindingSet> bindings = SPARQLUtil.queryModel((Model)inputModel, selectQuery);
		try {
			MConstraint constraint = new MConstraint();
			constraint.ID = "ajan_const_" + 1;
			MGeometryConstraint geo = new MGeometryConstraint();
			geo.ParentObjectID = "";
			MTransform trans = MOSIMUtil.getTransform(bindings.get(0));
			geo.ParentToConstraint = trans;
			constraint.GeometryConstraint = geo;
			list.add(constraint);
			return list;
		} catch (URISyntaxException ex) {
			return null;
		}
	}

	@Override
	public void setResponse(String id, Object response) {
		if (response instanceof MSimulationEvent) {
			MSimulationEvent event = (MSimulationEvent)response;
			if (event.Type.equals("end") && event.Reference.equals(instID)) {
				ResultModel model = new ResultModel();
				model.add(avatar, MOSIMVocabulary.IS_LOCATED_AT, vf.createLiteral(targetID));
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
		return new LinkedHashModel();
	}

	@Override
	protected Model getRemoveModel(final UUID id) {
		return new LinkedHashModel();
	}
}
