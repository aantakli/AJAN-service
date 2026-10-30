package de.dfki.asr.ajan.e2e;

import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Python runtime acceptance test: an agent built from the shipped Python_Example
 * (executionservice/use-case/examples/Python_Example; tracked lower-case, it shares
 * a case-insensitive directory with Examples/ on Windows) runs its PythonLeafNode through the
 * embedded JEP/CPython env, and the script's output lands in the agent's knowledge base.
 *
 * <p>The example is not part of the initial use-case data the service loads at startup, so
 * the test pushes the shipped agents.ttl/behaviors.ttl into the triplestore over the RDF4J
 * REST API (works the same in host and container mode).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PythonNodeIT extends AjanSystemBase {

    private static final String AGENT_ID = "E2EPythonAgent";
    private static final String TEMPLATE =
            "http://localhost:8090/rdf4j/repositories/agents#AG_adc0aa84-fd2c-43e3-8c7a-593d4de609b9";
    private static final String CAPABILITY = "default";
    private static final Duration EFFECT_TIMEOUT = Duration.ofSeconds(60);
    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI AGENT_HAS_KNOWLEDGE = VF.createIRI("http://www.ajan.de/ajan-ns#agentKnowledge");
    private static final IRI AGENT_HAS_ID = VF.createIRI("http://www.ajan.de/ajan-ns#agentId");
    private static final IRI PYTHON_LABEL = VF.createIRI("http://www.ajan.de/python-ns#label");
    private static final String EXPECTED_LABEL = "is an Python Agent";
    private static final Pattern ASK_BOOLEAN = Pattern.compile("\"boolean\"\\s*:\\s*(true|false)");

    @Test
    @Order(1)
    void loadPythonExampleIntoTriplestore() throws Exception {
        String askUrl = system.triplestoreUrl() + "/repositories/agents?query="
                + URLEncoder.encode("ASK { <" + TEMPLATE + "> ?p ?o }", StandardCharsets.UTF_8);
        var ask = Http.getWithAccept(askUrl, "application/sparql-results+json");
        assertEquals(200, ask.statusCode(), "ASK on agents repo failed: " + ask.body());
        if (askResult(ask.body())) {
            return; // already present (e.g. a reused container)
        }
        Path example = Paths.get(System.getProperty("ajan.root", "..")).toAbsolutePath().normalize()
                .resolve("executionservice/use-case/examples/Python_Example");
        upload("agents", example.resolve("agents.ttl"));
        upload("behaviors", example.resolve("behaviors.ttl"));
    }

    @Test
    @Order(2)
    void createAgentFromPythonExampleTemplate() throws Exception {
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
    @Order(3)
    void pythonLeafNodeWritesItsResultToAgentKnowledge() throws Exception {
        String agentUrl = system.serviceBase() + "/agents/" + AGENT_ID;
        var trigger = Http.send("POST", agentUrl + "?capability=" + CAPABILITY, "text/turtle",
                "<http://ajan.e2e/ping> <http://ajan.e2e/says> \"python\" .\n");
        assertEquals(200, trigger.statusCode(),
                "triggering the behavior should succeed, was " + trigger.statusCode() + ": " + trigger.body());

        String knowledgeEndpoint = knowledgeEndpoint(agentUrl);
        Model last = null;
        long deadline = System.nanoTime() + EFFECT_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            var knowledge = Http.getWithAccept(knowledgeEndpoint, "text/turtle");
            if (knowledge.statusCode() == 200) {
                last = Rio.parse(new StringReader(knowledge.body()), knowledgeEndpoint, RDFFormat.TURTLE);
                if (hasPythonLabel(last)) {
                    return;
                }
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("expected <agent> <" + PYTHON_LABEL + "> \"" + EXPECTED_LABEL
                + "\" in the agent knowledge at " + knowledgeEndpoint + " within "
                + EFFECT_TIMEOUT.getSeconds() + "s. Last fetched model:\n" + last
                + "\n--- service log tail ---\n" + serviceLogTail());
    }

    @Test
    @Order(4)
    void deleteAgent() throws Exception {
        var del = Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        assertEquals(200, del.statusCode(),
                "delete should succeed, was " + del.statusCode() + ": " + del.body());
    }

    /**
     * Safety net: the agent is gone even if a test before deleteAgent() failed (a reused container
     * would otherwise keep it and the next run's create would fail). Errors are ignored; this runs
     * before AjanSystemBase stops the system.
     */
    @AfterAll
    static void deleteAgentBestEffort() {
        if (system == null) {
            return;
        }
        try {
            Http.send("DELETE", system.serviceBase() + "/agents/" + AGENT_ID, null, null);
        } catch (Exception ignored) {
            // best effort (already deleted by deleteAgent() or the service is down)
        }
    }

    /** The {@code boolean} field of a SPARQL ASK result in JSON. */
    private static boolean askResult(String json) {
        Matcher m = ASK_BOOLEAN.matcher(json);
        assertTrue(m.find(), "ASK result has no boolean field: " + json);
        return Boolean.parseBoolean(m.group(1));
    }

    /** The agent resource (subject carrying our agentId) must carry the label the script adds. */
    private static boolean hasPythonLabel(Model knowledge) {
        Set<Resource> agents = knowledge.filter(null, AGENT_HAS_ID, VF.createLiteral(AGENT_ID)).subjects();
        for (Resource agent : agents) {
            for (Value v : knowledge.filter(agent, PYTHON_LABEL, null).objects()) {
                if (EXPECTED_LABEL.equals(v.stringValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String knowledgeEndpoint(String agentUrl) throws Exception {
        var agentTurtle = Http.getWithAccept(agentUrl, "text/turtle");
        assertEquals(200, agentTurtle.statusCode(),
                "GET agent should succeed, was " + agentTurtle.statusCode() + ": " + agentTurtle.body());
        Model agentModel = Rio.parse(new StringReader(agentTurtle.body()), agentUrl, RDFFormat.TURTLE);
        Iterator<Value> endpoints = agentModel
                .filter(VF.createIRI(agentUrl), AGENT_HAS_KNOWLEDGE, null).objects().iterator();
        assertTrue(endpoints.hasNext(),
                "agent representation should expose ajan:agentKnowledge, model was:\n" + agentModel);
        return endpoints.next().stringValue();
    }

    private static void upload(String repo, Path ttl) throws Exception {
        var r = Http.send("POST", system.triplestoreUrl() + "/repositories/" + repo + "/statements",
                "text/turtle", Files.readString(ttl));
        assertEquals(204, r.statusCode(),
                "uploading " + ttl + " to repo " + repo + " failed: " + r.statusCode() + ": " + r.body());
    }

    private static String serviceLogTail() {
        try {
            var lines = Files.readAllLines(system.serviceLog());
            return String.join("\n", lines.subList(Math.max(0, lines.size() - 80), lines.size()));
        } catch (Exception e) {
            return "(service log not readable: " + e + ")";
        }
    }
}
