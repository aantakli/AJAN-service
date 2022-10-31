package de.dfki.asr.ajan.pluginsystem.mdpplugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rddl.competition.Client;
import rddl.competition.Server;

public class MDPPlugin extends Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(MDPPlugin.class);

    public MDPPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start(){
        System.out.println("MDPPlugin.start()");
        if(RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())){
            LOG.debug("MDPPlugin");
        }
    }

    @Override
    public void stop(){
        System.out.println("MDPPlugin.stop()");
    }

}