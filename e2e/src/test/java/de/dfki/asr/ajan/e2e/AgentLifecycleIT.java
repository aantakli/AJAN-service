package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentLifecycleIT {

    private static final String AGENT_ID = "E2EAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";

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
    @Order(1)
    void createAgentFromHelloWorldTemplate() throws Exception {
        String turtle = "@prefix ajan: <http://www.ajan.de/ajan-ns#> .\n"
                + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
                + "_:init rdf:type ajan:AgentInitialisation ;\n"
                + "  ajan:agentId \"" + AGENT_ID + "\" ;\n"
                + "  ajan:agentTemplate <" + TEMPLATE + "> .\n";
        var r = Http.send("POST", system.serviceBase() + "/agents/", "text/turtle", turtle);
        assertEquals(200, r.statusCode(),
                "agent creation should succeed, was " + r.statusCode() + ": " + r.body());
    }

    @Test
    @Order(2)
    void agentAppearsInListingAndDetails() throws Exception {
        var list = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, list.statusCode());
        assertTrue(list.body().contains(AGENT_ID), "agent listing should contain " + AGENT_ID);

        var details = Http.send("GET", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, details.statusCode());
    }

    @Test
    @Order(3)
    void agentKnowledgeVisibleInTriplestore() throws Exception {
        // Spec: "Ergebnis-RDF im Triplestore pruefen". Charakterisierung:
        // Nach der Agent-Anlage muss der Agent im Triplestore sichtbar sein.
        // Kalibrierung beim ersten Lauf: Legt der Service pro Agent ein eigenes
        // Repository an, greift die Assertion unten; landet das Agenten-RDF
        // stattdessen in einem Sammel-Repo (service.log/Repo-Listing pruefen),
        // Assertion durch eine SPARQL-Anfrage auf dieses Repo ersetzen, die
        // Triples mit "E2EAgent" findet.
        var repos = Http.send("GET", system.triplestoreUrl() + "/repositories", null, null);
        assertEquals(200, repos.statusCode());
        assertTrue(repos.body().contains(AGENT_ID),
                "triplestore should hold RDF for created agent, repositories were:\n" + repos.body());
    }

    @Test
    @Order(4)
    void sendMessageToHelloWorldCapability() throws Exception {
        String body = "<http://ajan.e2e/ping> <http://ajan.e2e/says> \"ping\" .\n";
        var r = Http.send("POST",
                system.serviceBase() + "/agents/" + AGENT_ID + "?capability=HelloWorld_BT",
                "text/turtle", body);
        assertEquals(200, r.statusCode(),
                "sending message should succeed, was " + r.statusCode() + ": " + r.body());
    }

    @Test
    @Order(5)
    void deleteAgentAndVerifyGone() throws Exception {
        var del = Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, del.statusCode(),
                "delete should succeed, was " + del.statusCode() + ": " + del.body());

        var list = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, list.statusCode());
        assertFalse(list.body().contains(AGENT_ID), "agent should be gone after delete");
    }
}
