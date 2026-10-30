package de.dfki.asr.ajan.triplestore;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import org.testng.annotations.Test;

public class TriplestoreLauncherTest {

    @Test
    public void danglingHttpPortFailsFast() {
        for (String dangling: new String[] {"--httpPort", "-httpPort"}) {
            IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
                    () -> TriplestoreLauncher.parsePort(new String[] {dangling}));
            assertTrue(ex.getMessage().contains("httpPort"), ex.getMessage());
        }
    }

    @Test
    public void allDocumentedSpellingsStillParse() {
        assertEquals(TriplestoreLauncher.parsePort(new String[] {"--httpPort=8090"}), 8090);
        assertEquals(TriplestoreLauncher.parsePort(new String[] {"-httpPort=8091"}), 8091);
        assertEquals(TriplestoreLauncher.parsePort(new String[] {"-httpPort", "8092"}), 8092);
        assertEquals(TriplestoreLauncher.parsePort(new String[] {"--httpPort", "8093"}), 8093);
        assertEquals(TriplestoreLauncher.parsePort(new String[] {}), 8080);
    }
}
