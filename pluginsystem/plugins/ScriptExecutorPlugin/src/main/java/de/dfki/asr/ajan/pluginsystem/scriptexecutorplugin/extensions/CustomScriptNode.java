package de.dfki.asr.ajan.pluginsystem.scriptexecutorplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.SimulationResult;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.utils.ScriptExtractor;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;

@Extension
@RDFBean("http://www.ajan.de/behavior/script-ns#CustomScriptNode")
public class CustomScriptNode extends AbstractTDBLeafTask implements NodeExtension {

  @RDFSubject @Getter @Setter private String url;

  @RDF("rdfs:label")
  @Getter
  @Setter
  private String label;

  @RDF("script:path")
  @Getter
  @Setter
  private String scriptPath;

  @Override
  public NodeStatus executeLeaf() {
    try {
      this.getObject().getLogger().info(this.getClass(), "executeLeaf called for node URL: " + url);

      PythonExecutionService pythonService = null;
      if (AJANPluginLoader.getInstance() != null) {
        pythonService = AJANPluginLoader.getInstance().getPythonExecutionService();
      }

      if (pythonService == null) {
        return new NodeStatus(
            Status.FAILED,
            this.getObject().getLogger(),
            this.getClass(),
            "PythonExecutionService not available");
      }

      // Log configured scriptPath
      this.getObject().getLogger().info(this.getClass(), "Configured scriptPath: " + scriptPath);

      Path fullScriptPath = null;
      try {
        var loaderInstance = AJANPluginLoader.getInstance();
        if (loaderInstance == null) {
          this.getObject()
              .getLogger()
              .info(
                  this.getClass(),
                  "AJANPluginLoader instance is null (plugins may not be initialized yet)");
        } else {
          var pluginManager = loaderInstance.getPLUGIN_MANAGER();
          if (pluginManager == null) {
            this.getObject().getLogger().info(this.getClass(), "Plugin Manager is null!");
          } else {
            var pluginWrapper = pluginManager.getPlugin("ScriptExecutorPlugin");
            if (pluginWrapper != null) {
              var plugin = pluginWrapper.getPlugin();
              if (plugin != null) {
                fullScriptPath = ScriptExtractor.getScriptPath(plugin, scriptPath);
                if (fullScriptPath == null) {
                  this.getObject()
                      .getLogger()
                      .info(
                          this.getClass(),
                          "Script not found in extraction cache. Attempting manual extraction...");
                  Path base = ScriptExtractor.extractScriptsFromPlugin(plugin, "scripts");
                  if (base != null) {
                    fullScriptPath = base.resolve(scriptPath);
                  }
                }
              } else {
                this.getObject()
                    .getLogger()
                    .info(this.getClass(), "Plugin instance for 'ScriptExecutorPlugin' is null!");
              }
            } else {
              this.getObject()
                  .getLogger()
                  .info(this.getClass(), "Plugin 'ScriptExecutorPlugin' not found!");
            }
          }
        }
      } catch (Exception e) {
        this.getObject()
            .getLogger()
            .info(this.getClass(), "Error getting script path: " + e.getMessage());
      }

      // Fallback: try to load the script from the classpath of this plugin
      if (fullScriptPath == null || (fullScriptPath != null && !Files.exists(fullScriptPath))) {
        try {
          this.getObject()
              .getLogger()
              .info(this.getClass(), "Trying classpath resource for script: " + scriptPath);
          try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (is != null) {
              Path tmp = Files.createTempFile("ajan-script-", ".py");
              Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
              tmp.toFile().deleteOnExit();
              fullScriptPath = tmp;
              this.getObject()
                  .getLogger()
                  .info(
                      this.getClass(),
                      "Resolved script from classpath to temp file: " + fullScriptPath);
            } else {
              this.getObject()
                  .getLogger()
                  .info(this.getClass(), "Script not found on classpath: " + scriptPath);
            }
          }
        } catch (Exception e) {
          this.getObject()
              .getLogger()
              .info(this.getClass(), "Error resolving script from classpath: " + e.getMessage());
        }
      }

      this.getObject().getLogger().info(this.getClass(), "Executing script at: " + fullScriptPath);

      if (fullScriptPath == null) {
        return new NodeStatus(
            Status.FAILED,
            this.getObject().getLogger(),
            this.getClass(),
            "Script path is null. Ensure 'scriptPath' parameter is set and plugin is loaded.");
      }

      Map<String, Object> inputs = new HashMap<>();
      inputs.put("agent_name", this.getObject().getBt().getInstance().toString());

      PythonExecutionResult result = pythonService.executeScriptFile(fullScriptPath, inputs);

      this.getObject()
          .getLogger()
          .info(this.getClass(), "Script execution success: " + result.isSuccess());

      if (result.isSuccess()) {
        String output = result.getOutput();
        this.getObject().getLogger().info(this.getClass(), "Raw output: " + output);
        if (result.getResults() != null) {
          this.getObject()
              .getLogger()
              .info(this.getClass(), "Available result keys: " + result.getResults().keySet());
          if (result.getResults().containsKey("result")) {
            output = result.getResults().get("result").toString();
            this.getObject()
                .getLogger()
                .info(this.getClass(), "Found 'result' variable: " + output);
          }
        }
        this.getObject().getLogger().info(this.getClass(), "Final node status label: " + output);
        return new NodeStatus(
            Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), output);
      } else {
        this.getObject().getLogger().info(this.getClass(), "Script error: " + result.getError());
        return new NodeStatus(
            Status.FAILED, this.getObject().getLogger(), this.getClass(), result.getError());
      }
    } catch (Exception e) {
      this.getObject()
          .getLogger()
          .info(this.getClass(), "Exception in executeLeaf: " + e.getMessage());
      return new NodeStatus(
          Status.FAILED, this.getObject().getLogger(), this.getClass(), e.getMessage());
    }
  }

  @Override
  public void end() {}

  @Override
  public SimulationResult.Result simulateNodeLogic(SimulationResult result, Resource root) {
    return SimulationResult.Result.SUCCESS;
  }

  @Override
  public String toString() {
    return "CustomScriptNode: " + label;
  }

  @Override
  public Resource getType() {
    return vf.createIRI("http://www.ajan.de/behavior/script-ns#CustomScriptNode");
  }
}
