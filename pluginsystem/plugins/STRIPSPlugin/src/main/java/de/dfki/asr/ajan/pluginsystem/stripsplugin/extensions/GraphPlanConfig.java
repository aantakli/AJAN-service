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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("strips:GraphPlanConfig")
@Data
public class GraphPlanConfig implements NodeExtension {
	@RDF("strips:maxLevels")
	private Integer maxLevels;

	@RDF("strips:maxLength")
	private Integer maxLength;

	@RDF("strips:timeout")
	private Long timeout;

	@RDF("strips:allSolutions")
	private Boolean allSolutions;
}
