package de.dfki.asr.ajan.e2e;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Charakterisiert die Web-Oberflaeche, fuer die die uebrige Suite blind ist:
 * CORS, Swagger, Root-Redirect, JSON-Bodies. Pinnt das IST (alter Stack:
 * Spring Boot 1.3.5 / RESTEasy 3.0.16 / Swagger 1.5) fest, damit der
 * Framework-Sprung nichts unbemerkt veraendert.
 * Quellen: ExecutionServiceApplication (CorsFilter), SwaggerUIConfiguration
 * (Redirect), JsonConsumer (exakter Media-Type-Vergleich).
 */
class WebSurfaceIT extends AjanSystemBase {

    private static final String AGENT_ID = "E2EWebSurfaceAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";
    private static final String ORIGIN = "http://editor.example";

    @BeforeAll
    static void createAgent() throws Exception {
        String turtle = "@prefix ajan: <http://www.ajan.de/ajan-ns#> .\n"
                + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
                + "_:init rdf:type ajan:AgentInitialisation ;\n"
                + "  ajan:agentId \"" + AGENT_ID + "\" ;\n"
                + "  ajan:agentTemplate <" + TEMPLATE + "> .\n";
        var r = Http.send("POST", system.serviceBase() + "/agents/", "text/turtle", turtle);
        assertEquals(200, r.statusCode(), "setup: agent creation failed: " + r.body());
    }

    @AfterAll
    static void deleteAgent() throws Exception {
        Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
    }

    private static String root() {
        String base = system.serviceBase();
        return base.substring(0, base.length() - "/ajan".length());
    }

    private static String header(HttpResponse<?> r, String name) {
        return r.headers().firstValue(name).orElse(null);
    }

