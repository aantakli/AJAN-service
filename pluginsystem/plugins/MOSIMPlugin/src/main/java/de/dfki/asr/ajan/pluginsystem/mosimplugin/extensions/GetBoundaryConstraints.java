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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions.InstructionParameters;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import de.mosim.mmi.mmu.MInstruction;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:GetBoundaryConstraints")
public class GetBoundaryConstraints extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:instruction")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetBoundaryConstraints");
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			InstructionParameters parameters = new InstructionParameters(getInputModel());
			List<MConstraint> constraints = getBoundaries(parameters);
			if (constraints == null) {
				String report = toString() + " FAILED";
				return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
			}
			MOSIMUtil.writeInput(getResultModel(constraints, parameters), repository.toString(), this.getObject());
			String report = toString() + " SUCCEEDED";
			return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
		} catch (URISyntaxException | IOException ex) {
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
		}
	}

	private Model getInputModel() throws URISyntaxException {
		Repository origin = BTUtil.getInitializedRepository(getObject(), query.getOriginBase());
		return query.getResult(origin);
	}

	private Model getResultModel(final List<MConstraint> constraints, InstructionParameters parameters) throws IOException {
		Model model = new LinkedHashModel();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		Resource subject = vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetBoundaryConstraints/" + parameters.getActionName());
		model.add(subject, rdfType, MOSIMVocabulary.BOUNDERY_CONSTRAINTS);
		for (MConstraint contst: constraints) {
			MOSIMUtil.setConstraint(model, subject, contst);
		}
		return model;
	}

	private List<MConstraint> getBoundaries(final InstructionParameters parameters) {
		try {
			try (TTransport transport = new TSocket(parameters.getCosimHost(), parameters.getCosimPort())) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
				MInstruction instruction = MOSIMUtil.createMInstruction(parameters.getActionName() + "_1", parameters.getActionName(), parameters);
				return client.GetBoundaryConstraints(instruction);
			}
		} catch (TException ex) {
			this.getObject().getLogger().info(this.getClass(), "Could not load List<MConstraint>", ex);
			return null;
		}
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GetBoundaryConstraints (" + getLabel() + ")";
	}
	
	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
