package de.dfki.asr.ajan.pluginsystem.loadcheck;

import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.EndpointExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Characterizes plugin loading in pf4j deployment mode (fat JARs in pluginsystem/deployments).
 * Values are OBSERVED, not desired. All ten plugins reach STARTED here, including MOSIM and
 * MQTT; the per-plugin extension counts are pinned as measured with pf4j 3.16.
 */
public class DeploymentModeLoadTest {

  private PluginManager pm;

  @BeforeClass
  public void load() throws Exception {
    Assert.assertEquals(System.getProperty("pf4j.mode"), "deployment");
    Path deployments = Paths.get(System.getProperty("pf4j.pluginsDir"));
    Assert.assertTrue(
        Files.isDirectory(deployments),
        "plugin deployments directory missing (run 'mvn install' for the plugins first): " + deployments);
    AJANPluginLoader loader = new AJANPluginLoader();
    loader.init();
    pm = loader.getPLUGIN_MANAGER();
  }

  private Map<String, PluginState> measuredStates() {
    Map<String, PluginState> m = new TreeMap<>();
    for (PluginWrapper w : pm.getPlugins()) {
      m.put(w.getPluginId(), w.getPluginState());
    }
    return m;
  }

  private Map<String, String> measuredCounts() {
    Map<String, String> m = new TreeMap<>();
    for (PluginWrapper w : pm.getPlugins()) {
      int node = pm.getExtensions(NodeExtension.class, w.getPluginId()).size();
      int ep = pm.getExtensions(EndpointExtension.class, w.getPluginId()).size();
      m.put(w.getPluginId(), node + "," + ep);
    }
    return m;
  }

  @Test
  public void everyDeploymentJarReachesObservedState() {
    Map<String, PluginState> expected = new TreeMap<>();
    // All ten plugins reach STARTED. MQTTPlugin used to be absent (empty Plugin-Id in its
    // manifest, because its POM never set the plugin.* properties); fixed in its POM.
    for (String id :
        new String[] {
          "ASPPlugin",
          "MOSIMPlugin",
          "MQTTPlugin",
          "MappingPlugin",
          "OPCUAPlugin",
          "PythonPlugin",
          "RMLMappingPlugin",
          "STRIPSPlugin",
          "ScriptExecutorPlugin",
          "StandardBTNodes"
        }) {
      expected.put(id, PluginState.STARTED);
    }
    Assert.assertEquals(measuredStates(), expected);
  }

  /**
   * PythonPlugin starts with or without an embedded Python environment; without one, scripts fail
   * with a reason instead of crashing. With {@code -Dajan.python.requireEnv=true} the embedded
   * environment must work inside the pf4j plugin class loader.
   */
  @Test
  public void pythonServiceRunsScriptsOrReportsWhyNot() {
    List<PythonExecutionService> services =
        pm.getExtensions(PythonExecutionService.class, "PythonPlugin");
    Assert.assertEquals(services.size(), 1);
    PythonExecutionService python = services.get(0);
    PythonExecutionResult r =
        python.executeScript("import orjson, AJANlib\nresult = orjson.dumps([1]).decode()", null);
    if (python.isPythonAvailable()) {
      Assert.assertTrue(r.isSuccess(), r.getError());
      Assert.assertEquals(r.getResults().get("result"), "[1]");
    } else {
      Assert.assertFalse(Boolean.getBoolean("ajan.python.requireEnv"), r.getError());
      Assert.assertFalse(r.isSuccess());
      Assert.assertTrue(r.getError().startsWith("Python not available: "), r.getError());
    }
  }

  @Test
  public void extensionCountsPerPluginMatchObserved() {
    Map<String, String> expected = new TreeMap<>();
    // OBSERVED, format "nodeExt,endpointExt"
    expected.put("ASPPlugin", "15,0");
    expected.put("MOSIMPlugin", "14,1");
    expected.put("MQTTPlugin", "8,0");
    expected.put("MappingPlugin", "5,0");
    expected.put("OPCUAPlugin", "4,0");
    expected.put("PythonPlugin", "1,0");
    expected.put("RMLMappingPlugin", "1,0");
    expected.put("STRIPSPlugin", "2,0");
    expected.put("ScriptExecutorPlugin", "1,0");
    expected.put("StandardBTNodes", "13,0");
    Assert.assertEquals(measuredCounts(), expected);
  }
}
