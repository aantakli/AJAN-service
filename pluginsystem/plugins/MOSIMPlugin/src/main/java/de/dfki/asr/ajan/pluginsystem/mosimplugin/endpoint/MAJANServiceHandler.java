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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint;

import de.dfki.asr.ajan.exceptions.AgentNotFoundException;
import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.model.Agent;
import de.mosim.mmi.agent.MRDFGraph;
import de.mosim.mmi.agent.MAJANService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@SuppressWarnings({"PMD", "Rule:UnusedImports"})
@Service
public class MAJANServiceHandler implements MAJANService.Iface {

    private final AgentManager agentManager;
    private final Map<String, ThriftAction> actions;
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MAJANServiceHandler.class);

    public MAJANServiceHandler(AgentManager am) {
        this.agentManager = am;
        this.actions = new HashMap<>();
    }

	public Map<String, ThriftAction> getActions() {
		return this.actions;
	}

    public void register(final String uid, final ThriftAction action) {
        this.actions.put(uid, action);
    }

    @Override
    public void ReceiveAsync(String actionID, int answer) throws TException {
        ThriftAction thrAction = actions.get(actionID);
        if (thrAction == null) {
            LOG.error("Key: " + actionID + " is not registered");
        } else {
            thrAction.setResponse(actionID, answer);
        }
    }

    @Override
    public String CreateAgent(String name, String agentTemplate, MRDFGraph content) throws TException {
        Agent agent;
        try {
            RDFFormat format = getRDFFormat(content.ContentType);
            agent = agentManager.createAgent(name, agentTemplate, content.Graph, format);
            return agent.getUrl();

        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(MAJANServiceHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
            return "null";// return URI of exectud Agent, else return null.
        }
    }

    @Override
    public boolean DeleteAgent(String agentName) throws TException {
        // changed String agentURI to agentName, because agentManager has no method to get an agent using URI.
        try {
            Agent agent = agentManager.getAgent(agentName);

            agent.stop();
            agentManager.deleteAgent(agent);
            return true;
        } catch (IllegalArgumentException | AgentNotFoundException ex) {
            return false;
        }
    }

    @Override
    public String ExecuteAgent(String agentName, String endpoint, MRDFGraph content) throws TException {
        try {
            Agent agent = agentManager.getAgent(agentName);
            if (endpoint == null) {
                agent.execute();
            } else {
                // add content graph to endpoint
                RDFFormat format = getRDFFormat(content.ContentType);
                if (format == null) {
                    return "null";
                }
                LinkedHashModel model = string2Model(content.Graph, format);
                agent.setEndpointEvent(endpoint, model);
            }
            return agent.getUrl();
        } catch (AgentNotFoundException e) {
            return "null";
        }
    }

    private LinkedHashModel string2Model(String s, RDFFormat format) {
        LinkedHashModel model = new LinkedHashModel();
        try {
			try (InputStream targetStream = new ByteArrayInputStream(s.getBytes())) {
				RDFParser rdfParser = Rio.createParser(format);
				rdfParser.setRDFHandler(new StatementCollector(model));
				
				rdfParser.parse(targetStream, "http://www.ajan.de");
			}
        } catch (IOException | RDFParseException | RDFHandlerException e) {
            LOG.info("Exception : " + e.toString());
        }
        return model;
    }

    private RDFFormat getRDFFormat(String contentType) {
        RDFParserRegistry registry = RDFParserRegistry.getInstance();
        Optional<RDFFormat> format = registry.getFileFormatForMIMEType(contentType);
        if (!format.isPresent()) {
            return null;
        }
        return format.get();
    }
}
