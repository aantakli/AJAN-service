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
    private final Set<String> installedPackages = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private PythonPackageManager() {
    }

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
        PowerShellManager psManager = PythonPlugin.getPowerShellManager();
        if (psManager != null) {
            psManager.executeCommand("pip install " + packageName);
            installedPackages.add(packageName);
        } else {
            LOG.error("Cannot install package {}: PowerShellManager is not available.", packageName);
        }
    }

    public synchronized void installRequirements(Path requirementsFile) {
        LOG.info("Installing requirements from: {}", requirementsFile);
        PowerShellManager psManager = PythonPlugin.getPowerShellManager();
        if (psManager != null) {
            psManager.executeCommand("pip install -r '" + requirementsFile.toAbsolutePath().toString().replace("'", "''") + "'");
        } else {
            LOG.error("Cannot install requirements: PowerShellManager is not available.");
        }
    }

    public boolean isPackageInstalled(String packageName) {
        return installedPackages.contains(packageName);
    }

    public Set<String> listInstalledPackages() {
        return new HashSet<>(installedPackages);
    }

    public synchronized void uninstallPackage(String packageName) {
        LOG.info("Uninstalling Python package: {}", packageName);
        PowerShellManager psManager = PythonPlugin.getPowerShellManager();
        if (psManager != null) {
            psManager.executeCommand("pip uninstall -y " + packageName);
            installedPackages.remove(packageName);
        } else {
            LOG.error("Cannot uninstall package {}: PowerShellManager is not available.", packageName);
        }
    }
}
