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

package de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.extensions;

import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.apicaller.APICaller;
import de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.apicaller.OLLamaCaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.pf4j.Extension;

@Extension
@RDFBean("rmlmapping:LeafNode")
public class RMLMappingLeafNode extends AbstractTDBLeafTask implements NodeExtension {
  @RDFSubject @Getter @Setter private String url;

  @RDF("rdfs:label")
  @Getter
  @Setter
  private String label;

  @RDF("rmlmapping:ontologie")
  @Getter
  @Setter
  private String ontology = "";

  @RDF("rmlmapping:aasxJSON")
  @Getter
  @Setter
  private String aasxJSON = "";

  @RDF("rmlmapping:prompt")
  @Getter
  @Setter
  private String prompt = "";

  @RDF("bt:targetBase")
  @Getter
  @Setter
  private URI targetBase;

  private AJANLogger LOG;

  @Override
  public String toString() {
    return "RMLMappingLeafNode (" + label + ")";
  }

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/rmlmapping-ns#LeafNode");
  }

  @Override
  public NodeStatus executeLeaf() {
    LOG = this.getObject().getLogger();
    LOG.info(this.getClass(), "Executing RMLMappingLeafNode: " + this);

    try {
      APICaller apiCaller = new OLLamaCaller();
      String response = apiCaller.callAPI(aasxJSON, ontology, prompt);
      System.out.println(response);
    } catch (Exception e) {
      return new NodeStatus(
          Status.FAILED, this.getObject().getLogger(), this.getClass(), this + " FAILED", e);
    }
    return new NodeStatus(
        Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), this + " " + status);
  }

  /*private String loadBeliefs() throws URISyntaxException, UnsupportedEncodingException {
    if (!getQuery().getSparql().isEmpty()) {
      Repository origin = BTUtil.getInitializedRepository(getObject(), getQuery().getOriginBase());
      Model model = getQuery().getResult(origin);
      return ACTNUtil.getModelPayload(model, "text/turtle");
    }
    return "";
  }*/

  private void writeSolution(final String input) throws IOException {
    Model model;
    if (input.isEmpty()) {
      LOG.info(this.getClass(), "Empty RMLMappingNode solution!\n");
      return;
    }
    model =
        Rio.parse(
            new ByteArrayInputStream(input.getBytes()), "http://www.ajan.de", RDFFormat.TURTLE);
    if (getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
      this.getObject().getExecutionBeliefs().update(model);
    } else if (getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
      this.getObject().getAgentBeliefs().update(model);
    }
  }

  @Override
  public void end() {
    this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
  }

  @Override
  public SimulationResult.Result simulateNodeLogic(SimulationResult result, Resource root) {
    return SimulationResult.Result.SUCCESS;
  }
}
