package de.dfki.asr.ajan.e2e;

import java.io.StringReader;
import java.time.Duration;
import java.util.Iterator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentLifecycleIT extends AjanSystemBase {

    private static final String AGENT_ID = "E2EAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_HelloWorld_BT_30275863-0113-4c7c-9ed9-b0502c643fa6";
    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI AGENT_HAS_KNOWLEDGE = VF.createIRI("http://www.ajan.de/ajan-ns#agentKnowledge");
    private static final IRI HELLO_WORD = VF.createIRI("http://www.ajan.de/ajan-ns#HelloWord");
    private static final IRI HELLO_MESSAGE = VF.createIRI("http://www.ajan.de/ajan-ns#message");

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

    // Step 6 (Task 4b): HelloWorld-Wirkungsprobe. Die BT-Definition
    // (executionservice/use-case/behaviors/behaviors.ttl,
    // BT_a719c6d6-57a5-4abd-86d3-03366ff4c8db) fuehrt bei Ausloesung von
    // HelloWorld_BT einen UpdateQuery mit originBase AgentKnowledge aus:
    // "INSERT { ajan:HelloWord ajan:message \"Hello World\" . } WHERE {...}".
    // AgentKnowledge ist exakt die Belief-Base, deren SPARQL-Update-Endpoint
    // ModelProducer#addAgentStatements unter dem Praedikat ajan:agentKnowledge
    // an der Agent-Repraesentation anhaengt (siehe AJANVocabulary.AGENT_HAS_KNOWLEDGE,
    // beide gespeist aus agent.getBeliefs()). Der Effekt ist damit von aussen
    // erreichbar: GET auf den agentKnowledge-Endpoint liefert den vollen
    // Graphen dieser Belief-Base als RDF (RDF4J-Repository-/statements-Route).
    // Nicht erreichbar/beobachtbar ist der Effekt ueber
    // GET .../behaviors/{id}?method=knowledge: das liefert die
    // ExecutionBeliefBase (originBase ExecutionKnowledge), eine andere
    // Belief-Base als die, in die die HelloWorld-BT tatsaechlich schreibt.
    @Test
    @Order(5)
    void helloWorldEffectAppearsInAgentKnowledgeBase() throws Exception {
        String agentUrl = system.serviceBase() + "/agents/" + AGENT_ID;
        var agentTurtle = Http.getWithAccept(agentUrl, "text/turtle");
        assertEquals(200, agentTurtle.statusCode(),
                "GET agent should succeed, was " + agentTurtle.statusCode() + ": " + agentTurtle.body());
        Model agentModel = Rio.parse(new StringReader(agentTurtle.body()), agentUrl, RDFFormat.TURTLE);
        IRI agentResource = VF.createIRI(agentUrl);
        Iterator<Value> knowledgeEndpoints = agentModel
                .filter(agentResource, AGENT_HAS_KNOWLEDGE, null).objects().iterator();
        assertTrue(knowledgeEndpoints.hasNext(),
                "agent representation should expose ajan:agentKnowledge, model was:\n" + agentModel);
        String knowledgeEndpoint = knowledgeEndpoints.next().stringValue();

        boolean effectObserved = false;
        Model lastKnowledgeModel = null;
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while (System.nanoTime() < deadline) {
            var knowledge = Http.getWithAccept(knowledgeEndpoint, "text/turtle");
            if (knowledge.statusCode() == 200) {
                lastKnowledgeModel = Rio.parse(new StringReader(knowledge.body()), knowledgeEndpoint, RDFFormat.TURTLE);
                if (lastKnowledgeModel.contains(HELLO_WORD, HELLO_MESSAGE, null)) {
                    effectObserved = true;
                    break;
                }
            }
            Thread.sleep(1000);
        }

        assertTrue(effectObserved,
                "OBSERVED: the HelloWorld_BT effect (ajan:HelloWord ajan:message \"Hello World\") "
                        + "was expected in the agent's AgentKnowledge base at " + knowledgeEndpoint
                        + " after sending the HelloWorld_BT capability, within 30s. Last fetched model:\n"
                        + lastKnowledgeModel);
    }

    @Test
    @Order(6)
    void deleteAgentAndVerifyGone() throws Exception {
        var del = Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, del.statusCode(),
                "delete should succeed, was " + del.statusCode() + ": " + del.body());

        var list = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, list.statusCode());
        assertFalse(list.body().contains(AGENT_ID), "agent should be gone after delete");
    }
}
