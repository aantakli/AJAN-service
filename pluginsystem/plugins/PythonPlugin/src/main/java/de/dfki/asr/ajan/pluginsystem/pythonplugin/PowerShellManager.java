package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerShellManager {
  private static final Logger LOG = LoggerFactory.getLogger(PowerShellManager.class);
  private final BlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();
  private Process process;
  private BufferedWriter writer;
  private BufferedReader reader;
  private Thread outputThread;
  private boolean running = false;

  private String path;

  public void setPath(String path) {
    this.path = path;
    if (running && path != null && !path.isEmpty()) {
      executeCommand("cd '" + path.replace("'", "''") + "'");
      executeCommand(".\\venv\\Scripts\\Activate.ps1\n");
    }
  }

  public synchronized void start() throws IOException {
    if (running) return;

    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoExit", "-Command", "-");
    pb.redirectErrorStream(true);
    process = pb.start();

    writer =
        new BufferedWriter(
            new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
    reader =
        new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

    running = true;
    outputThread = new Thread(this::readOutput);
    outputThread.setDaemon(true);
    outputThread.setName("PowerShell-Output-Reader");
    outputThread.start();

    LOG.info("PowerShell process started.");
  }

  private void readOutput() {
    try {
      String line;
      while (running && (line = reader.readLine()) != null) {
        LOG.debug("PowerShell Output: {}", line);
        outputQueue.offer(line);
      }
    } catch (IOException e) {
      if (running) {
        LOG.error("Error reading PowerShell output", e);
      }
    }
  }

  public synchronized void executeCommand(String command) {
    if (!running) {
      LOG.warn("PowerShell not running. Command ignored: {}", command);
      return;
    }
    try {
      writer.write(command);
      writer.newLine();
      writer.flush();
      LOG.info("Sent command to PowerShell: {}", command);
    } catch (IOException e) {
      LOG.error("Failed to send command to PowerShell", e);
    }
  }

  public synchronized void stop() {
    running = false;
    if (process != null) {
      process.destroy();
      try {
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
          process.destroyForcibly();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    if (outputThread != null) {
      outputThread.interrupt();
    }
    LOG.info("PowerShell process stopped.");
  }
}
