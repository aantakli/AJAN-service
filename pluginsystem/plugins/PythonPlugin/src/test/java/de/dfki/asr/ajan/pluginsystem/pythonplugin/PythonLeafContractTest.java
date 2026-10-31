package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonExecutionServiceImpl;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonLeafNode;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.extensions.PythonLeafNode.LeafResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * The PythonLeafNode script contract (D4) through the real embedded CPython. Gated on the env like
 * {@link PythonJepTest}.
 */
public class PythonLeafContractTest {

  /** The script of executionservice/use-case/Examples/Python_Example/behaviors.ttl (tabs). */
  private static final String PYTHON_EXAMPLE =
      "import AJANlib\n"
          + "from rdflib import Graph, URIRef, BNode, Literal\n"
          + "from rdflib.namespace import RDF, XSD\n"
          + "\n"
          + "class MyNode(AJANlib.LeafNode):\n"
          + "\tdef executeLeafNode(self, input: Graph):\n"
          + "\t\toutput = Graph()\n"
          + "\t\tagent = URIRef('http://www.ajan.de/ajan-ns#Agent')\n"
          + "\t\tlabel = URIRef('http://www.ajan.de/python-ns#label')\n"
          + "\t\t\n"
          + "\t\tfor agent in input.subjects(RDF.type, agent):\n"
          + "\t\t\toutput.add((agent, label, Literal('is an Python Agent', datatype=XSD.string)))\n"
          + "\t\t\n"
          + "\t\treturn AJANlib.NodeResult(status=AJANlib.Status.SUCCEEDED,"
          + " label='added statements!', rdf_output=output)\n"
          + "\n"
          + "\tdef getClassName(self):\n"
          + "\t\treturn 'MyNode'";

  private static final String AGENT_INPUT =
      "<http://localhost:8080/agents/a1> a <http://www.ajan.de/ajan-ns#Agent> .\n"
          + "<http://localhost:8080/agents/a1> <http://www.w3.org/2000/01/rdf-schema#label>"
          + " \"a1\" .\n";

  private final ValueFactory vf = SimpleValueFactory.getInstance();

  @BeforeClass
  public void initPython() {
    String zip = PythonEnvironment.isWindows() ? "/win_env.zip" : "/nix_env.zip";
    boolean external =
        System.getProperty(PythonEnvironment.HOME_PROPERTY) != null
            || System.getenv(PythonEnvironment.HOME_ENV) != null;
    if (!external && PythonLeafContractTest.class.getResource(zip) == null) {
      String msg = zip.substring(1) + " not embedded (python-env/dist/ is empty)";
      if (Boolean.getBoolean("ajan.python.requireEnv")) {
        throw new AssertionError(msg + " but -Dajan.python.requireEnv=true");
      }
      throw new SkipException(msg);
    }
    assertTrue(
        PythonPlugin.initialize(), "Python not available: " + PythonPlugin.unavailableReason());
  }

  @Test
  public void pythonExampleClassAddsLabelForEachAgent() throws Exception {
    LeafResult r = PythonLeafNode.runLeaf(PYTHON_EXAMPLE, AGENT_INPUT);
    assertEquals(r.status(), Status.SUCCEEDED, r.label());
    assertEquals(r.label(), "added statements!");
    Model out = PythonLeafNode.parseTurtle(r.turtle());
    assertEquals(out.size(), 1, r.turtle());
    Statement st = out.iterator().next();
    assertEquals(st.getSubject(), vf.createIRI("http://localhost:8080/agents/a1"));
    assertEquals(st.getPredicate(), vf.createIRI("http://www.ajan.de/python-ns#label"));
    assertEquals(((Literal) st.getObject()).getLabel(), "is an Python Agent");
  }

  @Test
  public void classWithEmptyInputAndEmptyOutputSucceedsWithoutTurtle() throws Exception {
    LeafResult r = PythonLeafNode.runLeaf(PYTHON_EXAMPLE, "");
    assertEquals(r.status(), Status.SUCCEEDED, r.label());
    assertEquals(r.turtle(), "");
    assertTrue(PythonLeafNode.parseTurtle(r.turtle()).isEmpty());
  }

  @Test
  public void stringStatusAndEditorDefaultEchoInput() throws Exception {
    // editor default of node_definitions.ttl: echoes the input graph
    String script =
        "import AJANlib\nimport rdflib\n\n\nclass MyNode(AJANlib.LeafNode):\n"
            + "\tdef executeLeafNode(self, input: rdflib.Graph):\n"
            + "\t\treturn AJANlib.NodeResult(status='running', label='Hallo World!',"
            + " rdf_output=input)\n\n"
            + "\tdef getClassName(self):\n\t\treturn 'MyNode'";
    LeafResult r = PythonLeafNode.runLeaf(script, AGENT_INPUT);
    assertEquals(r.status(), Status.RUNNING);
    assertEquals(r.statusName(), "RUNNING");
    assertEquals(PythonLeafNode.parseTurtle(r.turtle()).size(), 2);
  }

  @Test
  public void legacyGlobalsContract() throws Exception {
    LeafResult ok =
        PythonLeafNode.runLeaf(
            "assert 'ajan-ns#Agent' in input_rdf\n"
                + "rdf_output = '<urn:a> <urn:b> \"c\" .'\n",
            AGENT_INPUT);
    assertEquals(ok.status(), Status.SUCCEEDED, ok.label());
    assertEquals(PythonLeafNode.parseTurtle(ok.turtle()).size(), 1);

    LeafResult failed = PythonLeafNode.runLeaf("status = 'FAILED'\n", "");
    assertEquals(failed.status(), Status.FAILED);
    assertEquals(failed.statusName(), "FAILED");
    assertEquals(failed.turtle(), "");
  }

