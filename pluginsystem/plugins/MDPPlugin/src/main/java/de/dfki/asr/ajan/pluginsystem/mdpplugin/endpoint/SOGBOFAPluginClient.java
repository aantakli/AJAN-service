package de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint;

import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.SOGBOFAClient;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SOGBOFAPluginClient implements ExtensionPoint {
    private static final Logger LOG = LoggerFactory.getLogger(SOGBOFAClient.class);
    static Thread sogbofaClientListener;

    public static void initClient(String filePath,
                                  String portNumber,
                                  String ClientName,
                                  String instanceName) {
        setupAndStartClient(filePath,portNumber, ClientName, instanceName);
    }

    private static void setupAndStartClient(String filesPath,
                                            String portNumber,
                                            String ClientName,
                                            String instanceName) {
        startSOGBOFAClient(filesPath,portNumber, ClientName, instanceName);
    }

    private static void startSOGBOFAClient(String filesPath,
                                           String portNumber,
                                           String ClientName,
                                           String instanceName) {
        LOG.info("Initiating the thread for SOGBOFAPluginClient");
        Runnable sogbofaThread = new SOGBOFAClient(filesPath,portNumber, ClientName, instanceName);
        sogbofaClientListener = new Thread(sogbofaThread);
        sogbofaClientListener.start();
    }
}
