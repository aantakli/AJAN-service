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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult.Result;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.exceptions.BeliefBaseUpdateException;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;

@Extension
@RDFBean("bt:Wait")
public class Wait extends AbstractTDBLeafTask implements NodeExtension {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:milliseconds")
	@Getter @Setter
	private Integer milliseconds;
	
	private Thread thread;
	private Status running = Status.FRESH;
	private AgentTaskInformation info;

	@Override
	public Resource getType() {
		return StandardBTVocabulary.TIMESTAMP;
	}

	@Override
	public NodeStatus executeLeaf() {
		try {
			switch (running) {
				case FRESH:
				case CANCELLED:
				{
					startWaiting();
					String report = toString() + " RUNNING";
					return new NodeStatus(Status.RUNNING, this.getObject().getLogger(), this.getClass(), report);
				}
				case SUCCEEDED:
				{
					running = Status.FRESH;
					String report = toString() + " SUCCEEDED";
					return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
				}
				default:
				{
					String report = toString() + " RUNNING";
					return new NodeStatus(Status.RUNNING, this.getObject().getLogger(), this.getClass(), report);
				}
			}
		} catch (BeliefBaseUpdateException ex) {;
			return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), toString() + " FAILED due to query evaluation error", ex);
		}
	}

	private void startWaiting() throws BeliefBaseUpdateException {
		info = this.getObject();
		thread = new Thread(){
			public void run(){
				try {
					running = Status.RUNNING;
					sleep(milliseconds);
					running = Status.SUCCEEDED;
					info.getBt().run();
				} catch (InterruptedException ex) {
					info.getLogger().info(this.getClass(), "Thread cancelled!", ex);
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
		} else {
			this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
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
