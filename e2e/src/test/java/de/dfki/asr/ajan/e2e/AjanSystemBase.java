package de.dfki.asr.ajan.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Gemeinsame Basis fuer IT-Klassen, die den vollen AjanSystem-Prozessverbund
 * (Triplestore + executionservice) fuer die Dauer der Klasse hochfahren.
 * Verhalten identisch zu den bisherigen individuellen @BeforeAll/@AfterAll-
 * Paaren: ein System pro Testklasse, sauber abgebaut danach.
 */
public abstract class AjanSystemBase {

    protected static AjanSystem system;

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
}
