/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import jep.JepException;
import jep.SubInterpreter;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonPlugin extends Plugin {

  private static final Logger LOG = LoggerFactory.getLogger(PythonPlugin.class);

  public PythonPlugin(PluginWrapper wrapper) throws IOException, URISyntaxException {
    super(wrapper);
    System.out.println("PythonPlugin.constructor()");
    debugJepLoading(setupEmbeddedPythonEnv());
    testRunPythonScript();
  }

  private static void testRunPythonScript() {
    try (SubInterpreter subInterp = new SubInterpreter()) {
      subInterp.runScript("print('Hello from Python!')");
    } catch (JepException e) {
      LOG.error("Error running Python script", e);
    }
  }

  private static void debugJepLoading(Path pythonHomePath) {
    try {
      Path jepPath =
          pythonHomePath.resolve("Lib").resolve("site-packages").resolve("jep").resolve("jep.dll");
      LOG.info("Attempting to load JEP from: {}", jepPath);
      LOG.info("File exists: {}", Files.exists(jepPath));
      LOG.info("File size: {} bytes", Files.size(jepPath));
      LOG.info("JVM architecture: {}-bit", System.getProperty("sun.arch.data.model"));

      // Try direct loading
      System.load(jepPath.toAbsolutePath().toString());
      LOG.info("Successfully loaded JEP library");
    } catch (Throwable e) {
      LOG.error("Failed to load JEP: {} - {}", e.getClass().getName(), e.getMessage());
      e.printStackTrace();
    }
  }

  private static Path setupEmbeddedPythonEnv() throws IOException {
    String os = System.getProperty("os.name").toLowerCase();
    String envFolder = os.contains("win") ? "win_env" : "nix_env";
    String resourcePath = "/" + envFolder;
    Path tempDir = Files.createTempDirectory("embedded_python_env");
    unzip(PythonPlugin.class.getResourceAsStream(resourcePath + ".zip"), tempDir);

    // Set environment variables for JEP
    String pythonHome = tempDir.toString();
    System.setProperty("jep.python.home", pythonHome);
    System.setProperty("PYTHONHOME", pythonHome);

    // Add the JEP directory to java.library.path dynamically
    if (os.contains("win")) {
      Path jepDir = tempDir.resolve("Lib").resolve("site-packages").resolve("jep");
      try {
        addLibraryPath(jepDir.toString());
        // No need to call System.load() directly, JEP will find it now
      } catch (Exception e) {
        LOG.error("Failed to add JEP directory to library path", e);
      }
    } else {
      // Linux/macOS handling
      Path libDir = tempDir.resolve("lib");
      try {
        addLibraryPath(libDir.toString());
      } catch (Exception e) {
        LOG.error("Failed to add lib directory to library path", e);
      }
    }

    return tempDir;
  }

  // Helper method to add paths to java.library.path at runtime
  private static void addLibraryPath(String pathToAdd) throws Exception {
    Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    String[] paths = (String[]) usrPathsField.get(null);
    String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length - 1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }

  private static void unzip(InputStream is, Path targetDir) throws IOException {
    LOG.debug("Unzipping to directory: {}", targetDir);

    // Create temp file from input stream
    Path tempZipFile = Files.createTempFile("temp_", ".zip");
    Files.copy(is, tempZipFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    try {
      // Use Zip4j to extract - it handles directory/file conflicts better
      ZipFile zipFile = new ZipFile(tempZipFile.toFile());
      zipFile.extractAll(targetDir.toString());
    } catch (ZipException e) {
      LOG.error("Failed to extract ZIP with Zip4j: {}", e.getMessage());
      throw new IOException("Failed to extract ZIP", e);
    } finally {
      // Clean up the temporary zip file
      Files.deleteIfExists(tempZipFile);
    }
  }

  @Override
  public void start() {
    System.out.println("PythonPlugin.start()");
    if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
      LOG.debug("PythonPlugin");
    }
  }

  @Override
  public void stop() {
    System.out.println("PythonPlugin.stop()");
  }
}
