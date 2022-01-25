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

package de.dfki.asr.ajan.pluginsystem.mywelcomeplugin.extensions;

import de.dfki.asr.ajan.behaviour.exception.MessageEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mywelcomeplugin.utils.MyWelcomeUtil;
import de.dfki.asr.ajan.pluginsystem.mywelcomeplugin.vocabularies.MyWelcomeVocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("welcome:Logging")
public class Logging extends AbstractTDBLeafTask implements NodeExtension, TreeNode {
	@Getter @Setter
	@RDFSubject
	private String url;

	private static final Logger LOG = LoggerFactory.getLogger(Logging.class);
	private Resource instance;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("welcome:correlatorId")
	@Setter @Getter
	private BehaviorSelectQuery queryCorrId;

	@RDF("welcome:lvl")
	@Getter @Setter
	private URI lvl;

	@RDF("welcome:comp")
	@Getter @Setter
	private String comp;

	@RDF("welcome:msg")
	@Getter @Setter
	private String msg;

	protected String correlatorId = "";

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public Resource getType() {
	    return MyWelcomeVocabulary.LOGGING;
	}

	@Override
	public Resource getInstance(final Resource btInstance) {
		if (instance == null) {
			instance = BTUtil.getInstanceResource(url, btInstance);
		}
		return instance;
	}

	@Override
	public Resource getDefinition(final Resource btDefinition) {
		if (url == null) {
			return btDefinition;
		}
		return vf.createIRI(url);
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.SUCCESS;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		BTUtil.setGeneralNodeModel(model, root, mode, this);
		return model;
	}

	@Override
	public String toString() {
		return "Logging (" + label + ")";
	}

	@Override
	public LeafStatus executeLeaf() {
		try {
			correlatorId = MyWelcomeUtil.getCorrelationId(this.getObject(), queryCorrId);
			MyWelcomeUtil.logInfo(
				this, 
				UUID.randomUUID(), 
				correlatorId, 
				getLevel(), 
				msg);
			String report = toString() + " SUCCEEDED";
			LOG.info(report);
			return new LeafStatus(Status.SUCCEEDED, report);
		} catch (URISyntaxException | UnknownHostException | MessageEvaluationException ex) {
			String report = toString() + " FAILED";
			LOG.info(report);
			return new LeafStatus(Status.FAILED, report);
		}
	}

	private MyWelcomeUtil.LogLevel getLevel() {
		switch(lvl.toString()) {
			case "http://www.ajan.de/myWelcome-ns/logging#Info":
				return MyWelcomeUtil.LogLevel.INFO;
			case "http://www.ajan.de/myWelcome-ns/logging#Debug":
				return MyWelcomeUtil.LogLevel.DEBUG;
			case "http://www.ajan.de/myWelcome-ns/logging#Warn":
				return MyWelcomeUtil.LogLevel.WARN;
			case "http://www.ajan.de/myWelcome-ns/logging#Error":
				return MyWelcomeUtil.LogLevel.ERROR;
			case "http://www.ajan.de/myWelcome-ns/logging#Fatal":
				return MyWelcomeUtil.LogLevel.FATAL;
			case "http://www.ajan.de/myWelcome-ns/logging#Summary":
				return MyWelcomeUtil.LogLevel.SUMMARY;
			case "http://www.ajan.de/myWelcome-ns/logging#None":
				return MyWelcomeUtil.LogLevel.NONE;
			default:
				return MyWelcomeUtil.LogLevel.NONE;
		}
	}
}
