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
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import de.mosim.mmi.scene.MSceneObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:AbortInstruction")
public class AbortInstruction extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:host")
	@Getter @Setter
	private BehaviorSelectQuery query;

	private String host;
	private int port;

	@RDF("bt-mosim:instructionID")
	@Setter
	private BehaviorSelectQuery instructionIDs;

	protected static final Logger LOG = LoggerFactory.getLogger(AbortInstruction.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#AbortInstruction");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query, this.getObject());
			if(!hostMap.isEmpty()) {
				Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				if (abortInstruction()) {
					String report = toString() + " SUCCEEDED";
					LOG.info(report);
					return new LeafStatus(Status.SUCCEEDED, report);
				};
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED";
			LOG.info(report);
			return new LeafStatus(Status.FAILED, report);
		}
		String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
	}

	private boolean abortInstruction() throws URISyntaxException {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
				List<String> ids = getInstructionIDs();
				if (ids.isEmpty()) {
					client.Abort();
				} else if (ids.size() == 1) {
					client.AbortInstruction(ids.get(0));
				} else {
					client.AbortInstructions(ids);
				}
				transport.close();
				return true;
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MSceneObject>", ex);
			return false;
		}
	}

	private List<String> getInstructionIDs() throws URISyntaxException {
		List<String> instructions = new ArrayList();
		if (instructionIDs != null) {
			Repository repo = BTUtil.getInitializedRepository(this.getObject(), instructionIDs.getOriginBase());
			List<BindingSet> result = instructionIDs.getResult(repo);
			if (!result.isEmpty()) {
				Iterator<BindingSet> itr = result.iterator();
				while (itr.hasNext()) {
					instructions.add(itr.next().getValue("id").stringValue());
				}
			}
		}
		return instructions;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "AbortInstruction (" + getLabel() + ")";
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
