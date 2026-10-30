package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonExecutionServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Runs the real embedded CPython through JEP. Needs the env zip for this OS on the classpath
 * (python-env/dist/ at build time); otherwise skipped, or failed with
 * {@code -Dajan.python.requireEnv=true}.
 */
public class PythonJepTest {

  private final PythonExecutionServiceImpl service = new PythonExecutionServiceImpl();

  @BeforeClass
  public void initPython() {
    String zip = PythonEnvironment.isWindows() ? "/win_env.zip" : "/nix_env.zip";
    boolean external =
        System.getProperty(PythonEnvironment.HOME_PROPERTY) != null
            || System.getenv(PythonEnvironment.HOME_ENV) != null;
    if (!external && PythonJepTest.class.getResource(zip) == null) {
      String msg = zip.substring(1) + " not embedded (python-env/dist/ is empty)";
      if (Boolean.getBoolean("ajan.python.requireEnv")) {
        throw new AssertionError(msg + " but -Dajan.python.requireEnv=true");
      }
      throw new SkipException(msg);
    }
    assertTrue(PythonPlugin.initialize(), "Python not available: " + PythonPlugin.unavailableReason());
  }

  private PythonExecutionResult run(String script, Map<String, Object> inputs) {
    PythonExecutionResult r = service.executeScript(script, inputs);
    assertTrue(r.isSuccess(), "script failed: " + r.getError());
    return r;
  }

  @Test
  public void bundledPackagesAndAjanLibImport() {
    PythonExecutionResult r =
        run(
            "import sys, rdflib, asyncua, orjson, AJANlib\n"
                + "result = '|'.join([sys.version.split()[0], orjson.dumps({'a': 1}).decode(),"
                + " AJANlib.__file__, rdflib.Graph().__class__.__name__])\n",
            null);
    String[] parts = r.getResults().get("result").toString().split("\\|");
    assertTrue(parts[0].startsWith("3.13."), parts[0]);
    assertEquals(parts[1], "{\"a\":1}", "orjson (native extension) works");
    assertTrue(parts[2].replace('\\', '/').contains("/ajan-lib/AJANlib.py"), parts[2]);
    assertEquals(parts[3], "Graph");
  }

  @Test
  public void interpreterIgnoresHostPythonEnvironment() {
    PythonExecutionResult r =
        run(
            "import sys\nresult = str(sys.flags.ignore_environment) + '|' + sys.prefix\n", null);
    String[] parts = r.getResults().get("result").toString().split("\\|");
    assertEquals(parts[0], "1");
    assertEquals(
        java.nio.file.Paths.get(parts[1]),
        PythonPlugin.environment().orElseThrow().home());
  }

  @Test
  public void failedDlopenReportsDlerror() {
    if (PythonEnvironment.isWindows()) {
      throw new SkipException("dlopen is POSIX only");
    }
    IllegalStateException e =
        org.testng.Assert.expectThrows(
            IllegalStateException.class,
            () -> {
              try {
                PythonPlugin.dlopenGlobal("/nonexistent/libajan-missing.so");
              } catch (IllegalStateException ise) {
                throw ise;
              } catch (Throwable t) {
                throw new AssertionError(t);
              }
            });
    assertTrue(e.getMessage().contains("cannot open shared object file"), e.getMessage());
  }

  @Test
  public void pythonErrorsBecomeFailedResults() {
    PythonExecutionResult r = service.executeScript("raise ValueError('boom')", null);
    assertFalse(r.isSuccess());
    assertTrue(r.getError().contains("boom"), r.getError());
  }

  @Test
  public void concurrentScriptsRunInParallelWithSeparateGlobals() throws Exception {
    // Both scripts wait on a shared latch inside Python: this only completes if both run at the
    // same time (no global lock in executeScript) and JEP releases the GIL in Java calls.
    CountDownLatch bothInside = new CountDownLatch(2);
    String script =
        "from java.util.concurrent import TimeUnit\n"
            + "mine = value\n"
            + "latch.countDown()\n"
            + "together = getattr(latch, 'await')(10, TimeUnit.SECONDS)  # 'await' is a keyword\n"
            + "result = '%s|%s|%s' % (mine, value, together)\n";
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<PythonExecutionResult>> futures = new ArrayList<>();
      for (String v : new String[] {"A", "B"}) {
        futures.add(
            pool.submit(
                () -> service.executeScript(script, Map.of("value", v, "latch", bothInside))));
      }
      String[] expected = {"A|A|True", "B|B|True"};
      for (int i = 0; i < 2; i++) {
        PythonExecutionResult r = futures.get(i).get(30, TimeUnit.SECONDS);
        assertTrue(r.isSuccess(), r.getError());
        assertEquals(r.getResults().get("result"), expected[i]);
      }
    } finally {
      pool.shutdownNow();
    }
  }
}
