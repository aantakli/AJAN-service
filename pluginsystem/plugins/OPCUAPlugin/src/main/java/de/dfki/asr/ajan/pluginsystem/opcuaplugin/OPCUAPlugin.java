package de.dfki.asr.ajan.pluginsystem.opcuaplugin;

import de.dfki.asr.ajan.pluginsystem.utils.ScriptExtractor;
import java.io.IOException;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OPCUAPlugin extends Plugin {
  private static final Logger LOG = LoggerFactory.getLogger(OPCUAPlugin.class);

  public OPCUAPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    LOG.info("OPCUAPlugin.start()");
    try {
      ScriptExtractor.extractScriptsFromPlugin(this, "scripts");
    } catch (IOException e) {
      LOG.error("Failed to extract scripts", e);
    }
  }

  @Override
  public void stop() {
    LOG.info("OPCUAPlugin.stop()");
    ScriptExtractor.cleanupScripts(this);
  }
}
