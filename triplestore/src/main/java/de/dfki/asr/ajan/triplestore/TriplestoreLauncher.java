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
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.ContextConfig;
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
    private static final String DEFAULT_WEB_XML_RESOURCE = "conf/web.xml";

    private TriplestoreLauncher() { }

    public static void main(final String[] args) throws IOException, LifecycleException {
        int port = parsePort(args);
        Path base = Paths.get(EXTRACT_DIR).toAbsolutePath().normalize();
        Files.createDirectories(base);

        Path serverWar = extract("webapps/rdf4j.war", base.resolve("rdf4j.war"));
        Path workbenchWar = extract("webapps/workbench.war", base.resolve("workbench.war"));
        // Frueher lieferte tomcat8-maven-plugin (exec-war-only) genau diese
        // Datei als globale conf/web.xml aus und wandte sie via ContextConfig
        // auf JEDEN deployten Kontext an (siehe triplestore/src/main/tomcatconf
        // /web.xml). Tomcat.addWebapp(String, String) kennt kein globales
        // conf/web.xml, nur programmatische Defaults (Default-/JSP-Servlet,
        // Mime-Mappings, Welcome-Files) -- der darin registrierte CorsFilter
        // fehlt dadurch ersatzlos. Extrahieren und pro Kontext ueber einen
        // eigenen ContextConfig#setDefaultWebXml wieder anwenden stellt exakt
        // dieselbe Wirkung her, die der alte Runner fuer jeden Kontext hatte.
        //
        // WICHTIG zur Wirkung: org.apache.catalina.filters.CorsFilter ist
        // "deny-all by default" (DEFAULT_ALLOWED_ORIGINS = "", Tomcat-eigene
        // Quelle) -- diese Datei hat seit dem allerersten Commit dieses Repos
        // nie ein cors.allowed.origins-init-param gesetzt. Diese Restaurierung
        // stellt also genau das historische Verhalten wieder her: ein
        // aktiver Cross-Origin-Request bekommt 403 vom Filter, statt (wie
        // ohne diese Datei) klanglos durchzugehen und erst clientseitig vom
        // Browser mangels Access-Control-Allow-Origin blockiert zu werden.
        // Das ist KEINE Wiederherstellung von permissivem CORS -- wer echte
        // Browser-Clients direkt gegen :8090 erlauben will, muss
        // cors.allowed.origins bewusst und separat setzen (Produktentscheidung,
        // nicht Teil dieser Migrations-Etappe).
        Path defaultWebXml = extract(DEFAULT_WEB_XML_RESOURCE, base.resolve("conf").resolve("web.xml"));

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
        // Tomcat.addWebapp(Host, String, String, LifecycleListener) wuerde,
        // solange addDefaultWebXmlToWebapp (Default: true) aktiv bleibt, jeden
        // an "config" uebergebenen ContextConfig per
        // ContextConfig#setDefaultWebXml(noDefaultWebXmlPath()) wieder auf
        // "kein globales web.xml" zuruecksetzen (siehe
        // Tomcat.addWebapp-Quelle) -- deaktivieren, damit unser
        // setDefaultWebXml(defaultWebXml) unten tatsaechlich wirkt.
        tomcat.setAddDefaultWebXmlToWebapp(false);

        Context rdf4jCtx = tomcat.addWebapp(tomcat.getHost(), "/rdf4j", serverWar.toString(),
                newContextConfig(defaultWebXml));
        Context workbenchCtx = tomcat.addWebapp(tomcat.getHost(), "/workbench", workbenchWar.toString(),
                newContextConfig(defaultWebXml));
        // setAddDefaultWebXmlToWebapp(false) above also disabled
        // Tomcat.getDefaultWebXmlListener()'s initWebappDefaults(), which is
        // the ONLY source of the ~200 default MIME type mappings
        // (MimeTypeMappings.properties) when no real conf/web.xml is given.
        // The restored triplestore/src/main/tomcatconf/web.xml has ZERO
        // <mime-mapping> entries (it only ever carried CorsFilter plus the
        // default/JSP servlet setup), so without this call every static
        // asset in both WARs (workbench alone ships 90: *.js/*.css/*.png/
        // *.xsl) would be served with no Content-Type at all -- a regression
        // the old tomcat8-maven-plugin runner never had, because it combined
        // Tomcat's OWN real global conf/web.xml (which DOES carry the full
        // MIME table) with the app's web.xml, i.e. never went through this
        // embedded-only "programmatic defaults XOR custom default web.xml"
        // fork in Tomcat.addWebapp() at all. Re-adding just the MIME table
        // here (not re-enabling addDefaultWebXmlToWebapp) restores that
        // parity without re-registering default/jsp servlets a second time:
        // doing that instead (setAddDefaultWebXmlToWebapp(true) and setting
        // our defaultWebXml again after addWebapp()) was verified to crash
        // both contexts with "IllegalArgumentException: Child name [default]
        // is not unique", because initWebappDefaults() and our web.xml's own
        // <servlet-name>default</servlet-name>/<servlet-name>jsp</servlet-name>
        // would both try to register the same two servlet names. Everything
        // else initWebappDefaults() sets up (default servlet, JSP servlet,
        // their mappings, welcome files) is already supplied by the restored
        // web.xml itself; session-timeout defaults to 30 in StandardContext
        // regardless, matching initWebappDefaults()'s explicit setting.
        Tomcat.addDefaultMimeTypeMappings(rdf4jCtx);
        Tomcat.addDefaultMimeTypeMappings(workbenchCtx);

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
     * Baut einen {@link ContextConfig} auf, der Tomcat statt der (deaktivierten,
     * s. {@code setAddDefaultWebXmlToWebapp(false)} in {@link #main}) internen
     * programmatischen Defaults die extrahierte globale {@code conf/web.xml}
     * geben laesst -- inklusive des darin registrierten CorsFilter auf
     * {@code /*}. Jeder Kontext braucht seine eigene Instanz; ein geteilter
     * ContextConfig ist nicht wiederverwendbar (Lifecycle-Listener-Bindung an
     * genau einen Context).
     */
    private static ContextConfig newContextConfig(final Path defaultWebXml) {
        ContextConfig config = new ContextConfig();
        config.setDefaultWebXml(defaultWebXml.toString());
        return config;
    }

    /**
     * Entpackt eine Ressource (WAR oder die globale conf/web.xml) aus dem
     * Fat-JAR neben die Tomcat-Basedir. Idempotent: bei gleicher Groesse wird
     * nicht erneut geschrieben, damit wiederholte Starts (E2E-Suite startet
     * das System pro IT-Klasse neu) nicht jedes Mal ueber 100 MB kopieren.
     */
    private static Path extract(final String resource, final Path target) throws IOException {
        try (InputStream in = TriplestoreLauncher.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("bundled webapp missing from jar: " + resource);
            }
            Files.createDirectories(target.getParent());
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
