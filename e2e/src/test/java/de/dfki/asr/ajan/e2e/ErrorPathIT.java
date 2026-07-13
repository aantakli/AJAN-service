package de.dfki.asr.ajan.e2e;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Charakterisiert die Fehlerpfade des REST-Surface. Ziel ist NICHT das
 * "richtige" HTTP-Verhalten durchzusetzen, sondern das IST festzunageln, so
 * dass eine Regression (anderer Code, anderer Content-Type, kaputtes
 * Fehler-RDF) in spaeteren Migrationsetappen sofort auffaellt.
 * Fehler-Mapping-Quelle: executionservice/.../exceptions/WebExceptionMapper.java
 * (faengt nur WebApplicationException ab).
 */
class ErrorPathIT extends AjanSystemBase {

    // (a) GET auf unbekannten Agenten: AgentsService#getAgent -> AgentManager
    // #getAgent wirft AgentNotFoundException (extends AgentValidationException
    // extends WebApplicationException, Status NOT_FOUND) -> WebExceptionMapper
    // greift -> 404 mit RDF-Fehlerbeschreibung als Turtle.
    // OBSERVED (2026-07-13): 404, Content-Type "text/turtle; charset=UTF-8"
    // (Leerzeichen nach dem Semikolon - RESTEasy haengt den Charset selbst an
    // WebExceptionMapper#createRDFExceptionResponse's "text/turtle" an).
    // Body enthaelt RDF-Statements ueber die geworfene Exception (u.a.
    // rdfs:label mit der Exception-Message und rdf:type mit dem
    // Java-Klassennamen als urn:java-class:-IRI).
    @Test
    void getUnknownAgentReturns404() throws Exception {
        String unknownId = "DoesNotExist_" + UUID.randomUUID();
        var r = Http.send("GET", system.serviceBase() + "/agents/" + unknownId, null, null);
        assertEquals(404, r.statusCode(),
                "GET of an unknown agent should be 404, was " + r.statusCode() + ": " + r.body());
        String contentType = r.headers().firstValue("Content-Type").orElse(null);
        assertEquals("text/turtle; charset=UTF-8", contentType,
                "OBSERVED error Content-Type was: " + contentType);
        assertTrue(r.body().contains("AgentNotFoundException"),
                "error body should describe the AgentNotFoundException, was:\n" + r.body());
    }

    // (b) POST von kaputtem Turtle: RDFConsumer#readFrom ruft Rio.parse(...)
    // auf, das bei Parse-Fehlern eine RDFParseException wirft. RDFParseException
    // ist eine RDF4JException (org.eclipse.rdf4j.RDF4JException, unchecked, KEIN
    // WebApplicationException) -> WebExceptionMapper greift NICHT.
    // OBSERVED (2026-07-13): trotzdem 400 (nicht 500) - RESTEasy faengt
    // Exceptions aus MessageBodyReader#readFrom offenbar selbst als
    // Bad-Request ab, unabhaengig vom projekteigenen Mapper. Content-Type
    // "text/html; charset=UTF-8" - RESTEasy's generischer HTML-Fehlerpfad,
    // NICHT das RDF-Turtle-Format des WebExceptionMapper. Body ist der rohe
    // Exception.toString() der RDFParseException.
    @Test
    void postMalformedTurtleIsRejected() throws Exception {
        var r = Http.send("POST", system.serviceBase() + "/agents/", "text/turtle", "@@@ not turtle");
        assertEquals(400, r.statusCode(),
                "OBSERVED status for malformed turtle POST was " + r.statusCode() + ": " + r.body());
        assertTrue(r.body().contains("RDFParseException"),
                "error body should mention the RDFParseException that RDFConsumer#readFrom let escape, was:\n"
                        + r.body());
        String contentType = r.headers().firstValue("Content-Type").orElse(null);
        assertEquals("text/html; charset=UTF-8", contentType,
                "OBSERVED Content-Type for the malformed-turtle error path was: " + contentType);
    }

    // (c) DELETE eines unbekannten Agenten: gleicher Sub-Resource-Locator wie
    // (a) (AgentsService#getAgent) wirft bereits AgentNotFoundException, bevor
    // AgentResource#deleteAgent (mit seinem eigenen IllegalArgumentException
    // ->404-Handling) ueberhaupt erreicht wird. Beide Pfade landen auf 404,
    // aber ueber unterschiedlichen Code.
    @Test
    void deleteUnknownAgentReturns404() throws Exception {
        String unknownId = "DoesNotExist_" + UUID.randomUUID();
        var r = Http.send("DELETE", system.serviceBase() + "/agents/" + unknownId, null, null);
        assertEquals(404, r.statusCode(),
                "DELETE of an unknown agent should be 404, was " + r.statusCode() + ": " + r.body());
    }
}
