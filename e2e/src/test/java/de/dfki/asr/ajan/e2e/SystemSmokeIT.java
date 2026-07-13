package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSmokeIT extends AjanSystemBase {

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
