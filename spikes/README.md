# Spikes (Wegwerf-Code, Etappe 2)

Kurzlebige Wegwerf-Projekte zur Beantwortung je einer Migrationsfrage. **Kein Produktcode.**

- Bewusst **ausserhalb des Maven-Reaktors** (`ajan-parent` listet sie nicht als Module): sie duerfen Versionen verwenden, die das Hauptprojekt noch nicht traegt — Spring Boot 4.x, JDK 21, pf4j 3.15 — ohne den regulaeren Build zu beeinflussen. Jedes Spike-Projekt wird einzeln per `mvn -f spikes/<name>/pom.xml ...` gebaut.
- Nach Abschluss der jeweiligen Etappe haben sie keinen Wartungsanspruch: sie werden nicht mitmigriert, nicht refactort und duerfen entfernt werden, sobald die zugehoerige Frage im Produktcode beantwortet ist.
- Vorhanden: `boot4-resteasy` (Spike B: RESTEasy-Spring-Boot-Starter unter Boot 4), `pf4j-java21` (Spike D: pf4j aktuell + Fat-JAR-Resolution unter JDK 21), `etappe3-query-equivalence` (Task-3-Differentialharness OLD/ROUND1/NEW fuer die SailQueryPreparer-Ablösung; Ergebnis vor dem RDF4J-5.3.1-Flip unwiederholbar festgehalten in `etappe3-query-equivalence/PRE-FLIP-RESULT.md`).

Ergebnisse, Belege und Geltungsbereich der einzelnen Spikes:
`docs/superpowers/specs/2026-07-11-spike-results.md`
