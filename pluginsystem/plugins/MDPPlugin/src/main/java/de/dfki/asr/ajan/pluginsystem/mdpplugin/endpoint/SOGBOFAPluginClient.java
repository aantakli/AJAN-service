package de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint;

import org.pf4j.ExtensionPoint;
import rddl.competition.SOGBOFA;

public class SOGBOFAPluginClient implements ExtensionPoint {
    static SOGBOFA sogbofaPlanner;

    public static void initClient() {
        setupAndStartClient();
    }

    private static void setupAndStartClient() {
        startSOGBOFAClient();
    }

    private static void startSOGBOFAClient() {
        // TODO: Start the SOGBOFA Client here
    }
}
