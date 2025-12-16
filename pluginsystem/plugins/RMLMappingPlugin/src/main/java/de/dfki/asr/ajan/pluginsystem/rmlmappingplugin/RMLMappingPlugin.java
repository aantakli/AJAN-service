package de.dfki.asr.ajan.pluginsystem.rmlmappingplugin;

import java.lang.reflect.Field;
import java.util.Map;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMLMappingPlugin extends Plugin {

  private static final Logger LOG = LoggerFactory.getLogger(RMLMappingPlugin.class);

  public RMLMappingPlugin(PluginWrapper wrapper)  {
    super(wrapper);
    System.out.println("RMLMappingPlugin.constructor()");
  }


  // Helper method to update environment variables (for current process)
  private static void setEnv(String name, String value) {
    try {
      Map<String, String> env = System.getenv();
      Class<?> cl = env.getClass();
      Field field = cl.getDeclaredField("m");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> writableEnv = (Map<String, String>) field.get(env);
      writableEnv.put(name, value);
    } catch (Exception e) {
      // fallback for Windows
      try {
        Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
        Field theEnvironmentField = pe.getDeclaredField("theEnvironment");
        theEnvironmentField.setAccessible(true);
        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
        env.put(name, value);

        Field cienvField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
        cienvField.setAccessible(true);
        Map<String, String> cienv = (Map<String, String>) cienvField.get(null);
        cienv.put(name, value);
      } catch (Exception ignored) {
      }
    }
  }


  @Override
  public void start() {
    System.out.println("RMLMappingPlugin.start()");
    if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
      LOG.debug("RMLMappingPlugin");
    }
  }

  @Override
  public void stop() {
    System.out.println("RMLMappingPlugin.stop()");
  }
}
