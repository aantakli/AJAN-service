package de.dfki.asr.ajan.e2e;

import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginLoadIT extends AjanSystemBase {

    // Entwicklungsmodus (pf4j.mode=development): alle 10 muessen laden.
    // Achtung: das CI-dist/Docker-Image enthaelt heute nur 8 (ohne OPCUA,
    // ScriptExecutor) — dieser Test laeuft bewusst gegen den Dev-Modus.
    private static final List<String> PLUGIN_IDS = List.of(
            "ASPPlugin", "MappingPlugin", "MOSIMPlugin", "MQTTPlugin", "OPCUAPlugin",
            "PythonPlugin", "RMLMappingPlugin", "ScriptExecutorPlugin", "STRIPSPlugin",
            "StandardBTNodes");

    // A bare substring match on the plugin id is a false-positive trap: it also
    // matches error/stacktrace lines that mention a plugin which resolved but
    // then FAILED to start. pf4j's AbstractPluginManager does not emit an
    // explicit "started successfully" line, so the strongest per-plugin signal
    // it logs is "Start plugin '<id>@<version>'" (org.pf4j.AbstractPluginManager),
    // one line per plugin that reached the start phase. We require that exact
    // line per plugin id instead of a loose substring check.
    @Test
    void allTenPluginsLogStartAttemptInStartupLog() throws Exception {
        String log = Files.readString(system.serviceLog());
        for (String id : PLUGIN_IDS) {
            Pattern startLine = Pattern.compile("Start plugin '" + Pattern.quote(id) + "@[^']*'");
            assertTrue(startLine.matcher(log).find(),
                    "service startup log should contain pf4j's \"Start plugin '"
                            + id + "@<version>'\" line for plugin '" + id + "'");
        }
    }
}
