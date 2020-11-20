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

package de.dfki.asr.ajan.pluginsystem;


import de.dfki.asr.ajan.pluginsystem.extensionpoints.EndpointExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.util.List;
import lombok.Data;
import org.cyberborean.rdfbeans.exceptions.RDFBeanValidationException;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

@Data
@Component
public class AJANPluginLoader {

	private final PluginManager PLUGIN_MANAGER = new DefaultPluginManager();

	public void init() throws RDFBeanValidationException {
		PLUGIN_MANAGER.loadPlugins();
		PLUGIN_MANAGER.startPlugins();
	}

	public void stop() {
		PLUGIN_MANAGER.stopPlugins();
	}

	public List<NodeExtension> getNodeExtensions() {
		return PLUGIN_MANAGER.getExtensions(NodeExtension.class);
	}

        public List<EndpointExtension> getEndpointExtensions() {
		return PLUGIN_MANAGER.getExtensions(EndpointExtension.class);
	}
}
