package de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint;

import org.pf4j.ExtensionPoint;
import rddl.competition.Server;

public class RDDLPluginServer implements ExtensionPoint {
    static Server rddlServer;

    public static void initServer() {
        setupAndStartServer();
    }

    private static void setupAndStartServer() {
        startAJANRDDLServer();
    }

    private static void startAJANRDDLServer() {
        startRDDLServer();
    }

    private static void startRDDLServer() {
        // TODO:Create RDDL Server communication here
    }

}
