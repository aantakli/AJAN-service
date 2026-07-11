package de.dfki.asr.ajan.e2e;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginLoadIT {

    // Entwicklungsmodus (pf4j.mode=development): alle 10 muessen laden.
    // Achtung: das CI-dist/Docker-Image enthaelt heute nur 8 (ohne OPCUA,
    // ScriptExecutor) — dieser Test laeuft bewusst gegen den Dev-Modus.
    private static final List<String> PLUGIN_IDS = List.of(
            "ASPPlugin", "MappingPlugin", "MOSIMPlugin", "MQTTPlugin", "OPCUAPlugin",
            "PythonPlugin", "RMLMappingPlugin", "ScriptExecutorPlugin", "STRIPSPlugin",
            "StandardBTNodes");

    private static AjanSystem system;

    @BeforeAll
    static void startSystem() throws Exception {
        system = AjanSystem.start();
    }

    @AfterAll
    static void stopSystem() {
        if (system != null) {
            system.close();
        }
    }

    @Test
    void allTenPluginsAppearInStartupLog() throws Exception {
        String log = Files.readString(system.serviceLog());
        for (String id : PLUGIN_IDS) {
            assertTrue(log.contains(id),
                    "service startup log should mention plugin '" + id + "'");
        }
    }
}
