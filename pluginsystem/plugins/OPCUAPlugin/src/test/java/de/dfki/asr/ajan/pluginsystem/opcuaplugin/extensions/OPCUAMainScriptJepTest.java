package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonEnvironment;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.PythonPlugin;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonExecutionServiceImpl;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * The real {@code scripts/opcua/main.py} through the embedded CPython against an unreachable
 * endpoint. Needs the env zip on the classpath (PythonPlugin's python-env/dist/); otherwise
 * skipped, or failed with {@code -Dajan.python.requireEnv=true}.
 */
public class OPCUAMainScriptJepTest {

  private static final String UNREACHABLE = "opc.tcp://127.0.0.1:1";

  private final PythonExecutionServiceImpl service = new PythonExecutionServiceImpl();

  @BeforeClass
  public void initPython() {
    boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
    String zip = windows ? "/win_env.zip" : "/nix_env.zip";
    boolean external =
        System.getProperty(PythonEnvironment.HOME_PROPERTY) != null
            || System.getenv(PythonEnvironment.HOME_ENV) != null;
    if (!external && PythonPlugin.class.getResource(zip) == null) {
      String msg = zip.substring(1) + " not embedded (PythonPlugin python-env/dist/ is empty)";
      if (Boolean.getBoolean("ajan.python.requireEnv")) {
        throw new AssertionError(msg + " but -Dajan.python.requireEnv=true");
      }
      throw new SkipException(msg);
    }
    assertTrue(PythonPlugin.initialize(), "Python not available: " + PythonPlugin.unavailableReason());
  }

  private static Path script(String name) throws URISyntaxException {
    URL url = OPCUAMainScriptJepTest.class.getResource("/scripts/" + name);
    assertNotNull(url, name);
    return Path.of(url.toURI());
  }

  @Test(timeOut = 120_000)
  public void enableAssetAgainstUnreachableEndpointFails() throws Exception {
    List<String> argv =
        OPCUAMainScript.methodArgv(UNREACHABLE, "enable_asset", "--enable", "true");
    PythonExecutionResult r =
        OPCUAMainScript.callMethod(service, script(OPCUAMainScript.MAIN), argv);
    assertEquals(OPCUAMainScript.statusOf(r), Status.FAILED, String.valueOf(r.getOutput()));
    assertTrue(r.getError().contains("code 1"), r.getError());
  }

  @Test(timeOut = 120_000)
  public void eventClientAgainstUnreachableEndpointEndsFailed() throws Exception {
    BlockingQueue<PythonExecutionResult> exits = new ArrayBlockingQueue<>(2);
    assertTrue(
        OPCUAMainScript.startEventClient(
            service, script(OPCUAMainScript.EVENTS), UNREACHABLE, exits::add));
    PythonExecutionResult exit = exits.poll(100, TimeUnit.SECONDS);
    assertNotNull(exit, "event client did not end");
    assertFalse(exit.isSuccess());
    assertTrue(exit.getError().contains("code 1"), exit.getError());
    while (OPCUAMainScript.isEventClientRunning(UNREACHABLE)) {
      Thread.sleep(10);
    }
  }
}
