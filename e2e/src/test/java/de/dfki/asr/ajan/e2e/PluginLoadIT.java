package de.dfki.asr.ajan.e2e;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
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
        // Kalibrierung (Windows): Nach AjanSystem.close() der vorherigen
        // Testklasse bleibt der Listen-Socket des getoeteten Prozesses noch
        // 1-3s in der TCP-Tabelle (Owner-PID bereits tot, connect() gelingt
        // trotzdem) — failIfPortInUse() schluege dann faelschlich an. Daher
        // vor dem Start und nach dem Stop auf echte Port-Freigabe warten.
        awaitPortsFree();
        system = AjanSystem.start();
    }

    @AfterAll
    static void stopSystem() throws Exception {
        if (system != null) {
            system.close();
            awaitPortsFree(); // Port-Linger nicht an die naechste Testklasse durchreichen
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

    private static void awaitPortsFree() throws InterruptedException {
        awaitPortFree(8080, Duration.ofSeconds(30));
        awaitPortFree(8090, Duration.ofSeconds(30));
    }

    private static void awaitPortFree(int port, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            try (Socket ignored = new Socket("localhost", port)) {
                Thread.sleep(250); // noch belegt (ggf. Linger eines toten Prozesses)
            } catch (IOException portFree) {
                return;
            }
        }
        throw new IllegalStateException("Port " + port + " wurde nicht innerhalb von "
                + timeout.getSeconds() + "s frei — fremde Instanz laeuft?");
    }
}
