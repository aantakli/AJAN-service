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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.mosim.mmi.avatar.MJointType;
import de.mosim.mmi.math.MQuaternion;
import de.mosim.mmi.math.MTransform;
import de.mosim.mmi.math.MVector3;
import de.mosim.mmi.services.MSkeletonAccess;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:GetAvatarTransform")
public class GetAvatarTransform extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;
	
	@RDF("bt-mosim:avatarID")
	@Getter @Setter
	private BehaviorSelectQuery avatarID;

	@RDF("bt-mosim:host")
	@Getter @Setter
	private BehaviorSelectQuery query;

	private String host;
	private int port;

	@RDF("bt:targetBase")
	@Getter @Setter
	private URI repository;

	protected static final Logger LOG = LoggerFactory.getLogger(GetAvatarTransform.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#GetAvatar");
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			Map<String,String> hostMap = MOSIMUtil.getHostInfos(query,this.getObject());
			if(!hostMap.isEmpty()) {
				Map.Entry<String,String> entry = hostMap.entrySet().iterator().next();
				host = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				List<String> id = getAvatarID(avatarID, this.getObject());
				MTransform transform = getAvatarTransform(id.get(1));
				Model inputModel = getInputModel(transform, id);
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

	public List<String> getAvatarID(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
		List<String> idList = new ArrayList();
		if (query != null) {
			Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
			List<BindingSet> result = query.getResult(repo);
			if (!result.isEmpty()) {
				BindingSet bindings = result.get(0);
				idList.add(bindings.getValue("uri").stringValue()); 
				idList.add(bindings.getValue("avatarID").stringValue());
			}
		}
		return idList;
	}

	private MTransform getAvatarTransform(final String avatarID) {
		try {
			try (TTransport transport = new TSocket(host, port)) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MSkeletonAccess.Client client = new MSkeletonAccess.Client(protocol);
				return getTransform(client.GetGlobalJointPosition(avatarID, MJointType.Root), 
						client.GetGlobalJointRotation(avatarID, MJointType.Root));
			}
		} catch (TException ex) {
			LOG.error("Could not load Avatar MTransform", ex);
			return null;
		}
	}

	private Model getInputModel(final MTransform transform, final List<String> avatarID) {
		Model model = new LinkedHashModel();
		IRI subject = vf.createIRI(avatarID.get(0));
		MOSIMUtil.setTransform(model, subject, transform);
		return model;
	}

	private MTransform getTransform(final MVector3 pos, final MQuaternion rot) {
		MTransform transform = new MTransform();
		transform.setPosition(pos);
		transform.setRotation(rot);
		transform.setID("123456");
		return transform;
	}
	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "GetAvatarTransform (" + getLabel() + ")";
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
