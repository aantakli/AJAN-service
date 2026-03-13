package de.dfki.asr.ajan.pluginsystem.extensionpoints;

import org.pf4j.ExtensionPoint;
import java.nio.file.Path;
import java.util.Map;

public interface PythonExecutionService extends ExtensionPoint {

  PythonExecutionResult executeScript(String script, Map<String, Object> inputs);

  PythonExecutionResult executeScriptFile(Path scriptPath, Map<String, Object> inputs);

  PythonExecutionResult executePowerShellCommand(String command);

  Path getPythonEnvironmentPath();

  boolean isPythonAvailable();

  PythonExecutionResult installPythonPackage(String packageName);
}
