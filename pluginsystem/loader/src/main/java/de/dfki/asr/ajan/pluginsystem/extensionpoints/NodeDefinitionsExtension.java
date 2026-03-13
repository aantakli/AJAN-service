package de.dfki.asr.ajan.pluginsystem.extensionpoints;

import org.pf4j.ExtensionPoint;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class NodeDefinitionsExtension implements ExtensionPoint {

  public Path getDefinitionPath() {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream("node_definitions.ttl")) {
      if (inputStream == null) {
        // Try searching for it if it's not in the root
        return null;
      }
      // Create a temporary file to hold the TTL data
      Path tempFile = Files.createTempFile("ajan-" + getClass().getSimpleName() + "-", ".ttl");
      Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      // Register for deletion on exit
      tempFile.toFile().deleteOnExit();
      return tempFile;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

