/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionService;
import de.dfki.asr.ajan.pluginsystem.pythonplugin.exception.PythonException;
import de.dfki.asr.ajan.pluginsystem.utils.ScriptExtractor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.pf4j.PluginWrapper;

/**
 * Runs {@code scripts/opcua/main.py} for the OPCUA nodes.
 *
 * <p>Both entry points pass the {@code main.py} arguments to {@code main.main(argv)} directly
 * instead of through {@code sys.argv}, because {@code executeScriptFile} serializes calls with
 * {@code argv} (process-global {@code sys.argv}) for their whole run:
 *
 * <ul>
 *   <li>Method calls run {@code call.py} with {@code call_argv} ({@code --url <u> --call <c> ...})
 *       and block until it exits; a non-zero exit code (connection or method error) is FAILED.
 *   <li>The event client ({@code main.py --url <u>}) runs through {@code events.py} on a daemon
 *       thread {@code opcua-events-<endpoint>}, at most one per (normalized) endpoint. It ends with
 *       exit code 1 when the connection is lost; then a later Setup call starts a new one.
 * </ul>
 */
public final class OPCUAMainScript {

  public static final String MAIN = "opcua/main.py";
  public static final String CALL = "opcua/call.py";
  public static final String EVENTS = "opcua/events.py";
  public static final String CALL_ARGV_INPUT = "call_argv";
  public static final String THREAD_PREFIX = "opcua-events-";

  /** Running event clients by endpoint; an entry is removed when its thread ends. */
  private static final Map<String, Thread> EVENT_CLIENTS = new ConcurrentHashMap<>();

  private OPCUAMainScript() {}

  public static PythonExecutionService service() throws PythonException {
    PythonExecutionService service = AJANPluginLoader.getInstance().getPythonExecutionService();
    if (service == null) {
      throw new PythonException("PythonExecutionService not available");
    }
    return service;
  }

  /** The extracted script {@code name} of the OPCUAPlugin. */
  public static Path script(String name) throws PythonException {
    PluginWrapper wrapper = AJANPluginLoader.getInstance().getPluginManager().getPlugin("OPCUAPlugin");
    Path path = wrapper == null ? null : ScriptExtractor.getScriptPath(wrapper.getPlugin(), name);
    if (path == null || !path.toFile().exists()) {
      throw new PythonException("Could not find " + name + " script in extracted resources");
    }
    return path;
  }

  /** {@code --url <url> --call <call> [<option> <value>]...}; options come in pairs. */
  public static List<String> methodArgv(String url, String call, String... options) {
    List<String> argv = new ArrayList<>();
    argv.add("--url");
    argv.add(url == null ? "" : url);
    argv.add("--call");
    argv.add(call);
    for (String option : options) {
      argv.add(option == null ? "" : option);
    }
    return argv;
  }

  /** Runs {@code call.py} (main.main(argv)) and waits for it to exit; no sys.argv lock. */
  public static PythonExecutionResult callMethod(
      PythonExecutionService service, Path callScript, List<String> argv) {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put(CALL_ARGV_INPUT, argv);
    return service.executeScriptFile(callScript, inputs);
  }

  /**
   * Registry key and URL of an endpoint: trimmed, without trailing {@code /}, scheme and host in
   * lower case (user info keeps its case). {@code null} becomes {@code ""}.
   */
  public static String normalizeEndpoint(String endpoint) {
    if (endpoint == null) {
      return "";
    }
    String url = endpoint.trim();
    while (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    int schemeEnd = url.indexOf("://");
    if (schemeEnd < 0) {
      return url;
    }
    int authorityStart = schemeEnd + 3;
    int authorityEnd = url.indexOf('/', authorityStart);
    if (authorityEnd < 0) {
      authorityEnd = url.length();
    }
    String authority = url.substring(authorityStart, authorityEnd);
    int at = authority.lastIndexOf('@');
    String hostPort =
        authority.substring(0, at + 1) + authority.substring(at + 1).toLowerCase(Locale.ROOT);
    return url.substring(0, schemeEnd).toLowerCase(Locale.ROOT)
        + "://"
        + hostPort
        + url.substring(authorityEnd);
  }

  /** SUCCEEDED only for a successful result (exit code 0). */
  public static Status statusOf(PythonExecutionResult result) {
    return result != null && result.isSuccess() ? Status.SUCCEEDED : Status.FAILED;
  }

  public static String describe(PythonExecutionResult result) {
    if (result == null) {
      return "no result";
    }
    return result.isSuccess() ? "main.py succeeded" : "main.py failed: " + result.getError();
  }

  /**
   * Starts the event client for {@code endpoint} on a daemon thread unless one is running.
   *
   * @param onExit called on the client thread with the result when the client ends
   * @return true if a new client thread was started, false if one is already running
   */
  public static boolean startEventClient(
      PythonExecutionService service,
      Path eventsScript,
      String endpoint,
      Consumer<PythonExecutionResult> onExit) {
    String key = normalizeEndpoint(endpoint);
    AtomicBoolean started = new AtomicBoolean();
    EVENT_CLIENTS.compute(
        key,
        (k, running) -> {
          if (running != null && running.isAlive()) {
            return running;
          }
          Thread thread =
              new Thread(() -> runEventClient(service, eventsScript, k, onExit), THREAD_PREFIX + k);
          thread.setDaemon(true);
          thread.start();
          started.set(true);
          return thread;
        });
    return started.get();
  }

  public static boolean isEventClientRunning(String endpoint) {
    Thread thread = EVENT_CLIENTS.get(normalizeEndpoint(endpoint));
    return thread != null && thread.isAlive();
  }

  private static void runEventClient(
      PythonExecutionService service,
      Path eventsScript,
      String endpoint,
      Consumer<PythonExecutionResult> onExit) {
    PythonExecutionResult result = null;
    try {
      Map<String, Object> inputs = new HashMap<>();
      inputs.put("url", endpoint);
      result = service.executeScriptFile(eventsScript, inputs);
    } catch (RuntimeException e) {
      result = PythonExecutionResult.failed(String.valueOf(e.getMessage()), e, 0);
    } finally {
      // let a later Setup call start a new client
      EVENT_CLIENTS.remove(endpoint, Thread.currentThread());
      if (onExit != null) {
        onExit.accept(result);
      }
    }
  }
}
