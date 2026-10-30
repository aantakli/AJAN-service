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

package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;

@Extension
@RDFBean("http://www.ajan.de/behavior/opcua-ns#EnableAssetNode")
public class OPCUAEnableAssetNode extends OPCUAMethodCallNode implements NodeExtension {
  @RDFSubject @Getter @Setter private String url;

  @RDF("rdfs:label")
  @Getter
  @Setter
  private String label;

  @RDF("opcua:enabled")
  @Getter
  @Setter
  private boolean enabled;

  @RDF("opcua:endpointUrl")
  @Getter
  @Setter
  private String endpointUrl = "";

  @RDF("bt:targetBase")
  @Getter
  @Setter
  private URI targetBase;

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/opcua-ns#EnableAssetNode");
  }

  @Override
  protected String method() {
    return "enable_asset";
  }

  @Override
  protected String[] options() {
    return new String[] {"--enable", isEnabled() ? "true" : "false"};
  }
}
