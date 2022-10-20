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

package de.dfki.asr.ajan.pluginsystem.aspplugin.extensions.parts;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.pf4j.Extension;
import org.springframework.util.StringUtils;

@Extension
@RDFBean("asp:Variable")
public class Variable extends Term implements NodeExtension {

	private String value;
	private int intValue;
	private String stringValue;

	public Variable(){}

	@Override
	@RDF("asp:value")	
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(final String value) {
		this.value = StringUtils.capitalize(value);
	}

	@Override
	@RDF("asp:intValue")
	public int getIntValue() {
		return this.intValue;
	}

	@Override
	public void setIntValue(final int value) {
		this.intValue = value;
	}

	@Override
	@RDF("asp:stringValue")
	public String getStringValue() {
		return this.stringValue;
	}

	@Override
	public void setStringValue(final String value) {
		this.stringValue = StringUtils.capitalize(value);
	}
}
