package de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonEnvironment;
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

/**
 * Runs scripts in a fresh {@link SharedInterpreter} on the calling thread. Not synchronized: each
 * call has its own globals; the GIL serializes Python bytecode, and blocking calls release it.
 */
@Extension
public class PythonExecutionServiceImpl implements PythonExecutionService {

  private static final Logger LOG = LoggerFactory.getLogger(PythonExecutionServiceImpl.class);

  @Override
  public PythonExecutionResult executeScript(String script, Map<String, Object> inputs) {
    long startTime = System.currentTimeMillis();
    if (!PythonPlugin.isAvailable()) {
      return record(notAvailable());
    }

    try (SharedInterpreter interpreter = new SharedInterpreter()) {
      if (inputs != null) {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
          interpreter.set(entry.getKey(), entry.getValue());
        }
      }

      interpreter.exec(script);

      // JEP cannot list globals: collect the input names (possibly reassigned) and 'result'.
      Map<String, Object> results = new HashMap<>();
      if (inputs != null) {
        for (String key : inputs.keySet()) {
          try {
            results.put(key, interpreter.getValue(key));
          } catch (Exception e) {
            // variable deleted by the script
          }
        }
      }
      try {
        Object result = interpreter.getValue("result");
        if (result != null) {
          results.put("result", result);
        }
      } catch (Exception e) {
        // no 'result' defined
      }

      return record(
          PythonExecutionResult.success(
              "Script executed successfully", results, System.currentTimeMillis() - startTime));
    } catch (Exception e) {
      LOG.error("Error executing Python script", e);
      return record(
          PythonExecutionResult.failed(e.getMessage(), e, System.currentTimeMillis() - startTime));
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
  public PythonExecutionResult executePowerShellCommand(String command) {
    return PythonExecutionResult.failed(
        "not supported: PowerShell execution was removed; use executeScript or executeScriptFile",
        null,
        0);
  }

  @Override
  public Path getPythonEnvironmentPath() {
    return PythonPlugin.environment().map(PythonEnvironment::home).orElse(null);
  }

  @Override
  public boolean isPythonAvailable() {
    return PythonPlugin.isAvailable();
  }

  @Override
  public PythonExecutionResult installPythonPackage(String packageName) {
    return PythonExecutionResult.failed(
        "not supported: runtime package installation was removed; add '"
            + packageName
            + "' to pluginsystem/plugins/PythonPlugin/python-env/requirements.in and rebuild the"
            + " environment",
        null,
        0);
  }

  private static PythonExecutionResult notAvailable() {
    return PythonExecutionResult.failed(
        "Python not available: " + PythonPlugin.unavailableReason(), null, 0);
  }

  private static PythonExecutionResult record(PythonExecutionResult result) {
    PythonExecutionMetrics.getInstance().recordExecution(result);
    return result;
  }
}
