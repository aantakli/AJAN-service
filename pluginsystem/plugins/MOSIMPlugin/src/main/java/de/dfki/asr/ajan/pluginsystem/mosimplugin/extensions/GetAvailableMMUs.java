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
import static de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.AbortInstruction.LOG;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.core.MIPAddress;
import de.mosim.mmi.mmu.MMUDescription;
import de.mosim.mmi.register.MMIRegisterService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:GetAvailableMMUs")
public class GetAvailableMMUs extends AbstractTDBLeafTask implements NodeExtension {
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

	@RDF("bt-mosim:sessionID")
	@Getter @Setter
	private String session;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	protected static final Logger LOG = LoggerFactory.getLogger(GetAvailableMMUs.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetAvailableMMUs");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query,this.getObject());
			if(!hostMap.isEmpty()) {
				Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				Map<MMUDescription,List<MIPAddress>> mmus = getMMUs();
				Model inputModel = getInputModel(mmus);
				MOSIMUtil.writeInput(inputModel, repository.toString(), this.getObject());
				String report = toString() + " SUCCEEDED";
				LOG.info(report);
				return new LeafStatus(Status.SUCCEEDED, report);
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

	private Map<MMUDescription,List<MIPAddress>> getMMUs() {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MMIRegisterService.Client client = new MMIRegisterService.Client(protocol);
				return client.GetAvailableMMUs(host);
			}
		} catch (TException ex) {
			LOG.error("Could not load List<MMUDescription>", ex);
			return null;
		}
	}

	private Model getInputModel(final Map<MMUDescription,List<MIPAddress>> mmus) {
		Model model = new LinkedHashModel();
		IRI rdfType = org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
		for (MMUDescription mmu: mmus.keySet()) {
			Resource subject = vf.createBNode();
			model.add(subject, rdfType, MOSIMVocabulary.MMU_DESCRIPTION);
			model.add(subject, MOSIMVocabulary.HAS_ID, vf.createLiteral(mmu.ID));
			model.add(subject, MOSIMVocabulary.HAS_MOTION_TYPE, getMotionType(mmu.MotionType));
		}
		return model;
	}

	private IRI getMotionType(final String motionType) {
		String mmu = motionType.substring(0,1).toUpperCase() + motionType.substring(1);
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#" + mmu);
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GetAvailableMMUs (" + getLabel() + ")";
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
