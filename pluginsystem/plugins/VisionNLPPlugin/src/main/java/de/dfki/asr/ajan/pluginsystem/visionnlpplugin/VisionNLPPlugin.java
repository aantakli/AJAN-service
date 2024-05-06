package de.dfki.asr.ajan.pluginsystem.visionnlpplugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

public class VisionNLPPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(VisionNLPPlugin.class);

    public VisionNLPPlugin(PluginWrapper wrapper){
        super(wrapper);
    }

    @Override
    public void start(){
        System.out.println("VisionNLPPlugin.start()");
        if(RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())){
            LOG.debug("VisionNLPPlugin");
        }
    }

    @Override
    public void stop(){
        System.out.println("VisionNLPPlugin.stop()");
    }

}
