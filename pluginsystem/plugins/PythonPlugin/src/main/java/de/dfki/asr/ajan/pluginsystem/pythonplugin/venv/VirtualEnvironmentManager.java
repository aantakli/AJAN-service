package de.dfki.asr.ajan.pluginsystem.pythonplugin.venv;

import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualEnvironmentManager {
  private static final Logger LOG = LoggerFactory.getLogger(VirtualEnvironmentManager.class);
  private static VirtualEnvironmentManager instance;
  private final Map<String, Path> pluginVenvs = new ConcurrentHashMap<>();

  private VirtualEnvironmentManager() {}

  public static synchronized VirtualEnvironmentManager getInstance() {
    if (instance == null) {
      instance = new VirtualEnvironmentManager();
    }
    return instance;
  }

  public Path createVirtualEnv(String pluginId) throws IOException {
    Path baseDir = PythonPlugin.getPythonEnvPath().getParent().resolve("venvs");
    Files.createDirectories(baseDir);
    Path venvPath = baseDir.resolve(pluginId);

    if (!Files.exists(venvPath)) {
      LOG.info("Creating virtual environment for plugin {}: {}", pluginId, venvPath);
      executeVenvCommand(venvPath);
    }

    pluginVenvs.put(pluginId, venvPath);
    return venvPath;
  }

  public void deleteVirtualEnv(String pluginId) {
    Path venvPath = pluginVenvs.remove(pluginId);
    if (venvPath != null && Files.exists(venvPath)) {
      LOG.info("Deleting virtual environment for plugin {}: {}", pluginId, venvPath);
      // Deletion could be implemented here (recursive delete)
    }
  }

  public Path getVenvPath(String pluginId) {
    return pluginVenvs.get(pluginId);
  }

  private void executeVenvCommand(Path venvPath) {
    try {
      Path pythonExecutable =
          PythonPlugin.getPythonEnvPath()
              .resolve(
                  System.getProperty("os.name").toLowerCase().contains("win")
                      ? "python.exe"
                      : "bin/python");
      ProcessBuilder pb =
          new ProcessBuilder(
              pythonExecutable.toAbsolutePath().toString(),
              "-m",
              "venv",
              venvPath.toAbsolutePath().toString());
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        LOG.error("Venv command failed with exit code: {}", exitCode);
      }
    } catch (Exception e) {
      LOG.error("Failed to execute venv command", e);
    }
  }
}
