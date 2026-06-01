package de.dfki.asr.ajan.pluginsystem.scriptexecutorplugin;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.utils.ScriptExtractor;
import java.io.IOException;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptExecutorPlugin extends Plugin {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorPlugin.class);
  private final PythonExecutionService pythonService;

  public ScriptExecutorPlugin(PluginWrapper wrapper) {
    super(wrapper);
    // In a real AJAN system, we might get the loader via some context or dependency injection
    // For now, we assume we can access it or it's provided by the system.
    // This is just an example implementation.
    this.pythonService = null; // Will be retrieved via AJANPluginLoader in nodes
  }

  @Override
  public void start() {
    System.out.println("ScriptExecutorPlugin.start()");
    if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
      LOG.debug("ScriptExecutorPlugin");
    }
    try {
      ScriptExtractor.extractScriptsFromPlugin(this, "scripts");
    } catch (IOException e) {
      LOG.error("Failed to extract scripts", e);
    }
  }

  @Override
  public void stop() {
    LOG.info("ScriptExecutorPlugin.stop()");
    ScriptExtractor.cleanupScripts(this);
  }
}
