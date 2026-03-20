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
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import org.cyberborean.rdfbeans.exceptions.RDFBeanValidationException;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.LegacyExtensionFinder;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

@Data
@Component
public class AJANPluginLoader {

  @Getter private static AJANPluginLoader instance;

  private final PluginManager PLUGIN_MANAGER =
      new DefaultPluginManager() {
        @Override
        protected ExtensionFinder createExtensionFinder() {
          // Use LegacyExtensionFinder which scans for @Extension annotations at runtime
          // This works in development mode without needing extensions.idx files
          return new LegacyExtensionFinder(this);
        }
      };

  public AJANPluginLoader() {
    instance = this;
  }

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

  public PythonExecutionService getPythonExecutionService() {
    List<PythonExecutionService> services =
        PLUGIN_MANAGER.getExtensions(PythonExecutionService.class);
    if (services.isEmpty()) {
      throw new IllegalStateException(
          "PythonExecutionService not available. Make sure PythonPlugin is loaded.");
    }
    return services.get(0);
  }

  public PluginManager getPluginManager() {
    return PLUGIN_MANAGER;
  }
}
