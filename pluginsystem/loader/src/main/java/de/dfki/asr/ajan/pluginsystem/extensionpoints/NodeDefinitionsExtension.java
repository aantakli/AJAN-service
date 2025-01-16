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
                throw new IllegalArgumentException("File not found!");
            }
            String filePath = System.getProperty("java.class.path");
            String[] packagePath = this.getClass().getPackage().toString().split("\\.");
            Path path = Paths.get(filePath).toAbsolutePath().getParent().resolve(packagePath[packagePath.length - 2] + "_node_definitions.ttl");
            Files.deleteIfExists(path);
            Files.copy(inputStream, path);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

