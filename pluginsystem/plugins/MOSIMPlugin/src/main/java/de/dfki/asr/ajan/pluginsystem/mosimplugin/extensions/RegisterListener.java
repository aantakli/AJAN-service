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
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import static de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.ThriftPluginServer.THRIFT_HOST;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.core.MBoolResponse;
import de.mosim.mmi.core.MIPAddress;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.springframework.stereotype.Component;
import org.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:RegisterListener")
public class RegisterListener extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:coSim")
	@Getter @Setter
	private BehaviorSelectQuery query;

	private String host;
	private int port;
	private String avatarId;
	private String eventtype;

	@RDF("bt-mosim:callback")
	@Getter @Setter
	private BehaviorSelectQuery callback;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;
	private int clPort;

	@RDF("bt-mosim:eventTypeQuery")
	@Getter @Setter
	private BehaviorSelectQuery eventTypeQuery;

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#RegisterListener");
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			Map<String,String> cosim = MOSIMUtil.getCoSimInfos(query,this.getObject());
			if(!cosim.isEmpty()) {
				host = cosim.get("host");
				port = Integer.parseInt(cosim.get("port"));
				avatarId = cosim.get("avatarId");
				eventtype = MOSIMUtil.getEventType(eventTypeQuery, this.getObject());
				try {
					registerEventCallback();
					Model inputModel = getInputModel();
					MOSIMUtil.writeInput(inputModel, repository.toString(), this.getObject());
					String report = toString() + " SUCCEEDED";
					return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
				} catch (TException ex) {
					String report = toString() + " FAILED";
					return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
				}
			}
		} catch (URISyntaxException ex) {
			String report = toString() + " FAILED";
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
		}
		String report = toString() + " FAILED";
		return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
			
	}

	private boolean registerEventCallback() throws TTransportException, TException, URISyntaxException {
		MIPAddress address = new MIPAddress();
		address.setAddress(THRIFT_HOST);
		clPort = MOSIMUtil.getPortInfos(callback, this.getObject());
		address.setPort(clPort);
		TTransport transport;
        transport = new TSocket(host, port);
        transport.open();
        TProtocol protocol = new TCompactProtocol(transport);
		MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
		MBoolResponse registered = client.RegisterAtEvent(address, eventtype, avatarId);
		transport.close();
		return registered.Successful;
	}

	private Model getInputModel() {
		Model model = new LinkedHashModel();
		Resource subj = vf.createBNode();
		model.add(subj, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MOSIMVocabulary.CO_SIMULATOR);
		model.add(subj, MOSIMVocabulary.HAS_HOST, vf.createLiteral(host));
		model.add(subj, MOSIMVocabulary.HAS_PORT, vf.createLiteral(port));
		model.add(subj, MOSIMVocabulary.HAS_EVENT_TYPE, vf.createLiteral(eventtype));
		model.add(subj, MOSIMVocabulary.HAS_CALLBACK, vf.createLiteral(clPort));
		return model;
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "RegisterListener (" + getLabel() + ")";
	}
	
	@Override
	public SimulationResult.Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return SimulationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
