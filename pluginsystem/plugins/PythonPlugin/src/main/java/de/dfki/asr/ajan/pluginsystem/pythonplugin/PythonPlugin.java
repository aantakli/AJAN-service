package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.Optional;
import jep.JepConfig;
import jep.MainInterpreter;
import jep.PyConfig;
import jep.SharedInterpreter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the embedded CPython (via JEP) once per JVM. The plugin always reaches STARTED: when no
 * Python environment is found or initialization fails, {@link #isAvailable()} is {@code false}
 * and {@link #unavailableReason()} says why; Python nodes then fail with that reason.
 */
public class PythonPlugin extends Plugin {

  private static final Logger LOG = LoggerFactory.getLogger(PythonPlugin.class);
  private static final int RTLD_NOW = 0x2;
  private static final int RTLD_GLOBAL = 0x100;

  private static boolean initialized;
  private static volatile boolean available;
  private static volatile String unavailableReason = "Python runtime not initialized";
  private static volatile PythonEnvironment environment;

  public PythonPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    initialize();
  }

  @Override
  public void stop() {
    // CPython cannot be finalized and restarted inside one process; nothing to release here.
  }

  public static boolean isAvailable() {
    return available;
  }

  public static String unavailableReason() {
    return available ? null : unavailableReason;
  }

  public static Optional<PythonEnvironment> environment() {
    return Optional.ofNullable(environment);
  }

  /**
   * Resolves the environment and configures JEP. Idempotent: JEP's global configuration can be
   * set only once per JVM, so a failed attempt is not retried.
   *
   * @return whether Python is available
   */
  public static synchronized boolean initialize() {
    if (initialized) {
      return available;
    }
    initialized = true;
    try {
      PythonEnvironment env = PythonEnvironment.resolve();
      environment = env;
      LOG.info("Python environment: {}", env);
      loadNativeLibraries(env);
      configureJep(env);
      try (SharedInterpreter probe = new SharedInterpreter()) {
        probe.exec("import sys");
        LOG.info("Embedded Python {} ready (jep {})", probe.getValue("sys.version"),
            env.jepLibrary());
        try {
          probe.exec("import AJANlib");
        } catch (Exception e) {
          LOG.warn("AJANlib cannot be imported (scripts using it will fail): {}", e.toString());
        }
      }
      available = true;
    } catch (PythonUnavailableException e) {
      unavailableReason = e.getMessage();
      LOG.warn("Python not available: {}", unavailableReason);
    } catch (Throwable t) { // JepException, UnsatisfiedLinkError, ...
      unavailableReason = "initialization failed: " + t;
      LOG.error("Python not available: {}", unavailableReason, t);
    }
    return available;
  }

  private static void loadNativeLibraries(PythonEnvironment env) throws Throwable {
    boolean windows = PythonEnvironment.isWindows();
    for (Path lib : env.nativePreloads()) {
      // Satisfies jep's DT_NEEDED / import of libpython (libjep has no rpath) by soname.
      System.load(lib.toString());
      if (!windows) {
        // C extension modules (e.g. orjson) are not linked against libpython and resolve Py*
        // symbols from the global namespace; System.load uses RTLD_LOCAL.
        dlopenGlobal(lib);
      }
      LOG.debug("Preloaded {}", lib);
    }
    MainInterpreter.setJepLibraryPath(env.jepLibrary().toString());
  }

  private static void dlopenGlobal(Path lib) throws Throwable {
    Linker linker = Linker.nativeLinker();
    MemorySegment dlopenAddr =
        linker
            .defaultLookup()
            .find("dlopen")
            .orElseThrow(() -> new IllegalStateException("dlopen not found"));
    MethodHandle dlopen =
        linker.downcallHandle(
            dlopenAddr,
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment handle =
          (MemorySegment) dlopen.invokeExact(arena.allocateFrom(lib.toString()),
              RTLD_NOW | RTLD_GLOBAL);
      if (handle.equals(MemorySegment.NULL)) {
        throw new IllegalStateException("dlopen(RTLD_GLOBAL) failed for " + lib);
      }
    }
  }

  private static void configureJep(PythonEnvironment env) throws Exception {
    PyConfig pyConfig = new PyConfig();
    pyConfig.setPythonHome(env.home().toString());
    // Deterministic: ignore PYTHONHOME/PYTHONPATH of the host and the user's site-packages.
    pyConfig.setIgnoreEnvironmentFlag(1);
    pyConfig.setNoUserSiteDirectory(1);
    MainInterpreter.setInitParams(pyConfig);

    JepConfig jepConfig = new JepConfig();
    jepConfig.addIncludePaths(env.ajanLib().toString(), env.sitePackages().toString());
    SharedInterpreter.setConfig(jepConfig);
  }
}
