/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.behaviour.nodes;

import de.dfki.asr.ajan.behaviour.nodes.common.*;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult.Result;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
@RDFBean("bt:Breakpoint")
public class Breakpoint extends AbstractTDBLeafTask {
	@Getter @Setter
	@RDFSubject
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@Override
	public Resource getType() {
		return BTVocabulary.DEBUG;
	}

	@Override
	public Status execute() {
		Debug debug = this.getObject().getDebug();
		debug.setDebugging(true);
		BTRoot bt = this.getObject().getBt();
		NodeStatus leafStatus = new NodeStatus(null, this.getObject().getLogger(), this.getClass(), toString() + " SUCCEEDED");
		String report = BTUtil.createReport(getUrl(), bt, leafStatus, debug, new LinkedHashModel());
		BTUtil.sendReport(this.getObject(),report);
		return Status.SUCCEEDED;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}

	@Override
	public Result simulateNodeLogic(final SimulationResult result, final Resource root) {
		return Result.SUCCESS;
	}

	@Override
	public void end() {
		this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "Breakpoint (" + label + ")";
	}

	@Override
	public NodeStatus executeLeaf() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
