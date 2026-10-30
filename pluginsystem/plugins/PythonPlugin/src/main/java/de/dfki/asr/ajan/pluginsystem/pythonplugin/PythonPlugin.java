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
        try {
          probe.exec("import rdflib");
        } catch (Exception e) {
          // Script files (OPCUA, CustomScriptNode) do not need it: AJANlib imports it lazily.
          LOG.warn("rdflib cannot be imported, PythonLeafNode scripts will fail ({}); install"
              + " rdflib into the Python environment {}", e.toString(), env.home());
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
        preloadGlobal(lib.toString());
      }
      LOG.debug("Preloaded {}", lib);
    }
    if (!windows && env.nativePreloads().isEmpty()) {
      // External (e.g. distro) Python whose libpython lives outside <home>/lib: load by soname.
      Optional<String> soname = env.libpythonSoname();
      if (soname.isEmpty()) {
        LOG.warn("No libpython preloaded (soname unknown for {}); jep or native extension"
            + " modules may fail to load", env.home());
      } else if (preloadGlobal(soname.get())) {
        LOG.info("Preloaded {} by soname", soname.get());
      }
    }
    MainInterpreter.setJepLibraryPath(env.jepLibrary().toString());
  }

  /**
   * {@link #dlopenGlobal}; a failing dlopen propagates, so Python becomes unavailable with the
   * dlerror text as reason. Only denied native access (JVM started without {@code
   * --enable-native-access}) is downgraded to a warning, because jep itself may still load.
   *
   * @return whether the library was loaded with RTLD_GLOBAL
   */
  private static boolean preloadGlobal(String lib) throws Throwable {
    try {
      dlopenGlobal(lib);
      return true;
    } catch (IllegalCallerException e) {
      LOG.warn("Cannot preload {} with RTLD_GLOBAL ({}); start the JVM with"
          + " --enable-native-access=ALL-UNNAMED, otherwise native extension modules may fail",
          lib, e.getMessage());
      return false;
    }
  }

  /** {@code dlopen(lib, RTLD_NOW | RTLD_GLOBAL)} via FFM; failures carry dlerror()'s text. */
  static void dlopenGlobal(String lib) throws Throwable {
    Linker linker = Linker.nativeLinker();
    MethodHandle dlopen =
        linker.downcallHandle(
            symbol(linker, "dlopen"),
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    // Resolved before dlopen: a later symbol lookup (dlsym) would reset the dlerror state.
    MethodHandle dlerror =
        linker.downcallHandle(
            symbol(linker, "dlerror"), FunctionDescriptor.of(ValueLayout.ADDRESS));
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment handle =
          (MemorySegment) dlopen.invokeExact(arena.allocateFrom(lib), RTLD_NOW | RTLD_GLOBAL);
      if (handle.equals(MemorySegment.NULL)) {
        throw new IllegalStateException(
            "dlopen(RTLD_GLOBAL) failed for " + lib + ": " + dlerror(dlerror));
      }
    }
  }

  private static String dlerror(MethodHandle dlerror) {
    try {
      MemorySegment msg = (MemorySegment) dlerror.invokeExact();
      return msg.equals(MemorySegment.NULL)
          ? "(no dlerror)"
          : msg.reinterpret(Long.MAX_VALUE).getString(0);
    } catch (Throwable t) {
      return "(dlerror unavailable: " + t + ")";
    }
  }

  private static MemorySegment symbol(Linker linker, String name) {
    return linker
        .defaultLookup()
        .find(name)
        .orElseThrow(() -> new IllegalStateException(name + " not found"));
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
