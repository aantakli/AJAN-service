package de.dfki.asr.ajan.e2e;

import java.io.StringReader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Charakterisiert die RDF-Serialisierung von {@code GET /ajan/agents/{id}}:
 * Content-Type-Aushandlung (Turtle vs. JSON-LD) und dass der Body ein
 * echtes, geparstes RDF-Modell mit dem erwarteten Agent-Subjekt ist — nicht
 * nur ein Statuscode/Substring-Treffer. Das ist genau die Regression, die
 * RDF4J 5 / Boot 4 am ehesten still einfuehren wuerden (anderer
 * Content-Type, anderes Serialisierungsformat, kaputte Namespaces).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RdfResponseIT extends AjanSystemBase {

    private static final String AGENT_ID = "E2ERdfAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";
    private static final IRI AGENT_TYPE = SimpleValueFactory.getInstance()
            .createIRI("http://www.ajan.de/ajan-ns#Agent");

    @Test
    @Order(1)
    void createAgentForRdfCharacterization() throws Exception {
        String turtle = "@prefix ajan: <http://www.ajan.de/ajan-ns#> .\n"
                + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
                + "_:init rdf:type ajan:AgentInitialisation ;\n"
                + "  ajan:agentId \"" + AGENT_ID + "\" ;\n"
                + "  ajan:agentTemplate <" + TEMPLATE + "> .\n";
        var r = Http.send("POST", system.serviceBase() + "/agents/", "text/turtle", turtle);
        assertEquals(200, r.statusCode(),
                "agent creation should succeed, was " + r.statusCode() + ": " + r.body());
    }

    // OBSERVED (2026-07-13, executionservice on RESTEasy/JAX-RS, RDF4J 3.6.3):
    // GET /ajan/agents/{id} with Accept: text/turtle -> 200,
    // Content-Type "text/turtle; charset=UTF-8" (note the space after the
    // semicolon - RESTEasy's default charset-append convention). Body parses
    // as valid Turtle with the agent's own URI as subject, rdf:type
    // ajan:Agent present, and more than one triple (id/template/knowledge/
    // behavior/action statements from ModelProducer.addAgentStatements).
    @Test
    @Order(2)
    void turtleRepresentationHasPinnedContentTypeAndParsesAsRdf() throws Exception {
        String agentUrl = system.serviceBase() + "/agents/" + AGENT_ID;
        var r = Http.getWithAccept(agentUrl, "text/turtle");
        assertEquals(200, r.statusCode(), "GET agent with Accept: text/turtle should succeed, was "
                + r.statusCode() + ": " + r.body());

        String contentType = r.headers().firstValue("Content-Type").orElse(null);
        assertEquals("text/turtle; charset=UTF-8", contentType,
                "OBSERVED Content-Type for Accept: text/turtle was: " + contentType);

        Model model = Rio.parse(new StringReader(r.body()), agentUrl, RDFFormat.TURTLE);
        assertFalse(model.isEmpty(), "turtle body should parse into a non-empty RDF model, was:\n" + r.body());

        IRI agentSubject = SimpleValueFactory.getInstance().createIRI(agentUrl);
        assertTrue(model.contains(agentSubject, RDF.TYPE, AGENT_TYPE),
                "parsed model should contain <" + agentUrl + "> rdf:type ajan:Agent, model was:\n" + model);
    }

    // OBSERVED (2026-07-13): GET /ajan/agents/{id} with Accept:
    // application/ld+json -> 200, Content-Type
    // "application/ld+json; charset=UTF-8". Pinning the header only (Step 4
    // does not require parsing the JSON-LD body); both negotiated formats get
    // the same "; charset=UTF-8" suffix appended by RESTEasy, which is itself
    // worth pinning as a migration trip wire.
    @Test
    @Order(3)
    void jsonLdRepresentationHasPinnedContentType() throws Exception {
        String agentUrl = system.serviceBase() + "/agents/" + AGENT_ID;
        var r = Http.getWithAccept(agentUrl, "application/ld+json");
        assertEquals(200, r.statusCode(), "GET agent with Accept: application/ld+json should succeed, was "
                + r.statusCode() + ": " + r.body());

        String contentType = r.headers().firstValue("Content-Type").orElse(null);
        assertEquals("application/ld+json; charset=UTF-8", contentType,
                "OBSERVED Content-Type for Accept: application/ld+json was: " + contentType);
        assertFalse(r.body().isBlank(), "json-ld body should not be blank");
    }

    @Test
    @Order(4)
    void cleanUpAgent() throws Exception {
        var del = Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, del.statusCode(), "cleanup delete should succeed, was " + del.statusCode());
    }
}
