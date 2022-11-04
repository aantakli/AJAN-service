package de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint;

import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.RDDLServer;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDDLPluginServer implements ExtensionPoint {
    private static final Logger LOG = LoggerFactory.getLogger(RDDLPluginServer.class);
    static Thread rddlServerListener;
    public static void initServer(String filesPath, String port) {
        LOG.info("Setting up and starting the RDDLPluginServer");
        setupAndStartServer(filesPath,port);
    }

    private static void setupAndStartServer(String filesPath, String port) {
        startRDDLServer(filesPath, port);
    }

    private static void startRDDLServer(String filesPath, String port) {
        LOG.info("Initiating the thread for RDDLPluginServer");
        Runnable rddlThread = new RDDLServer(filesPath, Integer.parseInt(port));
        rddlServerListener = new Thread(rddlThread);
        rddlServerListener.start();
    }

    private static void stopRDDLServer(){
//        rddlServerListener.interrupt(); // socket connection will wait until a connection is made, not sure whether to kill immediately
        LOG.info("Attempting to stop the RDDLPluginServer Thread");
        rddlServerListener.stop();
    }

}
