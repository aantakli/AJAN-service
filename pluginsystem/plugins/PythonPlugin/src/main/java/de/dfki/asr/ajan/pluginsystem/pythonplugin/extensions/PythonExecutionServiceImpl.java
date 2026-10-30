package de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonEnvironment;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPlugin;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.monitoring.PythonExecutionMetrics;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import jep.SharedInterpreter;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs scripts in a fresh {@link SharedInterpreter} on the calling thread. Not synchronized: each
 * call has its own globals; the GIL serializes Python bytecode, and blocking calls release it.
 * Exception: {@link #executeScriptFile} calls with {@code argv} (process-global {@code sys.argv}).
 */
@Extension
public class PythonExecutionServiceImpl implements PythonExecutionService {

  private static final Logger LOG = LoggerFactory.getLogger(PythonExecutionServiceImpl.class);

  /** Input key of {@link #executeScriptFile}: {@code List} of arguments for {@code sys.argv}. */
  public static final String ARGV_INPUT = "argv";

  /** Serializes {@link #executeScriptFile} calls that set the process-global {@code sys.argv}. */
  private static final ReentrantLock ARGV_LOCK = new ReentrantLock();

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

      return record(
          PythonExecutionResult.success(
              "Script executed successfully",
              collectResults(interpreter, inputs),
              System.currentTimeMillis() - startTime));
    } catch (Exception e) {
      LOG.error("Error executing Python script", e);
      return record(
          PythonExecutionResult.failed(e.getMessage(), e, System.currentTimeMillis() - startTime));
    }
  }

  /**
   * Runs the file as {@code __main__} ({@code __file__} set, its directory in front of {@code
   * sys.path} while it runs, see {@code AJANlib.run_script_file}). Input {@code argv} ({@link
   * List}) becomes {@code sys.argv[1:]}. {@code SystemExit} with code 0/None is success, any other
   * code is a failure.
   *
   * <p>{@code sys.argv} is process-global, so calls that pass {@code argv} are serialized by
   * {@link #ARGV_LOCK} for their whole run (only those; calls without {@code argv} stay
   * concurrent). Long-running scripts must therefore not take {@code argv}: pass parameters as
   * input variables instead.
   */
  @Override
  public PythonExecutionResult executeScriptFile(Path scriptPath, Map<String, Object> inputs) {
    long startTime = System.currentTimeMillis();
    if (scriptPath == null || !Files.isRegularFile(scriptPath)) {
      return record(
          PythonExecutionResult.failed(
              "Error reading script file: not found: " + scriptPath, null, 0));
    }
    if (!PythonPlugin.isAvailable()) {
      return record(notAvailable());
    }
    Object argv = inputs == null ? null : inputs.get(ARGV_INPUT);
    boolean withArgv = argv instanceof List;
    if (withArgv) {
      ARGV_LOCK.lock();
    }
    try (SharedInterpreter interpreter = new SharedInterpreter()) {
      if (inputs != null) {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
          interpreter.set(entry.getKey(), entry.getValue());
        }
      }
      interpreter.set("_ajan_script_path", scriptPath.toAbsolutePath().toString());
      interpreter.set("_ajan_argv", withArgv ? argv : null);
      interpreter.exec(
          "import AJANlib as _ajan_lib\n"
              + "_ajan_ok, _ajan_message = _ajan_lib.run_script_file("
              + "globals(), _ajan_script_path, _ajan_argv)\n");
      long elapsed = System.currentTimeMillis() - startTime;
      if (!Boolean.TRUE.equals(interpreter.getValue("_ajan_ok", Boolean.class))) {
        String message = interpreter.getValue("_ajan_message", String.class);
        LOG.warn("Python script {} failed: {}", scriptPath, message);
        return record(PythonExecutionResult.failed(message, null, elapsed));
      }
      return record(
          PythonExecutionResult.success(
              "Script executed successfully", collectResults(interpreter, inputs), elapsed));
    } catch (Exception e) {
      LOG.error("Error executing Python script file: {}", scriptPath, e);
      return record(
          PythonExecutionResult.failed(e.getMessage(), e, System.currentTimeMillis() - startTime));
    } finally {
      if (withArgv) {
        ARGV_LOCK.unlock();
      }
    }
  }

  /** JEP cannot list globals: collects the input names (possibly reassigned) and 'result'. */
  private static Map<String, Object> collectResults(
      SharedInterpreter interpreter, Map<String, Object> inputs) {
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
    return results;
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
