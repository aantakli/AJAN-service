package spike;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

public class PluginLoadCheck {

  public static void main(String[] args) throws IOException {
    Path pluginsDir = Files.createTempDirectory("pf4j-spike-plugins");

    Path fatJar =
        Path.of("..", "..", "pluginsystem", "deployments", "StandardBTNodes-0.1-all.jar")
            .toAbsolutePath()
            .normalize();

    if (!Files.exists(fatJar)) {
      // exec-maven-plugin may run with the reactor root (not the spike module dir) as
      // user.dir depending on how it was invoked; fall back to a single-level-up path.
      Path fallback =
          Path.of("pluginsystem", "deployments", "StandardBTNodes-0.1-all.jar")
              .toAbsolutePath()
              .normalize();
      if (Files.exists(fallback)) {
        fatJar = fallback;
      } else {
        throw new IllegalStateException(
            "Fat JAR not found at: " + fatJar + " nor at: " + fallback
                + " (user.dir=" + System.getProperty("user.dir") + ")");
      }
    }

    Path target = pluginsDir.resolve("StandardBTNodes-0.1.jar");
    Files.copy(fatJar, target, StandardCopyOption.REPLACE_EXISTING);

    System.setProperty("pf4j.mode", "deployment");

    PluginManager manager = new DefaultPluginManager(pluginsDir);
    manager.loadPlugins();

    boolean anyResolved = false;
    for (PluginWrapper wrapper : manager.getPlugins()) {
      System.out.println(
          "RESOLVED: " + wrapper.getPluginId() + " state=" + wrapper.getPluginState());
      anyResolved = true;
    }

    if (!anyResolved) {
      throw new IllegalStateException("No plugins were resolved from " + pluginsDir);
    }
  }
}
