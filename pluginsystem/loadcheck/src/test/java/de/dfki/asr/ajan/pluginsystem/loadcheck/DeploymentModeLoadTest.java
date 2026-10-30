package de.dfki.asr.ajan.pluginsystem.loadcheck;

import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.EndpointExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
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
 * Values are OBSERVED, not desired. Plugins whose extensions need host classes from the
 * executionservice (MOSIM, MQTT) may fail here because the test classpath lacks them; that is an
 * artifact of the test environment and is pinned as the observed value anyway.
 */
public class DeploymentModeLoadTest {

  private PluginManager pm;

  @BeforeClass
  public void load() throws Exception {
    Assert.assertEquals(System.getProperty("pf4j.mode"), "deployment");
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
    // OBSERVED: all nine loadable plugins reach STARTED.
    // OBSERVED: MQTTPlugin-0.1-all.jar has an empty Plugin-Id/Plugin-Class manifest (W17); pf4j
    // does not register any plugin for it, so it is absent here (not repaired in this task).
    for (String id :
        new String[] {
          "ASPPlugin",
          "MOSIMPlugin",
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
    System.out.println("MEASURED_STATES=" + measuredStates());
    Assert.assertEquals(measuredStates(), expected);
  }

  @Test
  public void extensionCountsPerPluginMatchObserved() {
    Map<String, String> expected = new TreeMap<>();
    // OBSERVED, format "nodeExt,endpointExt" (MQTTPlugin absent, see above)
    expected.put("ASPPlugin", "15,0");
    expected.put("MOSIMPlugin", "14,1");
    expected.put("MappingPlugin", "5,0");
    expected.put("OPCUAPlugin", "4,0");
    expected.put("PythonPlugin", "1,0");
    expected.put("RMLMappingPlugin", "1,0");
    expected.put("STRIPSPlugin", "2,0");
    expected.put("ScriptExecutorPlugin", "1,0");
    expected.put("StandardBTNodes", "13,0");
    System.out.println("MEASURED_COUNTS=" + measuredCounts());
    Assert.assertEquals(measuredCounts(), expected);
  }
}
