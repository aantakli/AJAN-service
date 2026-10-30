package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

/** Java side of the OPCUA nodes, without Python. */
public class OPCUAMainScriptTest {

  @Test
  public void methodArgv() {
    assertEquals(
        OPCUAMainScript.methodArgv("opc.tcp://h:4840", "enable_asset", "--enable", "true"),
        List.of("--url", "opc.tcp://h:4840", "--call", "enable_asset", "--enable", "true"));
    assertEquals(
        OPCUAMainScript.methodArgv(null, "select_joint", "--joint-id", null),
        List.of("--url", "", "--call", "select_joint", "--joint-id", ""));
  }

  @Test
  public void onlySuccessfulResultsSucceed() {
    assertEquals(
        OPCUAMainScript.statusOf(PythonExecutionResult.success("ok", Map.of(), 1)),
        Status.SUCCEEDED);
    assertEquals(
        OPCUAMainScript.statusOf(PythonExecutionResult.failed("script exited with code 1", null, 1)),
        Status.FAILED);
    assertEquals(OPCUAMainScript.statusOf(null), Status.FAILED);
  }

  @Test
  public void callMethodPassesArgv() {
    FakeService service = new FakeService();
    service.next = PythonExecutionResult.failed("script exited with code 1", null, 1);
    List<String> argv = OPCUAMainScript.methodArgv("opc.tcp://h:1", "enable_asset", "--enable", "false");
    PythonExecutionResult r = OPCUAMainScript.callMethod(service, Path.of("call.py"), argv);
    assertEquals(service.lastInputs.get("call_argv"), argv);
    assertFalse(service.lastInputs.containsKey("argv"), "no sys.argv (and no argv lock)");
    assertEquals(OPCUAMainScript.statusOf(r), Status.FAILED);
  }

  @Test
  public void normalizeEndpoint() {
    assertEquals(
        OPCUAMainScript.normalizeEndpoint(" OPC.TCP://My-Host.Example:4840// "),
        "opc.tcp://my-host.example:4840");
    assertEquals(
        OPCUAMainScript.normalizeEndpoint("opc.tcp://User:PW@HOST:4840/Path/X/"),
        "opc.tcp://User:PW@host:4840/Path/X");
    assertEquals(OPCUAMainScript.normalizeEndpoint(null), "");
    assertEquals(OPCUAMainScript.normalizeEndpoint("  "), "");
  }

  @Test(timeOut = 30_000)
  public void equivalentEndpointsShareOneEventClient() throws Exception {
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch running = new CountDownLatch(1);
    BlockingQueue<String> seen = new ArrayBlockingQueue<>(4);
    BlockingQueue<PythonExecutionResult> exits = new ArrayBlockingQueue<>(4);
    FakeService service =
        new FakeService() {
          @Override
          public PythonExecutionResult executeScriptFile(Path p, Map<String, Object> inputs) {
            seen.add(Thread.currentThread().getName() + "|" + inputs.get("url"));
            running.countDown();
            try {
              release.await();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            return PythonExecutionResult.success("ok", Map.of(), 0);
          }
        };
    try {
      assertTrue(
          OPCUAMainScript.startEventClient(
              service, Path.of("events.py"), "opc.tcp://Dedupe-Host:4840/", exits::add));
      assertTrue(running.await(10, TimeUnit.SECONDS));
      assertFalse(
          OPCUAMainScript.startEventClient(
              service, Path.of("events.py"), " opc.tcp://dedupe-host:4840", exits::add));
      assertTrue(OPCUAMainScript.isEventClientRunning("OPC.TCP://DEDUPE-HOST:4840"));
      assertEquals(
          seen.poll(10, TimeUnit.SECONDS),
          "opcua-events-opc.tcp://dedupe-host:4840|opc.tcp://dedupe-host:4840");
    } finally {
      release.countDown();
    }
    assertTrue(exits.poll(10, TimeUnit.SECONDS) != null);
  }

  @Test(timeOut = 30_000)
  public void atMostOneEventClientPerEndpointAndRestartAfterDeath() throws Exception {
    String endpoint = "opc.tcp://registry-test:4840";
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch running = new CountDownLatch(1);
    AtomicInteger calls = new AtomicInteger();
    BlockingQueue<String> threadNames = new ArrayBlockingQueue<>(4);
    BlockingQueue<PythonExecutionResult> exits = new ArrayBlockingQueue<>(4);
    FakeService service =
        new FakeService() {
          @Override
          public PythonExecutionResult executeScriptFile(Path p, Map<String, Object> inputs) {
            calls.incrementAndGet();
            threadNames.add(Thread.currentThread().getName() + "|" + inputs.get("url")
                + "|" + Thread.currentThread().isDaemon());
            running.countDown();
            try {
              release.await();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            return PythonExecutionResult.failed("script exited with code 1", null, 1);
          }
        };

    assertTrue(OPCUAMainScript.startEventClient(service, Path.of("events.py"), endpoint, exits::add));
    assertTrue(running.await(10, TimeUnit.SECONDS));
    assertFalse(
        OPCUAMainScript.startEventClient(service, Path.of("events.py"), endpoint, exits::add),
        "second Setup call while the client runs starts nothing");
    assertTrue(OPCUAMainScript.isEventClientRunning(endpoint));
    assertEquals(
        threadNames.poll(10, TimeUnit.SECONDS),
        "opcua-events-" + endpoint + "|" + endpoint + "|true");

    release.countDown(); // the client dies (e.g. connection lost)
    PythonExecutionResult exit = exits.poll(10, TimeUnit.SECONDS);
    assertFalse(exit.isSuccess());
    while (OPCUAMainScript.isEventClientRunning(endpoint)) {
      Thread.sleep(10);
    }

    assertTrue(
        OPCUAMainScript.startEventClient(service, Path.of("events.py"), endpoint, exits::add),
        "a later Setup call starts a new client");
    assertTrue(exits.poll(10, TimeUnit.SECONDS) != null);
    assertEquals(calls.get(), 2);
  }

  /** Records the inputs; returns {@link #next}. */
  static class FakeService implements PythonExecutionService {
    volatile Map<String, Object> lastInputs;
    volatile PythonExecutionResult next = PythonExecutionResult.success("ok", Map.of(), 0);

    @Override
    public PythonExecutionResult executeScript(String script, Map<String, Object> inputs) {
      throw new UnsupportedOperationException();
    }

    @Override
    public PythonExecutionResult executeScriptFile(Path scriptPath, Map<String, Object> inputs) {
      lastInputs = inputs;
      return next;
    }

    @Override
    public PythonExecutionResult executePowerShellCommand(String command) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Path getPythonEnvironmentPath() {
      return null;
    }

    @Override
    public boolean isPythonAvailable() {
      return true;
    }

    @Override
    public PythonExecutionResult installPythonPackage(String packageName) {
      throw new UnsupportedOperationException();
    }
  }
}
