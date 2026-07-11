package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSmokeIT {

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
    void triplestoreListsRepositoriesIncludingAgents() throws Exception {
        var r = Http.send("GET", system.triplestoreUrl() + "/repositories", null, null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("agents"),
                "repositories listing should contain 'agents' repo, was:\n" + r.body());
    }

    @Test
    void serviceListsAgentsEndpoint() throws Exception {
        var r = Http.send("GET", system.serviceBase() + "/agents", null, null);
        assertEquals(200, r.statusCode());
    }
}
