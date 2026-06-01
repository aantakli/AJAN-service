package de.dfki.asr.ajan.pluginsystem.pythonplugin.hotreload;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptWatcher implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptWatcher.class);
  private final WatchService watchService;
  private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
  private boolean running = true;

  public ScriptWatcher() throws IOException {
    this.watchService = FileSystems.getDefault().newWatchService();
  }

  public void watchDirectory(Path dir) throws IOException {
    WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    keys.put(key, dir);
    LOG.info("Watching directory for script changes: {}", dir);
  }

  @Override
  public void run() {
    try {
      while (running) {
        WatchKey key = watchService.take();
        Path dir = keys.get(key);
        if (dir == null) continue;

        for (WatchEvent<?> event : key.pollEvents()) {
          if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            Path filename = (Path) event.context();
            Path child = dir.resolve(filename);
            if (child.toString().endsWith(".py")) {
              LOG.info("Script modified: {}. Reloading...", child);
              // Reload logic would go here
            }
          }
        }
        if (!key.reset()) {
          keys.remove(key);
          if (keys.isEmpty()) break;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      LOG.error("Error in ScriptWatcher", e);
    }
  }

  public void stop() {
    running = false;
    try {
      watchService.close();
    } catch (IOException e) {
      LOG.error("Failed to close WatchService", e);
    }
  }
}