  @Test
  public void raisingScriptFailsWithTraceback() throws Exception {
    LeafResult r =
        PythonLeafNode.runLeaf(
            "import AJANlib\nclass N(AJANlib.LeafNode):\n"
                + "    def executeLeafNode(self, input):\n"
                + "        raise RuntimeError('kaputt')\n",
            "");
    assertEquals(r.status(), Status.FAILED);
    assertTrue(r.label().contains("Traceback"), r.label());
    assertTrue(r.label().contains("RuntimeError: kaputt"), r.label());
    assertTrue(r.label().contains("executeLeafNode"), r.label());

    LeafResult syntax = PythonLeafNode.runLeaf("def broken(:\n", "");
    assertEquals(syntax.status(), Status.FAILED);
    assertTrue(syntax.label().contains("SyntaxError"), syntax.label());
  }

  @Test
  public void twoSubclassesFail() throws Exception {
    LeafResult r =
        PythonLeafNode.runLeaf(
            "import AJANlib\n"
                + "class A(AJANlib.LeafNode): pass\n"
                + "class B(AJANlib.LeafNode): pass\n",
            "");
    assertEquals(r.status(), Status.FAILED);
    assertTrue(r.label().contains("ValueError"), r.label());
    assertTrue(r.label().contains("more than one"), r.label());
  }

  @Test
  public void stdoutIsCapturedPerThread() throws Exception {
    // Both leaves print, meet at a barrier (shared via the sys module, which all
    // SharedInterpreters share), and print again: the output interleaves in time.
    PythonExecutionResult setup =
        new PythonExecutionServiceImpl()
            .executeScript("import sys, threading\nsys._ajan_test_barrier = threading.Barrier(2)\n",
                null);
    assertTrue(setup.isSuccess(), setup.getError());
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<LeafResult>> futures = new ArrayList<>();
      for (String name : new String[] {"A", "B"}) {
        String script =
            "import sys, AJANlib\n"
                + "class N(AJANlib.LeafNode):\n"
                + "    def executeLeafNode(self, input):\n"
                + "        print('" + name + "-before')\n"
                + "        sys._ajan_test_barrier.wait(10)\n"
                + "        print('" + name + "-after')\n"
                + "        return AJANlib.NodeResult(AJANlib.Status.SUCCEEDED, '', None)\n";
        futures.add(pool.submit(() -> PythonLeafNode.runLeaf(script, "")));
      }
      String[] names = {"A", "B"};
      for (int i = 0; i < 2; i++) {
        LeafResult r = futures.get(i).get(30, TimeUnit.SECONDS);
        assertEquals(r.status(), Status.SUCCEEDED, r.label());
        assertEquals(r.stdout(), names[i] + "-before\n" + names[i] + "-after\n");
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void realStdoutSurvivesExceptions() throws Exception {
    LeafResult r = PythonLeafNode.runLeaf("print('lost?')\nraise ValueError('x')\n", "");
    assertEquals(r.status(), Status.FAILED);
    assertEquals(r.stdout(), "lost?\n");

    PythonExecutionResult check =
        new PythonExecutionServiceImpl()
            .executeScript(
                "import sys, AJANlib\n"
                    + "print('passthrough to the real stdout')\n"
                    + "proxy = sys.stdout\n"
                    + "result = '%s|%s|%s' % (type(proxy).__name__, proxy.capturing(),"
                    + " proxy._real is not None and not proxy._real.closed)\n",
                null);
    assertTrue(check.isSuccess(), check.getError());
    assertEquals(check.getResults().get("result"), "_ThreadLocalStdout|False|True");

    LeafResult again = PythonLeafNode.runLeaf("print('next')\n", "");
    assertEquals(again.stdout(), "next\n");
  }

  @Test
  public void realStdoutIsLineBuffered() {
    // Uncaptured prints (script files, other threads) go to the real stdout, which CPython
    // block-buffers when it is not a terminal (surefire pipe, Docker): they would not reach the
    // service log until 8 KiB had accumulated.
    PythonExecutionResult check =
        new PythonExecutionServiceImpl()
            .executeScript(
                "import sys, AJANlib\n"
                    + "real = sys.stdout._real\n"
                    + "result = '%s|%s' % (real.isatty(), real.line_buffering)\n",
                null);
    assertTrue(check.isSuccess(), check.getError());
    assertTrue(
        String.valueOf(check.getResults().get("result")).endsWith("|True"),
        "real stdout should be line-buffered, was (isatty|line_buffering): "
            + check.getResults().get("result"));
  }

  @Test
  public void statusMapping() {
    assertEquals(PythonLeafNode.toStatus("SUCCEEDED"), Status.SUCCEEDED);
    assertEquals(PythonLeafNode.toStatus("RUNNING"), Status.RUNNING);
    assertEquals(PythonLeafNode.toStatus("FAILED"), Status.FAILED);
    assertEquals(PythonLeafNode.toStatus("FRESH"), Status.FAILED);
    assertEquals(PythonLeafNode.toStatus(null), Status.FAILED);
    assertFalse(PythonLeafNode.toStatus("bogus") == Status.SUCCEEDED);
  }
}
