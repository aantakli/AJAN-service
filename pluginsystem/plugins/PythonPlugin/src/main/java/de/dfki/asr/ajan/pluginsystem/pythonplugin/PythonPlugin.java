package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import jep.JepConfig;
import jep.JepException;
import jep.SharedInterpreter;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonPlugin extends Plugin {

  private static final Logger LOG = LoggerFactory.getLogger(PythonPlugin.class);
  private static SharedInterpreter mainInterpreter = null;

  public PythonPlugin(PluginWrapper wrapper) throws IOException, URISyntaxException {
    super(wrapper);
    System.out.println("PythonPlugin.constructor()");

    // Setup environment FIRST
    Path pythonEnvPath = setupEmbeddedPythonEnv();

    // Initialize JEP with proper configuration
    initializeJep(pythonEnvPath);
  }

  private static void initializeJep(Path pythonEnvPath) {
    try {
      String pythonPath =
          String.join(
              File.pathSeparator,
              pythonEnvPath.toAbsolutePath().toString(),
              pythonEnvPath.resolve("Lib").toAbsolutePath().toString(),
              pythonEnvPath.resolve("Lib").resolve("site-packages").toAbsolutePath().toString(),
              pythonEnvPath
                  .resolve("Lib")
                  .resolve("site-packages")
                  .resolve("jep")
                  .toAbsolutePath()
                  .toString());

      JepConfig config = new JepConfig();

      // Use addIncludePaths for Python module paths
      for (String path : pythonPath.split(File.pathSeparator)) {
        config.addIncludePaths(path);
      }

      // Initialize SharedInterpreter (no config parameter)
      SharedInterpreter.setConfig(config);
      mainInterpreter = new SharedInterpreter();

      LOG.info("Successfully initialized JEP with embedded Python environment");

    } catch (JepException e) {
      LOG.error("Failed to initialize JEP", e);
      throw new RuntimeException("JEP initialization failed", e);
    }
  }

  private static void fixJepBatFile(Path tempDir) throws IOException {
    Path jepBatPath = tempDir.resolve("Scripts").resolve("jep.bat");

    if (Files.exists(jepBatPath)) {
      LOG.info("Fixing jep.bat file at: {}", jepBatPath);

      String correctedBatContent =
          "@echo off\n"
              + "SETLOCAL\n"
              + "SET VIRTUAL_ENV=\n"
              + "\n"
              + "IF NOT \"%VIRTUAL_ENV%\"==\"\" (\n"
              + "    SET PATH=\"%VIRTUAL_ENV%\\bin\";\"%JAVA_HOME%\\bin\";\"%PATH%\"\n"
              + "    SET PYTHONHOME=%VIRTUAL_ENV%\n"
              + ")\n"
              + "\n"
              + "REM Use relative paths to the embedded environment\n"
              + "SET EMBEDDED_HOME=%~dp0..\n"
              + "SET cp=\"%EMBEDDED_HOME%\\Lib\\site-packages\\jep\\jep-4.2.2.jar\"\n"
              + "IF DEFINED CLASSPATH (\n"
              + "    SET cp=%cp%;%CLASSPATH%\n"
              + ")\n"
              + "\n"
              + "SET jni_path=\"%EMBEDDED_HOME%\\Lib\\site-packages\\jep\"\n"
              + "\n"
              + "SET args=%*\n"
              + "IF \"%args%\"==\"\" (\n"
              + "    SET args=\"%EMBEDDED_HOME%\\Lib\\site-packages\\jep\\console.py\"\n"
              + ")\n"
              + "\n"
              + "java -classpath %cp% -Djava.library.path=%jni_path% jep.Run %args%\n"
              + "ENDLOCAL\n";

      Files.write(jepBatPath, correctedBatContent.getBytes());
      LOG.info("Fixed jep.bat file with embedded environment paths");
    } else {
      LOG.warn("jep.bat not found at expected location: {}", jepBatPath);
    }
  }

  private static Path setupEmbeddedPythonEnv() throws IOException {
    String os = System.getProperty("os.name").toLowerCase();
    String envFolder = os.contains("win") ? "win_env" : "nix_env";
    String resourcePath = "/" + envFolder;
    Path tempDir = Files.createTempDirectory("embedded_python_env");
    unzip(PythonPlugin.class.getResourceAsStream(resourcePath + ".zip"), tempDir);

    Path jepPath = tempDir.resolve("Lib").resolve("site-packages").resolve("jep");
    if (!Files.exists(jepPath)) {
      LOG.error("JEP not found in embedded environment at: {}", jepPath);
      throw new IOException("JEP not found in embedded Python environment");
    } else {
      LOG.info("JEP found in embedded environment at: {}", jepPath);
    }

    fixJepBatFile(tempDir);

    String pythonHome = tempDir.toAbsolutePath().toString();
    Path sitePkgsPath = tempDir.resolve("Lib").resolve("site-packages");

    String pythonPath =
        String.join(
            File.pathSeparator,
            pythonHome,
            pythonHome + File.separator + "Lib",
            sitePkgsPath.toAbsolutePath().toString(),
            jepPath.toAbsolutePath().toString());

    // Set all environment variables and system properties BEFORE JEP loads
    setEnv("PYTHONHOME", pythonHome);
    setEnv("PYTHONPATH", pythonPath);

    System.setProperty("jep.python.home", pythonHome);
    System.setProperty("PYTHONHOME", pythonHome);
    System.setProperty("PYTHONPATH", pythonPath);
    System.setProperty("jep.pythonpath", pythonPath);
    System.setProperty("jep.library.path", jepPath.toAbsolutePath().toString());

    Path jepJarPath = jepPath.resolve("jep-4.2.2.jar");
    if (Files.exists(jepJarPath)) {
      System.setProperty("jep.jar.path", jepJarPath.toAbsolutePath().toString());
      LOG.info("JEP JAR found at: {}", jepJarPath);
    } else {
      LOG.warn("JEP JAR not found at: {}", jepJarPath);
    }

    // Add library paths for native libraries
    try {
      addLibraryPath(pythonHome);
      addLibraryPath(jepPath.toString());

      if (os.contains("win")) {
        Path dllsPath = tempDir.resolve("DLLs");
        if (Files.exists(dllsPath)) {
          addLibraryPath(dllsPath.toString());
          prependToPathEnv(dllsPath.toString());
        }
        prependToPathEnv(jepPath.toString());
        prependToPathEnv(pythonHome);
      }
    } catch (Exception e) {
      LOG.error("Failed to add to library path", e);
    }

    LOG.info("Configured embedded Python environment:");
    LOG.info(" - PYTHONHOME: {}", pythonHome);
    LOG.info(" - PYTHONPATH: {}", pythonPath);
    LOG.info(" - jep.library.path: {}", jepPath.toAbsolutePath());

    return tempDir;
  }

  // Helper method to add paths to java.library.path at runtime
  private static void addLibraryPath(String pathToAdd) throws Exception {
    Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    String[] paths = (String[]) usrPathsField.get(null);
    for (String p : paths) {
      if (p.equals(pathToAdd)) {
        return;
      }
    }
    String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length - 1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }

  // Helper method to update environment variables (for current process)
  private static void setEnv(String name, String value) {
    try {
      Map<String, String> env = System.getenv();
      Class<?> cl = env.getClass();
      Field field = cl.getDeclaredField("m");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> writableEnv = (Map<String, String>) field.get(env);
      writableEnv.put(name, value);
    } catch (Exception e) {
      // fallback for Windows
      try {
        Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
        Field theEnvironmentField = pe.getDeclaredField("theEnvironment");
        theEnvironmentField.setAccessible(true);
        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
        env.put(name, value);

        Field cienvField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
        cienvField.setAccessible(true);
        Map<String, String> cienv = (Map<String, String>) cienvField.get(null);
        cienv.put(name, value);
      } catch (Exception ignored) {
      }
    }
  }

  // Prepend a directory to the process PATH environment variable
  private static void prependToPathEnv(String dir) {
    String currentPath = System.getenv("PATH");
    String newPath = dir + File.pathSeparator + currentPath;
    setEnv("PATH", newPath);
  }

  private static void unzip(InputStream is, Path targetDir) throws IOException {
    LOG.debug("Unzipping to directory: {}", targetDir);

    Path tempZipFile = Files.createTempFile("temp_", ".zip");
    Files.copy(is, tempZipFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    try {
      ZipFile zipFile = new ZipFile(tempZipFile.toFile());
      zipFile.extractAll(targetDir.toString());
    } catch (ZipException e) {
      LOG.error("Failed to extract ZIP with Zip4j: {}", e.getMessage());
      throw new IOException("Failed to extract ZIP", e);
    } finally {
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
    if (mainInterpreter != null) {
      try {
        mainInterpreter.close();
        mainInterpreter = null;
      } catch (Exception e) {
        LOG.warn("Error closing main interpreter", e);
      }
    }
  }
}
