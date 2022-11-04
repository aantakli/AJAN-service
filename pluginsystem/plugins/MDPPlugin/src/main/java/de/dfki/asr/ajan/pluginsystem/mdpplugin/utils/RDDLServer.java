package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rddl.RDDL;
import rddl.competition.Server;
import rddl.viz.NullScreenDisplay;
import rddl.viz.StateViz;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RDDLServer implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(RDDLServer.class);
    private final String filesPath;
    private final Integer port;
    private boolean exit;

    public RDDLServer(String filesPath, Integer portNumber){
        this.filesPath = filesPath;
        this.port = portNumber;
    }

    @Override
    public void run() {
        while (!exit) {
            ServerSocket socket1 = null;
            Socket connection = null;
            try {
                LOG.info(String.format("Opening a socket in %s port",port));
                socket1 = new ServerSocket(port);
                connection = socket1.accept();
                LOG.info("Accepted a Client");
            } catch (IOException e) {
                LOG.error("Socket connection failed"+e.getMessage());
                throw new RuntimeException(e);
            }
            RandomDataGenerator rdg = new RandomDataGenerator();
            rdg.reSeed(-1);
            RDDL rddl = new RDDL(filesPath);
            StateViz state_viz = new NullScreenDisplay(false);
            Runnable runnable = new Server(connection, 0, rddl, state_viz, port, rdg);
            LOG.info("Initiating the thread for Client");
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
