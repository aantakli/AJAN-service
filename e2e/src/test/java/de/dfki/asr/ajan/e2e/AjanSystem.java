package de.dfki.asr.ajan.e2e;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Startet Triplestore + executionservice aus den gebauten JARs als externe
 * Prozesse (analog startAll-Skripte / supervisord im Docker-Image) und
 * raeumt sie wieder ab. Charakterisiert den IST-Zustand — bei Migrations-
 * etappen darf sich am Verhalten dieser Klasse nichts aendern muessen.
 */
public final class AjanSystem implements AutoCloseable {

    private static final int TRIPLESTORE_PORT = 8090;
    private static final int SERVICE_PORT = 8080;

    private final Process triplestore; // null, wenn extern (Spike A)
    private final Process service;
    private final Path serviceLog;

    private AjanSystem(Process triplestore, Process service, Path serviceLog) {
        this.triplestore = triplestore;
        this.service = service;
        this.serviceLog = serviceLog;
    }

    public static AjanSystem start() throws IOException, InterruptedException {
        Path root = Paths.get(System.getProperty("ajan.root", "..")).toAbsolutePath().normalize();
        Path logDir = root.resolve("e2e/target/logs");
        Files.createDirectories(logDir);
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

        failIfPortInUse(SERVICE_PORT);

        String externalTriplestore = System.getProperty("ajan.e2e.triplestoreUrl");
        Process ts = null;
        String triplestoreUrl;
        if (externalTriplestore != null && !externalTriplestore.isEmpty()) {
            triplestoreUrl = externalTriplestore;
        } else {
            triplestoreUrl = "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
            failIfPortInUse(TRIPLESTORE_PORT);
            // Isoliertes RDF4J-Datenverzeichnis: kalter Start wie im Docker-Image
            Path rdf4jData = root.resolve("e2e/target/rdf4j-data");
            deleteRecursively(rdf4jData);
            List<String> cmd = new ArrayList<>(List.of(java,
                    "-Dorg.eclipse.rdf4j.appdata.basedir=" + rdf4jData,
                    "-jar", root.resolve("triplestore/target/triplestore-0.1-war-exec.jar").toString(),
                    "--httpPort=" + TRIPLESTORE_PORT));
            ts = new ProcessBuilder(cmd)
                    .directory(root.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(logDir.resolve("triplestore.log").toFile())
                    .start();
            try {
                Http.awaitOk(triplestoreUrl + "/repositories", Duration.ofSeconds(120));
            } catch (AssertionError e) {
                destroy(ts);
                throw e;
            }
        }

        Path serviceLog = logDir.resolve("service.log");
        List<String> cmd = new ArrayList<>(List.of(java,
                "-Dtriplestore.initialData.agentFolderPath=executionservice/use-case/agents",
                "-Dtriplestore.initialData.domainFolderPath=executionservice/use-case/domains",
                "-Dtriplestore.initialData.serviceFolderPath=executionservice/use-case/services",
                "-Dtriplestore.initialData.behaviorsFolderPath=executionservice/use-case/behaviors",
                "-Dtriplestore.initialData.editorDataFolderPath=executionservice/use-case/editor/editorData",
                "-Dpf4j.mode=development",
                "-Dpf4j.pluginsDir=pluginsystem/plugins",
                "-Dserver.port=" + SERVICE_PORT,
                "-DloadTTLFiles=true",
                "-Dtriplestore.url=" + triplestoreUrl,
                "-jar", root.resolve("executionservice/target/executionservice-0.1.jar").toString()));
        Process svc = new ProcessBuilder(cmd)
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(serviceLog.toFile())
                .start();
        try {
            Http.awaitOk("http://localhost:" + SERVICE_PORT + "/ajan/agents", Duration.ofSeconds(180));
        } catch (AssertionError e) {
            destroy(svc);
            destroy(ts);
            throw new AssertionError(e.getMessage() + "\n--- service.log tail ---\n" + tail(serviceLog, 60), e);
        }
        return new AjanSystem(ts, svc, serviceLog);
    }

    public String triplestoreUrl() {
        String ext = System.getProperty("ajan.e2e.triplestoreUrl");
        return (ext != null && !ext.isEmpty()) ? ext : "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
    }

    public String serviceBase() {
        return "http://localhost:" + SERVICE_PORT + "/ajan";
    }

    public Path serviceLog() {
        return serviceLog;
    }

    @Override
    public void close() {
        destroy(service);
        destroy(triplestore);
    }

    private static void destroy(Process p) {
        if (p == null) {
            return;
        }
        p.destroy();
        try {
            if (!p.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)) {
                p.destroyForcibly().waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            p.destroyForcibly();
        }
    }

    private static void failIfPortInUse(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            throw new IllegalStateException("Port " + port + " ist bereits belegt — laufende AJAN/Triplestore-Instanz stoppen.");
        } catch (IOException expected) {
            // Port frei
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private static String tail(Path file, int lines) {
        try {
            List<String> all = Files.readAllLines(file);
            return String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
        } catch (IOException e) {
            return "(log not readable: " + e + ")";
        }
    }
}
