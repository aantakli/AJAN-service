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

package de.dfki.asr.ajan.pluginsystem.standardbtnodes.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.exceptions.BeliefBaseUpdateException;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:Wait")
public class Wait extends AbstractTDBLeafTask implements NodeExtension {
	@Getter @Setter
	@RDFSubject
	private String url;

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:milliseconds")
	@Getter @Setter
	private Integer milliseconds;
	
	private Thread thread;
	private Status running = Status.FRESH;
	private BTRoot bt;

	@Override
	public Resource getType() {
		return StandardBTVocabulary.TIMESTAMP;
	}

	private static final Logger LOG = LoggerFactory.getLogger(Wait.class);

	@Override
	public LeafStatus executeLeaf() {
		try {
			switch (running) {
				case FRESH:
				case CANCELLED:
				{
					startWaiting();
					String report = toString() + " RUNNING";
					LOG.info(report);
					return new LeafStatus(Status.RUNNING, report);
				}
				case SUCCEEDED:
				{
					running = Status.FRESH;
					String report = toString() + " SUCCEEDED";
					LOG.info(report);
					return new LeafStatus(Status.SUCCEEDED, report);
				}
				default:
				{
					String report = toString() + " RUNNING";
					LOG.info(report);
					return new LeafStatus(Status.RUNNING, report);
				}
			}
		} catch (BeliefBaseUpdateException ex) {
			LOG.info(toString() + " FAILED due to query evaluation error", ex);
			return new LeafStatus(Status.FAILED, toString() + " FAILED due to query evaluation error");
		}
	}

	private void startWaiting() throws BeliefBaseUpdateException {
		bt = this.getObject().getBt();
		thread = new Thread(){
			public void run(){
				try {
					running = Status.RUNNING;
					sleep(milliseconds);
					running = Status.SUCCEEDED;
					bt.setEventInformation(new LinkedHashModel());
				} catch (InterruptedException ex) {
					LOG.info("Thread cancelled!");
				}
			}
		};
		thread.start();
	}

	@Override
	public void resetTask() {
		running = Status.FRESH;
		if (thread != null && thread.isAlive())
			thread.interrupt();
		super.resetTask();
	}

	@Override
	public void end() {
		if (getStatus() == Status.CANCELLED) {
			BTUtil.sendReport(this.getObject(), toString() + " CANCELLED");
			running = Status.CANCELLED;
			if (thread != null && thread.isAlive())
				thread.interrupt();
		}
	}

	@Override
	public String toString() {
		return "Wait (" + label + ")";
	}

	@Override
	public Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return Result.SUCCESS;
	}
}
