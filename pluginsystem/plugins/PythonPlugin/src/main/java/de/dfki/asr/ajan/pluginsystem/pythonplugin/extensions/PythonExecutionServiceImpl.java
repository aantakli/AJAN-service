package de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPackageManager;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPlugin;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.monitoring.PythonExecutionMetrics;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import jep.SharedInterpreter;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class PythonExecutionServiceImpl implements PythonExecutionService {

  private static final Logger LOG = LoggerFactory.getLogger(PythonExecutionServiceImpl.class);

  @Override
  public synchronized PythonExecutionResult executeScript(
      String script, Map<String, Object> inputs) {
    long startTime = System.currentTimeMillis();

    try (SharedInterpreter interpreter = new SharedInterpreter()) {
      // Set inputs
      if (inputs != null) {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
          interpreter.set(entry.getKey(), entry.getValue());
        }
      }

      // Execute script
      interpreter.exec(script);

      // Collect results (all variables set in the script)
      // Note: JEP doesn't easily allow listing all variables,
      // but we can try to get common result variables or the ones provided in inputs
      Map<String, Object> results = new HashMap<>();
      if (inputs != null) {
        for (String key : inputs.keySet()) {
          try {
            results.put(key, interpreter.getValue(key));
          } catch (Exception e) {
            // Ignore if variable is not available
          }
        }
      }

      // Also check for a standard 'result' variable
      try {
        Object result = interpreter.getValue("result");
        if (result != null) {
          results.put("result", result);
        }
      } catch (Exception e) {
        // Ignore
      }

      PythonExecutionResult res =
          PythonExecutionResult.success(
              "Script executed successfully", results, System.currentTimeMillis() - startTime);
      PythonExecutionMetrics.getInstance().recordExecution(res);
      return res;
    } catch (Exception e) {
      LOG.error("Error executing Python script", e);
      PythonExecutionResult res =
          PythonExecutionResult.failed(e.getMessage(), e, System.currentTimeMillis() - startTime);
      PythonExecutionMetrics.getInstance().recordExecution(res);
      return res;
    }
  }

  @Override
  public PythonExecutionResult executeScriptFile(Path scriptPath, Map<String, Object> inputs) {
    try {
      String script = new String(Files.readAllBytes(scriptPath));
      return executeScript(script, inputs);
    } catch (Exception e) {
      LOG.error("Error reading script file: {}", scriptPath, e);
      return PythonExecutionResult.failed("Error reading script file: " + e.getMessage(), e, 0);
    }
  }

  @Override
  @Deprecated
  public synchronized PythonExecutionResult executePowerShellCommand(String command) {
    LOG.error(
        "executePowerShellCommand is deprecated and no longer supported. Command was: {}", command);
    return PythonExecutionResult.failed(
        "PowerShell support has been removed. Use executeScript or executeScriptFile instead.",
        null,
        0);
  }

  @Override
  public Path getPythonEnvironmentPath() {
    return PythonPlugin.getPythonEnvPath();
  }

  @Override
  public boolean isPythonAvailable() {
    return PythonPlugin.getMainInterpreter() != null;
  }

  @Override
  public PythonExecutionResult installPythonPackage(String packageName) {
    long startTime = System.currentTimeMillis();
    try {
      PythonPackageManager.getInstance().installPackage(packageName);
      return PythonExecutionResult.success(
          "Package installation initiated: " + packageName,
          null,
          System.currentTimeMillis() - startTime);
    } catch (Exception e) {
      LOG.error("Error installing Python package", e);
      return PythonExecutionResult.failed(
          e.getMessage(), e, System.currentTimeMillis() - startTime);
    }
  }
}
