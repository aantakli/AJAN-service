package de.dfki.asr.ajan.pluginsystem.extensionpoints;

import java.nio.file.Path;
import java.util.Map;
import org.pf4j.ExtensionPoint;

public interface PythonExecutionService extends ExtensionPoint {

  PythonExecutionResult executeScript(String script, Map<String, Object> inputs);

  PythonExecutionResult executeScriptFile(Path scriptPath, Map<String, Object> inputs);

  @Deprecated
  PythonExecutionResult executePowerShellCommand(String command);

  Path getPythonEnvironmentPath();

  boolean isPythonAvailable();

  PythonExecutionResult installPythonPackage(String packageName);
}
