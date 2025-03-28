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

package de.dfki.asr.ajan.behaviour.nodes.common;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface TreeNode {
	void setUrl(String url);
	String getUrl();

	Resource getInstance(final Resource btInstance);
	Resource getDefinition(final Resource btDefinition);
	Resource getType();
	String getLabel();

	void simulate(final SimulationResult result);

	Model getModel(Model model, BTRoot root, ModelMode mode);
}
