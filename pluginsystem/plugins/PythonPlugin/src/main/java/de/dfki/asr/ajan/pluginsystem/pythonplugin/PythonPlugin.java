package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jep.JepConfig;
import jep.JepException;
import jep.MainInterpreter;
import jep.PyConfig;
import jep.SharedInterpreter;
import lombok.Getter;
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
  private static jep.JepConfig jepConfig = null;
  @Getter private static Path pythonEnvPath = null;

  public PythonPlugin(PluginWrapper wrapper) throws IOException, URISyntaxException {
    super(wrapper);
    System.out.println("PythonPlugin.constructor()");

    setupEmbeddedPythonEnv();
    initializeJep(pythonEnvPath);
  }

  public static SharedInterpreter getMainInterpreter() {
    return mainInterpreter;
  }

  public static jep.JepConfig getJepConfig() {
    return jepConfig;
  }

  private static void initializeJep(Path pythonHome) {
    try {
      Path sitePkgs = findSitePackages(pythonHome);
      if (sitePkgs == null) {
        throw new RuntimeException("site-packages not found for JEP initialization");
      }
      Path jepPath = sitePkgs.resolve("jep");

      String pythonPath = String.join(
          File.pathSeparator,
          pythonHome.toAbsolutePath().toString(),
          sitePkgs.getParent().toAbsolutePath().toString(),
          sitePkgs.toAbsolutePath().toString(),
          jepPath.toAbsolutePath().toString());

      // Tell JEP to call Py_SetPythonHome() natively before Py_Initialize().
      // The reflection-based setEnv() above only mutates Java's view of getenv()
      // and does not update the C environ array, so the embedded interpreter
      // would otherwise fail with "ModuleNotFoundError: No module named 'encodings'".
      // Must be set before the first MainInterpreter access (i.e. before any
      // SharedInterpreter is constructed).
      PyConfig pyConfig = new PyConfig();
      pyConfig.setPythonHome(pythonHome.toAbsolutePath().toString());
      MainInterpreter.setInitParams(pyConfig);

      jepConfig = new JepConfig();
      for (String path : pythonPath.split(File.pathSeparator)) {
        jepConfig.addIncludePaths(path);
      }

      SharedInterpreter.setConfig(jepConfig);
      mainInterpreter = new SharedInterpreter();

      LOG.info("Successfully initialized JEP with embedded Python environment");

    } catch (IOException e) {
      LOG.error("Failed to locate site-packages for JEP", e);
      throw new RuntimeException("JEP initialization failed", e);
    } catch (JepException e) {
      LOG.error("Failed to initialize JEP", e);
      throw new RuntimeException("JEP initialization failed", e);
    }
  }

  private static void fixJepBatFile(Path pythonHome) throws IOException {
    Path jepBatPath = pythonHome.resolve("Scripts").resolve("jep.bat");

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
      LOG.debug("jep.bat not present (expected on non-Windows): {}", jepBatPath);
    }
  }

  /**
   * Finds the actual Python home inside an extracted zip. Handles the case where the zip has a
   * single root wrapper directory (e.g. python-portable/).
   */
  private static Path findPythonHome(Path extractedRoot) throws IOException {
    try (var stream = Files.list(extractedRoot)) {
      List<Path> children = stream.collect(Collectors.toList());
      if (children.size() == 1 && Files.isDirectory(children.get(0))) {
        LOG.info("Python home resolved to: {}", children.get(0));
        return children.get(0);
      }
    }
    return extractedRoot;
  }

  /**
   * Locates site-packages inside the given Python home, handling both Windows-style (Lib/) and
   * Linux-style (lib/python3.x/) layouts.
   */
  private static Path findSitePackages(Path pythonHome) throws IOException {
    // Windows: Lib/site-packages
    Path winStyle = pythonHome.resolve("Lib").resolve("site-packages");
    if (Files.exists(winStyle)) return winStyle;

    // Linux: lib/python3.x/site-packages
    Path libDir = pythonHome.resolve("lib");
    if (Files.exists(libDir)) {
      try (var stream = Files.list(libDir)) {
        Optional<Path> pythonDir = stream
            .filter(Files::isDirectory)
            .filter(p -> p.getFileName().toString().startsWith("python"))
            .findFirst();
        if (pythonDir.isPresent()) {
          Path candidate = pythonDir.get().resolve("site-packages");
          if (Files.exists(candidate)) return candidate;
        }
      }
    }
    return null;
  }

  private static Path setupEmbeddedPythonEnv() throws IOException {
    String os = System.getProperty("os.name").toLowerCase();
    String envFolder = os.contains("win") ? "win_env" : "nix_env";
    Path tempDir = Files.createTempDirectory("embedded_python_env");
    unzip(PythonPlugin.class.getResourceAsStream("/" + envFolder + ".zip"), tempDir);

    Path pythonHome = findPythonHome(tempDir);
    pythonEnvPath = pythonHome;

    Path sitePkgs = findSitePackages(pythonHome);
    if (sitePkgs == null) {
      throw new IOException("site-packages not found in embedded Python environment");
    }

    Path jepPath = sitePkgs.resolve("jep");
    if (!Files.exists(jepPath)) {
      LOG.error("JEP not found in embedded environment at: {}", jepPath);
      throw new IOException("JEP not found in embedded Python environment");
    }
    LOG.info("JEP found in embedded environment at: {}", jepPath);

    fixJepBatFile(pythonHome);

    String pythonHomeStr = pythonHome.toAbsolutePath().toString();
    String pythonPath = String.join(
        File.pathSeparator,
        pythonHomeStr,
        sitePkgs.getParent().toAbsolutePath().toString(),
        sitePkgs.toAbsolutePath().toString(),
        jepPath.toAbsolutePath().toString());

    setEnv("PYTHONHOME", pythonHomeStr);
    setEnv("PYTHONPATH", pythonPath);

    System.setProperty("jep.python.home", pythonHomeStr);
    System.setProperty("PYTHONHOME", pythonHomeStr);
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

    try {
      addLibraryPath(pythonHomeStr);
      addLibraryPath(jepPath.toString());

      if (os.contains("win")) {
        Path dllsPath = pythonHome.resolve("DLLs");
        if (Files.exists(dllsPath)) {
          addLibraryPath(dllsPath.toString());
          prependToPathEnv(dllsPath.toString());
        }
        prependToPathEnv(jepPath.toString());
        prependToPathEnv(pythonHomeStr);
      }
    } catch (Exception e) {
      LOG.error("Failed to add to library path", e);
    }

    LOG.info("Configured embedded Python environment:");
    LOG.info(" - PYTHONHOME: {}", pythonHomeStr);
    LOG.info(" - PYTHONPATH: {}", pythonPath);
    LOG.info(" - jep.library.path: {}", jepPath.toAbsolutePath());

    return pythonHome;
  }

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

  private static void prependToPathEnv(String dir) {
    String currentPath = System.getenv("PATH");
    setEnv("PATH", dir + File.pathSeparator + currentPath);
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
