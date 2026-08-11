/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.triplestore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * Startet einen Embedded Tomcat 9 und deployed die mitgelieferten
 * RDF4J-Server- und Workbench-WARs unter /rdf4j und /workbench.
 *
 * Ersetzt das frueher per tomcat8-maven-plugin (exec-war-only) gebaute
 * triplestore-0.1-war-exec.jar. Ports, Kontextpfade und CLI bleiben identisch:
 *   java -jar triplestore-0.1.jar --httpPort=8090
 *
 * Tomcat 9 (nicht 10/11): die RDF4J-5-WARs sind javax.servlet.
 */
public final class TriplestoreLauncher {

    private static final int DEFAULT_PORT = 8080;
    private static final String PORT_OPTION = "httpPort";
    private static final String EXTRACT_DIR = ".extract";

    private TriplestoreLauncher() { }

    public static void main(final String[] args) throws IOException, LifecycleException {
        int port = parsePort(args);
        Path base = Paths.get(EXTRACT_DIR).toAbsolutePath().normalize();
        Files.createDirectories(base);

        Path serverWar = extract("webapps/rdf4j.war", base.resolve("rdf4j.war"));
        Path workbenchWar = extract("webapps/workbench.war", base.resolve("workbench.war"));

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toString());
        tomcat.setPort(port);
        tomcat.getConnector();
        // Parity mit der frueheren Plugin-Konfiguration (enableNaming=true).
        tomcat.enableNaming();
        tomcat.getHost().setAppBase(base.toString());
        // Die WARs werden explizit deployed; kein zusaetzliches Auto-Deploy,
        // sonst wuerde Tomcat sie ein zweites Mal unter /rdf4j bzw. /workbench
        // aus dem appBase-Verzeichnis aufsammeln.
        tomcat.getHost().setDeployOnStartup(false);
        tomcat.getHost().setAutoDeploy(false);

        tomcat.addWebapp("/rdf4j", serverWar.toString());
        tomcat.addWebapp("/workbench", workbenchWar.toString());

        tomcat.start();
        // Parity mit der TomcatShutdownHook, die der frueher genutzte
        // tomcat-maven-plugin-Runner (Tomcat7Runner) registriert hat: ohne
        // diesen Hook beendet SIGTERM (docker stop, supervisord, e2e
        // Process.destroy()) die JVM ohne contextDestroyed, und RDF4J faehrt
        // seine Repositories nie sauber herunter.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (LifecycleException e) {
                // best effort: die JVM faehrt ohnehin gerade herunter.
            }
        }));
        System.out.println("AJAN triplestore started: http://localhost:" + port + "/rdf4j"
                + " (workbench: http://localhost:" + port + "/workbench)");
        tomcat.getServer().await();
    }

    /**
     * Akzeptiert alle Schreibweisen, die die bestehenden Start-Skripte und
     * supervisord verwenden: --httpPort=N, -httpPort N, -httpPort=N,
     * --httpPort N. Unbekannte Argumente werden ignoriert.
     */
    static int parsePort(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String stripped = arg.startsWith("--") ? arg.substring(2)
                    : arg.startsWith("-") ? arg.substring(1) : null;
            if (stripped == null) {
                continue;
            }
            if (stripped.startsWith(PORT_OPTION + "=")) {
                return Integer.parseInt(stripped.substring(PORT_OPTION.length() + 1).trim());
            }
            if (stripped.equals(PORT_OPTION) && i + 1 < args.length) {
                return Integer.parseInt(args[i + 1].trim());
            }
        }
        return DEFAULT_PORT;
    }

    /**
     * Entpackt ein WAR aus dem Fat-JAR neben den Tomcat-Basedir. Idempotent:
     * bei gleicher Groesse wird nicht erneut geschrieben, damit wiederholte
     * Starts (E2E-Suite startet das System pro IT-Klasse neu) nicht jedes Mal
     * ueber 100 MB kopieren.
     */
    private static Path extract(final String resource, final Path target) throws IOException {
        try (InputStream in = TriplestoreLauncher.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("bundled webapp missing from jar: " + resource);
            }
            if (Files.exists(target)) {
                long bundled = countBytes(resource);
                if (Files.size(target) == bundled) {
                    return target;
                }
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private static long countBytes(final String resource) throws IOException {
        try (InputStream in = TriplestoreLauncher.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return -1;
            }
            long total = 0;
            byte[] buffer = new byte[65536];
            int read = in.read(buffer);
            while (read >= 0) {
                total += read;
                read = in.read(buffer);
            }
            return total;
        }
    }
}
