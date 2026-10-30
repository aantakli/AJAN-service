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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
        OPCUAMainScript.callMethod(service, script(OPCUAMainScript.CALL), argv);
    assertEquals(OPCUAMainScript.statusOf(r), Status.FAILED, String.valueOf(r.getOutput()));
    // the reason reaches the node status / agent log, not only an exit code
    assertTrue(
        r.getError().contains("OPC UA method enable_asset failed against " + UNREACHABLE + ": "),
        r.getError());
  }

  /**
   * A method call runs main.main on the calling (pooled) thread: afterwards that thread has no
   * closed event loop set, and a failing method status reaches the result as the reason.
   */
  @Test(timeOut = 60_000)
  public void methodCallLeavesNoClosedEventLoopAndReportsTheReason() throws Exception {
    String opcuaDir = script(OPCUAMainScript.MAIN).getParent().toString();
    Path dir = Files.createTempDirectory("ajan-opcua-loop");
    Path fakeCall = dir.resolve("fake_call.py");
    Files.writeString(
        fakeCall,
        String.join(
            "\n",
            "import asyncio, sys",
            "sys.path.insert(0, " + pyString(opcuaDir) + ")",
            "try:",
            "    import main",
            "finally:",
            "    sys.path.remove(" + pyString(opcuaDir) + ")",
            "",
            "class FakeMethods:",
            "    async def enable_asset(self, object_nodeid, method_nodeid, enable):",
            "        return {'status': method_status, 'status_message': 'boom'}",
            "",
            "class FakeClient:",
            "    def __init__(self, url):",
            "        self.methods = FakeMethods()",
            "    async def connect(self): pass",
            "    async def cleanup(self): pass",
            "",
            "real_client = main.OPCUAClient",
            "main.OPCUAClient = FakeClient",
            "try:",
            "    try:",
            "        main.main(['--url', 'opc.tcp://fake:4840', '--call', 'enable_asset',"
                + " '--enable', 'true'])",
            "        outcome = 'ok'",
            "    except SystemExit as e:",
            "        outcome = str(e.code)",
            "finally:",
            "    main.OPCUAClient = real_client",
            "try:",
            "    loop_state = 'closed' if asyncio.get_event_loop().is_closed() else 'open'",
            "except RuntimeError:",
            "    loop_state = 'unset'",
            "async def ping():",
            "    return 42",
            "fresh = asyncio.new_event_loop()",
            "try:",
            "    value = fresh.run_until_complete(ping())",
            "finally:",
            "    fresh.close()",
            "result = '|'.join([outcome, loop_state, str(value)])",
            ""),
        StandardCharsets.UTF_8);

    PythonExecutionResult ok = service.executeScriptFile(fakeCall, Map.of("method_status", 0));
    assertTrue(ok.isSuccess(), ok.getError());
    String[] parts = ok.getResults().get("result").toString().split("\\|");
    assertEquals(parts[0], "ok");
    assertTrue(!parts[1].equals("closed"), "closed event loop left on the thread");
    assertEquals(parts[2], "42");

    PythonExecutionResult failed = service.executeScriptFile(fakeCall, Map.of("method_status", 3));
    assertTrue(failed.isSuccess(), failed.getError());
    parts = failed.getResults().get("result").toString().split("\\|");
    assertTrue(
        parts[0].contains("enable_asset returned status 3: boom")
            && parts[0].contains("opc.tcp://fake:4840"),
        parts[0]);
    assertTrue(!parts[1].equals("closed"), "closed event loop left on the thread");
  }

  @Test
  public void mainPyStillTakesSysArgv() throws Exception {
    // standalone contract (python main.py ...): argparse --help exits 0
    PythonExecutionResult r =
        service.executeScriptFile(
            script(OPCUAMainScript.MAIN), Map.of("argv", List.of("--help")));
    assertTrue(r.isSuccess(), r.getError());
  }

  /**
   * Lost connection: an event client whose connection check fails ends with exit code 1, its
   * registry entry is cleared, and the next Setup call starts a new client. Runs main.main with a
   * fake OPCUAClient (connect/subscribe succeed, the 2nd connection check raises).
   */
  @Test(timeOut = 60_000)
  public void lostConnectionEndsEventClientAndAllowsRestart() throws Exception {
    String opcuaDir = script(OPCUAMainScript.MAIN).getParent().toString();
    Path dir = Files.createTempDirectory("ajan-opcua-lost");
    Path fakeEvents = dir.resolve("fake_events.py");
    Files.writeString(
        fakeEvents,
        String.join(
            "\n",
            "import sys",
            "sys.path.insert(0, " + pyString(opcuaDir) + ")",
            "try:",
            "    import main",
            "finally:",
            "    sys.path.remove(" + pyString(opcuaDir) + ")",
            "",
            "class FakeClient:",
            "    def __init__(self, url):",
            "        self.checks = 0",
            "    async def clear_old_logs(self): pass",
            "    async def connect(self): pass",
            "    async def subscribe_to_events(self): pass",
            "    async def cleanup(self): pass",
            "    async def check_connection(self):",
            "        self.checks += 1",
            "        if self.checks >= 2:",
            "            raise ConnectionError('connection lost (simulated)')",
            "",
            "real_client, real_interval = main.OPCUAClient, main.CHECK_INTERVAL",
            "main.OPCUAClient, main.CHECK_INTERVAL = FakeClient, 0.05",
            "try:",
            "    code = main.main(['--url', url])",
            "finally:",
            "    main.OPCUAClient, main.CHECK_INTERVAL = real_client, real_interval",
            "sys.exit(code)",
            ""),
        StandardCharsets.UTF_8);

    String endpoint = "opc.tcp://lost-connection:4840";
    BlockingQueue<PythonExecutionResult> exits = new ArrayBlockingQueue<>(4);
    for (int round = 1; round <= 2; round++) {
      assertTrue(
          OPCUAMainScript.startEventClient(service, fakeEvents, endpoint, exits::add),
          "round " + round + ": a new client starts");
      PythonExecutionResult exit = exits.poll(30, TimeUnit.SECONDS);
      assertNotNull(exit, "round " + round + ": event client did not end");
      assertFalse(exit.isSuccess());
      assertTrue(exit.getError().contains("connection lost (simulated)"), exit.getError());
      while (OPCUAMainScript.isEventClientRunning(endpoint)) {
        Thread.sleep(10);
      }
    }
  }

  private static String pyString(String s) {
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
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
    assertTrue(
        exit.getError().contains("OPC UA event client failed against " + UNREACHABLE),
        exit.getError());
    while (OPCUAMainScript.isEventClientRunning(UNREACHABLE)) {
      Thread.sleep(10);
    }
  }
}
