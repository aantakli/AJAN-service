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

package de.dfki.asr.ajan.pluginsystem.aspplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Constraint;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Fact;
import de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts.Rule;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.util.List;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.pf4j.Extension;

@Extension
@RDFBean("asp:RuleSet")
@Data
public class ASPRules implements NodeExtension {
	@RDF("asp:facts")
	private List<Fact> facts;

	@RDF("asp:rules")
	private List<Rule> rules;
	
	@RDF("asp:constraints")
	private List<Constraint> constraints;
	
	@RDF("asp:asRules")
	private String stringRules;
}
