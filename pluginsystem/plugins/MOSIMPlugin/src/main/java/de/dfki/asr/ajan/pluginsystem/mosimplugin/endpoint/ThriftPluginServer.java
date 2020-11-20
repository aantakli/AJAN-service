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

import de.dfki.asr.ajan.logic.agent.AgentManager;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.EndpointExtension;
import de.mosim.mmi.agent.MAJANService;
import java.util.HashMap;
import java.util.Map;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

@Slf4j
@Extension
public class ThriftPluginServer implements EndpointExtension {

    public static MAJANServiceHandler handler;
    private static MAJANService.Processor processor;
    private static TServer server;

	public static final String THRIFT_HOST = "127.0.0.1";
	public static final Integer THRIFT_PORT = 8081;
	private static final Map<Integer,TServer> CALLBACK_SERVERS = new HashMap<>();

    @Override
    public void initServer(Object agentManager) {
        AgentManager agentManagerObject = (AgentManager) agentManager;
        setupAndStartServer(agentManagerObject);

    }

    public static MAJANServiceHandler getHandler() throws NullPointerException {
        if (ThriftPluginServer.handler != null) {
            return ThriftPluginServer.handler;
        }
        throw new NullPointerException("ThriftAJANServiceHandler is null");
    }

    @Override
    public void destroyServer() {
        ThriftPluginServer.stop();
    }

    public void setupAndStartServer(AgentManager agentManagerObject) {
        try {
            startAJANThriftServer(agentManagerObject);
        } catch (Exception x) {
            log.info("Caught Exception in setupAndStartServer().");
        }
    }

	private static void startAJANThriftServer(AgentManager agentManagerObject) {
		handler = new MAJANServiceHandler(agentManagerObject);
        processor = new MAJANService.Processor(handler);
        Runnable simpleMAJANService = () -> {
                start(processor);
        };
		new Thread(simpleMAJANService).start();
	}

    private static void start(MAJANService.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(THRIFT_PORT);
            server = new TSimpleServer(new Args(serverTransport).processor(processor));
			log.info("Starting the plugin server...");
            server.serve();
        } catch (TTransportException e) {
            log.info("Caught TTransportException in start().");
        }
    }

	public static boolean add(final Integer port, final TServer server) {
		if (CALLBACK_SERVERS.containsKey(port)) {
			return false;
		} else {
			CALLBACK_SERVERS.put(port, server);
			return true;
		}
	}

    private static void stop() {
        if (server != null && server.isServing()) {
            log.info("Stopping the thrift server... ");
            server.stop();
        }
		if (!CALLBACK_SERVERS.isEmpty()) {
			for (Map.Entry<Integer, TServer> server: CALLBACK_SERVERS.entrySet()) {
				stop(server.getKey());
			}
        }
    }

	public static void stop(final Integer port) {
		if (CALLBACK_SERVERS.containsKey(port)) {
			TServer tServer = CALLBACK_SERVERS.get(port);
			if (tServer != null && tServer.isServing()) {
				log.info("Stopping the callback server on port: " + port + "...");
				tServer.stop();
			}
			CALLBACK_SERVERS.remove(port);
        }
	}
}
