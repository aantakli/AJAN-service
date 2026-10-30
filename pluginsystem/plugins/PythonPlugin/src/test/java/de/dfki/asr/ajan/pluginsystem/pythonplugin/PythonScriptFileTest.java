package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonExecutionServiceImpl;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * {@code executeScriptFile} (D5) through the real embedded CPython. Gated on the env like {@link
 * PythonJepTest}.
 */
public class PythonScriptFileTest {

  private final PythonExecutionServiceImpl service = new PythonExecutionServiceImpl();

  @BeforeClass
  public void initPython() {
    String zip = PythonEnvironment.isWindows() ? "/win_env.zip" : "/nix_env.zip";
    boolean external =
        System.getProperty(PythonEnvironment.HOME_PROPERTY) != null
            || System.getenv(PythonEnvironment.HOME_ENV) != null;
    if (!external && PythonScriptFileTest.class.getResource(zip) == null) {
      String msg = zip.substring(1) + " not embedded (python-env/dist/ is empty)";
      if (Boolean.getBoolean("ajan.python.requireEnv")) {
        throw new AssertionError(msg + " but -Dajan.python.requireEnv=true");
      }
      throw new SkipException(msg);
    }
    assertTrue(PythonPlugin.initialize(), "Python not available: " + PythonPlugin.unavailableReason());
  }

  private static Path script(Path dir, String name, String... lines) throws IOException {
    Path file = dir.resolve(name);
    Files.writeString(file, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
    return file;
  }

  private String pythonState() {
    PythonExecutionResult r =
        service.executeScript(
            "import sys\nresult = repr(list(getattr(sys, 'argv', []))) + '|' + repr(sys.path)\n",
            null);
    assertTrue(r.isSuccess(), r.getError());
    return r.getResults().get("result").toString();
  }

  @Test
  public void runsAsMainWithSiblingImportsAndArgv() throws IOException {
    Path dir = Files.createTempDirectory("ajan-script-file");
    script(dir, "helper_ajan_t5.py", "VALUE = 'from-sibling'");
    Path main =
        script(
            dir,
            "main.py",
            "import sys, argparse",
            "import helper_ajan_t5",
            "p = argparse.ArgumentParser()",
            "p.add_argument('--url')",
            "p.add_argument('--call')",
            "a = p.parse_args()",
            "if __name__ == '__main__':",
            "    result = '|'.join([helper_ajan_t5.VALUE, a.url, a.call, __file__,"
                + " sys.argv[0], greeting])");
    String before = pythonState();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("argv", List.of("--url", "opc.tcp://h:1", "--call", "enable_asset"));
    inputs.put("greeting", "hi");
    PythonExecutionResult r = service.executeScriptFile(main, inputs);

    assertTrue(r.isSuccess(), r.getError());
    String[] parts = r.getResults().get("result").toString().split("\\|");
    assertEquals(parts[0], "from-sibling");
    assertEquals(parts[1], "opc.tcp://h:1");
    assertEquals(parts[2], "enable_asset");
    assertEquals(Path.of(parts[3]), main.toAbsolutePath());
    assertEquals(Path.of(parts[4]), main.toAbsolutePath());
    assertEquals(parts[5], "hi", "inputs are globals");
    assertEquals(pythonState(), before, "sys.argv and sys.path are restored");
  }

  @Test
  public void exitCodesMapToTheResult() throws IOException {
    Path dir = Files.createTempDirectory("ajan-script-exit");
    String before = pythonState();

    PythonExecutionResult three =
        service.executeScriptFile(
            script(dir, "three.py", "import sys", "sys.exit(3)"), Map.of("argv", List.of("-x")));
    assertFalse(three.isSuccess());
    assertTrue(three.getError().contains("code 3"), three.getError());

    PythonExecutionResult zero =
        service.executeScriptFile(script(dir, "zero.py", "import sys", "sys.exit(0)"), null);
    assertTrue(zero.isSuccess(), zero.getError());

    PythonExecutionResult none =
        service.executeScriptFile(script(dir, "none.py", "import sys", "sys.exit()"), null);
    assertTrue(none.isSuccess(), none.getError());

    PythonExecutionResult raised =
        service.executeScriptFile(script(dir, "raise.py", "raise ValueError('kaputt')"), null);
    assertFalse(raised.isSuccess());
    assertTrue(raised.getError().contains("kaputt"), raised.getError());

    assertEquals(pythonState(), before, "sys.argv and sys.path are restored after failures");
  }

  @Test
  public void missingFileFails() {
    PythonExecutionResult r =
        service.executeScriptFile(Path.of("does-not-exist-ajan", "main.py"), null);
    assertFalse(r.isSuccess());
    assertTrue(r.getError().contains("not found"), r.getError());
  }
}
