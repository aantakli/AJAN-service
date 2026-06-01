package de.dfki.asr.ajan.pluginsystem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptExtractor.class);
  private static final Map<String, Path> extractionCache = new ConcurrentHashMap<>();

  public static Path extractScriptsFromPlugin(Plugin plugin, String resourcePath)
      throws IOException {
    String pluginId = plugin.getWrapper().getPluginId();
    if (extractionCache.containsKey(pluginId)) {
      return extractionCache.get(pluginId);
    }

    Path tempDir = Files.createTempDirectory("ajan-plugin-scripts-" + pluginId);
    String jarPath = plugin.getWrapper().getPluginPath().toString();

    if (jarPath.endsWith(".jar")) {
      LOG.info("Plugin {} is a JAR, extracting scripts...", pluginId);
      try (JarFile jar = new JarFile(jarPath)) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          String name = entry.getName();
          if (name.startsWith(resourcePath) && !entry.isDirectory()) {
            String relativeName = name.substring(resourcePath.length());
            if (relativeName.startsWith("/")) {
              relativeName = relativeName.substring(1);
            }
            Path targetFile = tempDir.resolve(relativeName);
            Files.createDirectories(targetFile.getParent());
            try (InputStream is = jar.getInputStream(entry)) {
              Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
          }
        }
      }
    } else {
      // Development mode - files are likely in the file system
      LOG.info(
          "Plugin {} is loaded from a directory (not a JAR): {}. Using direct file access for scripts.",
          pluginId,
          jarPath);
      // In dev mode we might just return the resource path relative to the plugin path
      Path pluginPath = plugin.getWrapper().getPluginPath();

      // Try multiple common development paths
      Path[] possiblePaths = {
        pluginPath.resolve("src/main/resources").resolve(resourcePath),
        pluginPath.resolve("target/classes").resolve(resourcePath),
        pluginPath.resolve(resourcePath),
        // If we are in the root and pluginPath is just a relative path to the plugin dir
        Paths.get("pluginsystem", "plugins", pluginId, "src", "main", "resources", resourcePath),
        Paths.get("pluginsystem", "plugins", pluginId, "target", "classes", resourcePath)
      };

      for (Path devPath : possiblePaths) {
        if (Files.exists(devPath)) {
          extractionCache.put(pluginId, devPath);
          LOG.info("Found scripts for plugin {} in development path {}", pluginId, devPath);
          return devPath;
        }
      }

      LOG.error(
          "Failed to find scripts for plugin {} in dev mode. Searched in several locations relative to {}",
          pluginId,
          pluginPath);
    }

    extractionCache.put(pluginId, tempDir);
    LOG.info("Extracted scripts for plugin {} to {}", pluginId, tempDir);
    return tempDir;
  }

  public static Path getScriptPath(Plugin plugin, String scriptName) {
    Path baseDir = extractionCache.get(plugin.getWrapper().getPluginId());
    if (baseDir == null) {
      return null;
    }
    return baseDir.resolve(scriptName);
  }

  public static void cleanupScripts(Plugin plugin) {
    Path tempDir = extractionCache.remove(plugin.getWrapper().getPluginId());
    if (tempDir != null && tempDir.toString().contains("ajan-plugin-scripts")) {
      try {
        deleteDirectoryRecursively(tempDir);
        LOG.info("Cleaned up scripts for plugin {}", plugin.getWrapper().getPluginId());
      } catch (IOException e) {
        LOG.error("Failed to cleanup scripts for plugin {}", plugin.getWrapper().getPluginId(), e);
      }
    }
  }

  private static void deleteDirectoryRecursively(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.list(path)
          .forEach(
              child -> {
                try {
                  deleteDirectoryRecursively(child);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
    Files.delete(path);
  }
}
