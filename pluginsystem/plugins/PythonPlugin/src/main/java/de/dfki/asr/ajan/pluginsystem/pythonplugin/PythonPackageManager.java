package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonPackageManager {
  private static final Logger LOG = LoggerFactory.getLogger(PythonPackageManager.class);
  private static PythonPackageManager instance;
  private final Set<String> installedPackages =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  private PythonPackageManager() {}

  public static synchronized PythonPackageManager getInstance() {
    if (instance == null) {
      instance = new PythonPackageManager();
    }
    return instance;
  }

  public synchronized void installPackage(String packageName) {
    if (isPackageInstalled(packageName)) {
      LOG.info("Package {} is already installed.", packageName);
      return;
    }

    LOG.info("Installing Python package: {}", packageName);
    executePipCommand("install", packageName);
    installedPackages.add(packageName);
  }

  public synchronized void installRequirements(Path requirementsFile) {
    LOG.info("Installing requirements from: {}", requirementsFile);
    executePipCommand("install", "-r", requirementsFile.toAbsolutePath().toString());
  }

  public boolean isPackageInstalled(String packageName) {
    return installedPackages.contains(packageName);
  }

  public Set<String> listInstalledPackages() {
    return new HashSet<>(installedPackages);
  }

  public synchronized void uninstallPackage(String packageName) {
    LOG.info("Uninstalling Python package: {}", packageName);
    executePipCommand("uninstall", "-y", packageName);
    installedPackages.remove(packageName);
  }

  private void executePipCommand(String... args) {
    try {
      Path pythonExecutable =
          PythonPlugin.getPythonEnvPath()
              .resolve(
                  System.getProperty("os.name").toLowerCase().contains("win")
                      ? "python.exe"
                      : "bin/python");
      String[] command = new String[args.length + 3];
      command[0] = pythonExecutable.toAbsolutePath().toString();
      command[1] = "-m";
      command[2] = "pip";
      System.arraycopy(args, 0, command, 3, args.length);

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        LOG.error("Pip command failed with exit code: {}", exitCode);
      }
    } catch (Exception e) {
      LOG.error("Failed to execute pip command", e);
    }
  }
}