    private static String mediaType(HttpResponse<?> r) {
        String ct = header(r, "Content-Type");
        assertTrue(ct != null, "response has no Content-Type");
        return ct.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private static Set<String> csvSet(String value) {
        return Arrays.stream(value.split(","))
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // OBSERVED (2026-10-30): GET /ajan/agents with Origin -> 200,
    // "Access-Control-Allow-Origin: *" (literal wildcard, NOT the echoed origin),
    // "Vary: Origin", NO Access-Control-Allow-Credentials header at all.
    @Test
    void corsSimpleRequest() throws Exception {
        var r = Http.sendWithHeaders("GET", system.serviceBase() + "/agents",
                Map.of("Origin", ORIGIN), null);
        assertEquals(200, r.statusCode());
        assertEquals("*", header(r, "Access-Control-Allow-Origin"));
        assertTrue(r.headers().firstValue("Access-Control-Allow-Credentials").isEmpty(),
                "OBSERVED: no Allow-Credentials header, was " + header(r, "Access-Control-Allow-Credentials"));
    }

    // OBSERVED (2026-10-30): OPTIONS preflight -> 200, Allow-Origin "*",
    // Allow-Methods "OPTIONS,HEAD,GET,PUT,POST,DELETE,PATCH", Allow-Headers
    // "content-type", no Allow-Credentials, Vary: Origin (not pinned).
    @Test
    void corsPreflight() throws Exception {
        var r = Http.sendWithHeaders("OPTIONS", system.serviceBase() + "/agents",
                Map.of("Origin", ORIGIN,
                        "Access-Control-Request-Method", "POST",
                        "Access-Control-Request-Headers", "content-type"), null);
        assertEquals(200, r.statusCode());
        assertEquals("*", header(r, "Access-Control-Allow-Origin"));
        assertTrue(r.headers().firstValue("Access-Control-Allow-Credentials").isEmpty(),
                "OBSERVED: no Allow-Credentials header, was " + header(r, "Access-Control-Allow-Credentials"));
        assertEquals(Set.of("OPTIONS", "HEAD", "GET", "PUT", "POST", "DELETE", "PATCH"),
                csvSet(header(r, "Access-Control-Allow-Methods")));
        String allowHeaders = header(r, "Access-Control-Allow-Headers");
        assertTrue(allowHeaders != null && allowHeaders.toLowerCase(Locale.ROOT).contains("content-type"),
                "Allow-Headers should contain content-type, was " + allowHeaders);
    }

    // OBSERVED (2026-10-30): 200, "Content-Type: application/json; charset=UTF-8",
    // body {"swagger":"2.0",...,"paths":{"/agents":{...
    @Test
    void swaggerSpecIsServed() throws Exception {
        var r = Http.send("GET", system.serviceBase() + "/swagger.json", null, null);
        assertEquals(200, r.statusCode());
        assertEquals("application/json", mediaType(r));
        assertTrue(r.body().contains("\"/agents\""), "spec should describe /agents");
    }

    // OBSERVED (2026-10-30): GET / -> 302 Found,
    // "Location: http://localhost:8080/swagger-ui/index.html?url=/ajan/swagger.json".
    // The query parameter may be dropped later; the target page may not change.
    @Test
    void rootRedirectsToSwaggerUi() throws Exception {
        var r = Http.send("GET", root() + "/", null, null);
        assertEquals(302, r.statusCode());
        String location = header(r, "Location");
        assertTrue(location != null, "redirect needs a Location header");
        assertTrue(location.contains("/swagger-ui/index.html"),
                "Location should point at the swagger UI, was " + location);
        assertTrue(location.endsWith("/swagger-ui/index.html?url=/ajan/swagger.json")
                        || location.endsWith("/swagger-ui/index.html"),
                "unexpected Location: " + location);
    }

    // OBSERVED (2026-10-30): 200, "Content-Type: text/html; charset=UTF-8".
    @Test
    void swaggerUiIsServed() throws Exception {
        var r = Http.send("GET", root() + "/swagger-ui/index.html", null, null);
        assertEquals(200, r.statusCode());
        assertEquals("text/html", mediaType(r));
    }

    // swagger-ui 5 ignores the ?url= query parameter, so the UI is configured by
    // swagger-initializer.js; it must point at the AJAN spec, not the petstore demo.
    @Test
    void swaggerUiPointsToAjanSpec() throws Exception {
        var r = Http.send("GET", root() + "/swagger-ui/swagger-initializer.js", null, null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("/ajan/swagger.json"), "initializer should point at the AJAN spec");
        assertFalse(r.body().contains("petstore"), "initializer must not fall back to the petstore demo");
    }

    // OBSERVED (2026-10-30): 200, "Content-Type: application/json; charset=UTF-8",
    // body [{"id":"services","sparqlEndpoint":...}].
    @Test
    void tripleDatabasesAsJson() throws Exception {
        var r = Http.getWithAccept(system.serviceBase() + "/tripledatabases", "application/json");
        assertEquals(200, r.statusCode());
        assertEquals("application/json", mediaType(r));
        assertTrue(r.body().trim().startsWith("["), "body should be a JSON array, was: " + r.body());
    }

    // OBSERVED (2026-10-30): 200, "Content-Type: application/octet-stream; charset=UTF-8",
    // body "pong".
    @Test
    void pingAnswers() throws Exception {
        var r = Http.send("GET", system.serviceBase() + "/ping", null, null);
        assertEquals(200, r.statusCode());
        assertEquals("application/octet-stream", mediaType(r));
    }

    // OBSERVED (2026-10-30): POST ?capability=HelloWorld_BT with a JSON body is
    // accepted by the body reader (no 400/415) but then fails in the endpoint:
    // 500, body "ClassCastException: ObjectNode cannot be cast to Model"
    // (HelloWorld_BT is a ModelEvent endpoint, JSON input is a JsonNode).
    // The 500 therefore pins "JSON was parsed and dispatched", not business
    // success. Charset variant and plain variant behave identically today
    // (no difference between the two at the HTTP level).
    @Test
    void jsonBodyWithCharsetIsAccepted() throws Exception {
        var r = Http.send("POST", system.serviceBase() + "/agents/" + AGENT_ID + "?capability=HelloWorld_BT",
                "application/json; charset=UTF-8", "{}");
        assertEquals(500, r.statusCode(), "OBSERVED status, body: " + r.body());
    }

    // OBSERVED (2026-10-30): see jsonBodyWithCharsetIsAccepted - 500, same cause.
    @Test
    void jsonBodyPlainIsAccepted() throws Exception {
        var r = Http.send("POST", system.serviceBase() + "/agents/" + AGENT_ID + "?capability=HelloWorld_BT",
                "application/json", "{}");
        assertEquals(500, r.statusCode(), "OBSERVED status, body: " + r.body());
    }
}
