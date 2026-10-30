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

  /**
   * Script files do not need rdflib (I1): with rdflib blocked by a meta-path finder, a fresh
   * AJANlib import and executeScriptFile still work; legacy leaf globals work too.
   */
  @Test
  public void scriptFilesWorkWithoutRdflib() throws IOException {
    Path dir = Files.createTempDirectory("ajan-script-nordflib");
    Path main =
        script(
            dir,
            "no_rdflib.py",
            "try:",
            "    import rdflib",
            "    blocked = 'rdflib importable'",
            "except ImportError:",
            "    blocked = 'blocked'",
            "import AJANlib",
            "result = '|'.join([blocked, AJANlib.__name__, repr(AJANlib.NodeResult("
                + "AJANlib.Status.SUCCEEDED, 'l').rdf_output), *AJANlib.execute_leaf_script("
                + "\"rdf_output = '<a:s> <a:p> <a:o> .'\", '')[:3]])");
    String before = pythonState();
    PythonExecutionResult block =
        service.executeScript(
            String.join(
                "\n",
                "import sys",
                "class _AjanBlockRdflib:",
                "    def find_spec(self, name, path=None, target=None):",
                "        if name == 'rdflib' or name.startswith('rdflib.'):",
                "            raise ImportError('rdflib blocked by test')",
                "        return None",
                "sys._ajan_test_saved = {k: v for k, v in sys.modules.items()",
                "    if k in ('rdflib', 'AJANlib') or k.startswith('rdflib.')}",
                "for k in sys._ajan_test_saved:",
                "    del sys.modules[k]",
                "sys._ajan_test_blocker = _AjanBlockRdflib()",
                "sys.meta_path.insert(0, sys._ajan_test_blocker)",
                ""),
            null);
    assertTrue(block.isSuccess(), block.getError());
    PythonExecutionResult r;
    try {
      r = service.executeScriptFile(main, null);
    } finally {
      PythonExecutionResult restore =
          service.executeScript(
              String.join(
                  "\n",
                  "import sys",
                  "sys.meta_path.remove(sys._ajan_test_blocker)",
                  "sys.modules.pop('AJANlib', None)",
                  "sys.modules.update(sys._ajan_test_saved)",
                  "del sys._ajan_test_blocker, sys._ajan_test_saved",
                  ""),
              null);
      assertTrue(restore.isSuccess(), restore.getError());
    }
    assertTrue(r.isSuccess(), r.getError());
    String[] parts = r.getResults().get("result").toString().split("\\|");
    assertEquals(parts[0], "blocked", "the test really blocks rdflib");
    assertEquals(parts[1], "AJANlib");
    assertEquals(parts[2], "None", "NodeResult builds no Graph by default");
    assertEquals(parts[3], "SUCCEEDED");
    assertEquals(parts[5], "<a:s> <a:p> <a:o> .", "legacy Turtle text needs no rdflib");
    assertEquals(pythonState(), before);
  }

  /** The stdout proxy delegates the rest of the TextIO API (M1). */
  @Test
  public void stdoutProxyDelegatesTextIoApi() {
    PythonExecutionResult r =
        service.executeScript(
            String.join(
                "\n",
                "import io, sys, AJANlib",
                "raw = io.BytesIO()",
                "p = AJANlib._ThreadLocalStdout(io.TextIOWrapper(raw, encoding='ascii'))",
                "p.reconfigure(encoding='utf-8', line_buffering=True)",
                "p.write('ä')",
                "p.flush()",
                "p.buffer.write(b'|bytes')",
                "p.buffer.flush()",
                "p.begin()",
                "p.write('captured')",
                "captured = p.getvalue()",
                "text = p.end()",
                "result = '|'.join([str(getattr(sys.stdout, '_ajan_stdout_proxy', False)),"
                    + " p.encoding, str(p.line_buffering), str(p.write_through),"
                    + " raw.getvalue().decode('utf-8'), captured, text])",
                ""),
            null);
    assertTrue(r.isSuccess(), r.getError());
    assertEquals(
        r.getResults().get("result").toString(),
        "True|utf-8|True|False|ä|bytes|captured|captured");
  }

  /**
   * A starting script's directory goes to the front of sys.path even when a running script from
   * the same directory already references it (M5).
   */
  @Test
  public void scriptDirMovesToTheFrontOnEveryAcquire() {
    PythonExecutionResult r =
        service.executeScript(
            String.join(
                "\n",
                "import sys, AJANlib",
                "a, b = '/ajan-m5/a', '/ajan-m5/b'",
                "before = list(sys.path)",
                "AJANlib._acquire_script_dir(b)",
                "AJANlib._acquire_script_dir(a)",
                "first = sys.path[0]",
                "AJANlib._acquire_script_dir(b)",
                "second, count_b = sys.path[0], sys.path.count(b)",
                "AJANlib._release_script_dir(b)",
                "still = b in sys.path",
                "AJANlib._release_script_dir(a)",
                "AJANlib._release_script_dir(b)",
                "result = '|'.join([first, second, str(count_b), str(still),"
                    + " str(sys.path == before)])",
                ""),
            null);
    assertTrue(r.isSuccess(), r.getError());
    assertEquals(
        r.getResults().get("result").toString(), "/ajan-m5/a|/ajan-m5/b|1|True|True");
  }

  @Test
  public void missingFileFails() {
    PythonExecutionResult r =
        service.executeScriptFile(Path.of("does-not-exist-ajan", "main.py"), null);
    assertFalse(r.isSuccess());
    assertTrue(r.getError().contains("not found"), r.getError());
  }
}
