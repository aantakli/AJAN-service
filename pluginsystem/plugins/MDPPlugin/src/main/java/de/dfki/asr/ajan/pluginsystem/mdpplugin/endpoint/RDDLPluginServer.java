package de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint;

import org.pf4j.ExtensionPoint;
import rddl.competition.Server;

public class RDDLPluginServer implements ExtensionPoint {

    public static void initServer(String filesPath, String port) {
        setupAndStartServer(filesPath,port);
    }

    private static void setupAndStartServer(String filesPath, String port) {
        startRDDLServer(filesPath, port);
    }

    private static void startRDDLServer(String filesPath, String port) {
        // TODO:Create RDDL Server communication here
        String[] filePathArg = new String[] {filesPath,port,"100","0","1"};
        Server.main(filePathArg);
    }

}
