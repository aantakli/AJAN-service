package de.dfki.asr.ajan.e2e;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Startet Triplestore + executionservice aus den gebauten JARs als externe
 * Prozesse (analog startAll-Skripte / supervisord im Docker-Image) und
 * raeumt sie wieder ab. Charakterisiert den IST-Zustand — bei Migrations-
 * etappen darf sich am Verhalten dieser Klasse nichts aendern muessen.
 *
 * <p>Container-Modus: ist {@code ajan.e2e.serviceUrl} gesetzt (z.B.
 * {@code http://localhost:8080/ajan}), wird kein Service gestartet/gestoppt,
 * sondern gegen diese URL getestet; der Triplestore kommt dann aus
 * {@code ajan.e2e.triplestoreUrl}, das Startlog fuer {@code PluginLoadIT} aus
 * {@code ajan.e2e.serviceLogFile} (z.B. {@code docker logs} in eine Datei).
 */
public final class AjanSystem implements AutoCloseable {

    private static final int TRIPLESTORE_PORT = 8090;
    private static final int SERVICE_PORT = 8080;
    private static final Duration PORT_RELEASE_TIMEOUT = Duration.ofSeconds(30);

    private final Process triplestore; // null, wenn extern (ajan.e2e.triplestoreUrl)
    private final Process service; // null im Container-Modus (ajan.e2e.serviceUrl)
    private final Path serviceLog;

    private AjanSystem(Process triplestore, Process service, Path serviceLog) {
        this.triplestore = triplestore;
        this.service = service;
        this.serviceLog = serviceLog;
    }

    public static AjanSystem start() throws IOException, InterruptedException {
        String externalService = externalServiceUrl();
        if (externalService != null) {
            return attachExternal(externalService);
        }
        Path root = Paths.get(System.getProperty("ajan.root", "..")).toAbsolutePath().normalize();
        Path logDir = root.resolve("e2e/target/logs");
        Files.createDirectories(logDir);
        String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

        failIfPortInUse(SERVICE_PORT);

        String externalTriplestore = prop("ajan.e2e.triplestoreUrl");
        Process ts = null;
        String triplestoreUrl;
        if (externalTriplestore != null) {
            triplestoreUrl = externalTriplestore;
        } else {
            triplestoreUrl = "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
            failIfPortInUse(TRIPLESTORE_PORT);
            // Isoliertes RDF4J-Datenverzeichnis: kalter Start wie im Docker-Image
            Path rdf4jData = root.resolve("e2e/target/rdf4j-data");
            deleteRecursively(rdf4jData);
            Path triplestoreJar = resolveSingleArtifact(root.resolve("triplestore/target"), "triplestore-*.jar");
            List<String> cmd = new ArrayList<>(List.of(javaBin,
                    "-Dorg.eclipse.rdf4j.appdata.basedir=" + rdf4jData,
                    "-jar", triplestoreJar.toString(),
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
                awaitPortFree(TRIPLESTORE_PORT);
                throw e;
            }
        }

        Path serviceLog = logDir.resolve("service.log");
        Path serviceJar = resolveSingleArtifact(root.resolve("executionservice/target"), "executionservice-*.jar");
        List<String> cmd = new ArrayList<>(List.of(javaBin,
                // JEP (PythonPlugin) loads native code; same flag as supervisord.conf
                "--enable-native-access=ALL-UNNAMED",
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
                "-jar", serviceJar.toString()));
        Process svc = new ProcessBuilder(cmd)
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(serviceLog.toFile())
                .start();
        try {
            Http.awaitOk("http://localhost:" + SERVICE_PORT + "/ajan/agents", Duration.ofSeconds(180));
            // OBSERVED (2026-07-13): InitialDataProvider loads the use-case TTL
            // files (agents.trig etc.) synchronously as part of Spring context
            // startup, strictly BEFORE the "Started Application" log line and
            // therefore before Tomcat ever answers /ajan/agents with 200 -
            // so this check never actually has to wait. It stays as an
            // explicit, independently-verified belt-and-suspenders gate in
            // case a future Boot/RDF4J bump makes the load asynchronous.
            awaitTtlDataLoaded(triplestoreUrl);
        } catch (AssertionError e) {
            destroy(svc);
            destroy(ts);
            awaitPortFree(SERVICE_PORT);
            if (ts != null) {
                awaitPortFree(TRIPLESTORE_PORT);
            }
            throw new AssertionError(e.getMessage() + "\n--- service.log tail ---\n" + tail(serviceLog, 60), e);
        }
        return new AjanSystem(ts, svc, serviceLog);
    }

    /**
     * Container-Modus: nichts starten, nur auf den laufenden Service und die
     * geladenen Use-Case-Daten warten. {@link #close()} stoppt dann nichts.
     */
    private static AjanSystem attachExternal(final String serviceUrl) throws InterruptedException {
        String triplestoreUrl = prop("ajan.e2e.triplestoreUrl");
        if (triplestoreUrl == null) {
            throw new IllegalStateException("ajan.e2e.serviceUrl requires ajan.e2e.triplestoreUrl as well");
        }
        String logFile = prop("ajan.e2e.serviceLogFile");
        if (logFile == null) {
            throw new IllegalStateException("ajan.e2e.serviceUrl requires ajan.e2e.serviceLogFile as well");
        }
        Http.awaitOk(serviceUrl + "/agents", Duration.ofSeconds(180));
        awaitTtlDataLoaded(triplestoreUrl);
        return new AjanSystem(null, null, Paths.get(logFile).toAbsolutePath());
    }

    private static String externalServiceUrl() {
        String url = prop("ajan.e2e.serviceUrl");
        if (url == null) {
            return null;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** System property {@code name}, or {@code null} if it is unset or empty. */
    private static String prop(final String name) {
        String value = System.getProperty(name);
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Wartet zusaetzlich zum blossen HTTP-200 auf /ajan/agents darauf, dass
     * die "agents"-Repository im Triplestore existiert UND das
     * HelloWorld-Template dort tatsaechlich Daten hat. Ein einfaches GET auf
     * die Fragment-URI des Templates ist dafuer ungeeignet: HTTP-Clients
     * senden den Fragment-Teil einer URI nie an den Server (RFC 9110), ein
     * GET auf ".../repositories/agents#AG_..." kommt beim RDF4J-Server also
     * nur als GET auf ".../repositories/agents" an - das ist der
     * SPARQL-Query-Endpunkt der Repository und liefert ohne "query"-Parameter
     * 400 Bad Request, unabhaengig vom Ladezustand. Stattdessen wird eine
     * echte SPARQL-ASK-Anfrage nach dem Template-Subjekt gestellt.
     */
    private static void awaitTtlDataLoaded(final String triplestoreUrl) throws InterruptedException {
        String templateIri = "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";
        String askQuery = "ASK { <" + templateIri + "> ?p ?o }";
        String askUrl = triplestoreUrl + "/repositories/agents?query="
                + URLEncoder.encode(askQuery, StandardCharsets.UTF_8);
        Duration timeout = Duration.ofSeconds(60);
        long deadline = System.nanoTime() + timeout.toNanos();
        String last = "no response";
        while (System.nanoTime() < deadline) {
            try {
                var repos = Http.send("GET", triplestoreUrl + "/repositories", null, null);
                boolean agentsRepoExists = repos.statusCode() == 200 && repos.body().contains("agents");
                if (agentsRepoExists) {
                    var ask = Http.getWithAccept(askUrl, "application/sparql-results+json");
                    if (ask.statusCode() == 200 && ask.body().contains("true")) {
                        return;
                    }
                    last = "agents repo present, HelloWorld template ASK query returned "
                            + ask.statusCode() + ": " + ask.body();
                } else {
                    last = "agents repo not yet present, repositories listing: " + repos.body();
                }
            } catch (IOException e) {
                last = e.toString();
            }
            Thread.sleep(1000);
        }
        throw new AssertionError(
                "Timeout waiting for use-case TTL data to load (agents repo + HelloWorld template ASK-able); last: "
                        + last);
    }

    private static Path resolveSingleArtifact(final Path dir, final String glob) throws IOException {
        List<Path> matches = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
            stream.forEach(matches::add);
        }
        if (matches.size() != 1) {
            throw new IllegalStateException("Expected exactly one artifact matching '" + glob + "' in " + dir
                    + " but found " + matches.size() + ": " + matches);
        }
        return matches.get(0);
    }

    public String triplestoreUrl() {
        String ext = prop("ajan.e2e.triplestoreUrl");
        return ext != null ? ext : "http://localhost:" + TRIPLESTORE_PORT + "/rdf4j";
    }

    public String serviceBase() {
        String ext = externalServiceUrl();
        return ext != null ? ext : "http://localhost:" + SERVICE_PORT + "/ajan";
    }

    public Path serviceLog() {
        return serviceLog;
    }

    @Override
    public void close() {
        if (service == null) {
            return; // Container-Modus: externer Service bleibt laufen
        }
        destroy(service);
        destroy(triplestore);
        // Windows: Nach dem Toeten eines Prozesses bleibt dessen Listen-Socket
        // noch 1-3s in der TCP-Tabelle (Owner-PID bereits tot, connect()
        // gelingt trotzdem). Ohne dieses Warten schlaegt failIfPortInUse()
        // in der naechsten AjanSystem.start()-Instanz (z.B. der naechsten
        // IT-Klasse) faelschlich an.
        awaitPortFree(SERVICE_PORT);
        if (triplestore != null) {
            awaitPortFree(TRIPLESTORE_PORT);
        }
    }

    private static void awaitPortFree(int port) {
        Instant deadline = Instant.now().plus(PORT_RELEASE_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            try (Socket ignored = new Socket("localhost", port)) {
                // noch belegt (ggf. Linger eines toten Prozesses)
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (IOException portFree) {
                return;
            }
        }
        throw new IllegalStateException("Port " + port + " wurde nach close() nicht innerhalb von "
                + PORT_RELEASE_TIMEOUT.getSeconds() + "s frei.");
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
