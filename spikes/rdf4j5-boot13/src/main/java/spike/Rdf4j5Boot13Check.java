package spike;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

/**
 * Gate fuer Etappe 3: laeuft der RDF4J-5.3.1-Client unter dem
 * Spring-Boot-1.3.5-dependencyManagement (Jackson/httpclient/slf4j) auf JDK 11
 * gegen einen echten RDF4J-5-Server?
 *
 * Geprueft werden fuenf Pfade:
 *   (1) Repository-Verwaltung ueber RemoteRepositoryManager,
 *   (2) SPARQL-GraphQuery ueber Repositories.graphQuery  <-- die Spike-A-Bruchstelle,
 *   (3) Rio-Serialisierung Turtle UND JSON-LD.
 *   (4) Repositories.tupleQuery mit dem unforcierten Default-Ergebnisformat der Session,
 *   (5) erzwungener SPARQL-Results-JSON-Roundtrip ueber QueryResultIO.
 *
 * KORREKTUR (Fix-Runde 1, Review-Befund CRITICAL 1+2): Die urspruengliche Fassung
 * dieses Probes behauptete "(3) Rio-Serialisierung Turtle UND JSON-LD <-- der
 * Jackson-Pfad" -- das war falsch und ist widerlegt. JSON-LD haengt in RDF4J 5 NICHT
 * an Jackson, sondern an der hasmac-JSONLD-Java-Implementierung (no.hasmac.jsonld.*)
 * plus jakarta.json/JSON-P (org.glassfish:jakarta.json:module, jakarta.json:jakarta.
 * json-api) -- verifiziert per `unzip -l rdf4j-rio-jsonld-5.3.1.jar`: keine einzige
 * com/fasterxml/jackson-Klassenreferenz in diesem Artefakt. Damit hat Variante A der
 * urspruenglichen Probe-Fassung den eigentlichen Risikofall (Jackson unter dem
 * Boot-1.3.5-Pin 2.6.6) nie ausgefuehrt. Der tatsaechliche Jackson-Konsument in
 * RDF4J 5.3.1 ist `rdf4j-queryresultio-sparqljson` (auf dem Klassenpfad, s.
 * tree-A.txt): `AbstractSPARQLJSONWriter`/`AbstractSPARQLJSONParser` referenzieren
 * laut javap `com.fasterxml.jackson.core.{JsonFactory,JsonGenerator,JsonFactoryBuilder,
 * StreamWriteFeature,StreamReadFeature}` -- die letzten drei existieren erst ab
 * Jackson 2.10 und fehlen in jackson-core-2.6.6.jar (dem Boot-1.3.5-Pin). Die neuen
 * Beine (4)+(5) unten schliessen genau diese Luecke: (4) prueft den vom Produktcode
 * tatsaechlich genutzten SELECT-Pfad mit dem unforcierten Default-Format, (5) erzwingt
 * SPARQL-Results-JSON und damit den Jackson-Pfad direkt. Das JSON-LD-Bein in (3)
 * bleibt als eigenstaendiger, aber Jackson-unabhaengiger Rio-Check bestehen.
 *
 * Zur Frage, welches Tupel-Ergebnisformat eine unforcierte RemoteRepositoryManager-
 * Session tatsaechlich anfordert (Review-Fairness-Qualifikation: moeglicherweise
 * Binary statt JSON): per javap auf rdf4j-http-client-5.3.1.jar statisch verifiziert
 * -- der `SPARQLProtocolSession`-Konstruktor setzt `preferredTQRFormat =
 * TupleQueryResultFormat.SPARQL` (SPARQL/XML, MIME application/sparql-results+xml).
 * `RDF4JProtocolSession` (von `HTTPRepository`/`RemoteRepositoryManager` genutzt) ruft
 * nur `super(...)` und ueberschreibt das NICHT. Der Default ist also weder Binary noch
 * JSON, sondern XML. `HTTPRepository.getPreferredTupleQueryResultFormat()` ist dafuer
 * KEIN brauchbarer Live-Zugriff: die Methode liefert nur `HTTPRepository`s eigenes
 * Feld `tupleFormat` zurueck, das ohne expliziten `setPreferredTupleQueryResultFormat`-
 * Aufruf `null` bleibt (der Session-interne Default wird nie hochgereicht); ein
 * Live-Read des tatsaechlichen Session-Feldwerts waere nur per Reflection auf ein
 * privates Feld moeglich -- bewusst NICHT gemacht (Kontorsion), stattdessen hier die
 * statisch per javap gesicherte Antwort dokumentiert und beim Lauf unten ausgegeben.
 *
 * Aufruf: mvn -f spikes/rdf4j5-boot13/pom.xml compile exec:java [-Dgate.server=<url>]
 * Exit 0 = gruen, Exception/Exit != 0 = rot (Meldung ist das Ergebnis).
 */
public final class Rdf4j5Boot13Check {

    private static final String REPO_ID = "gate_e";

    private Rdf4j5Boot13Check() { }

    public static void main(final String[] args) throws Exception {
        String server = System.getProperty("gate.server", "http://localhost:8090/rdf4j-server");
        System.out.println("jackson-databind : " + locate("com.fasterxml.jackson.databind.ObjectMapper"));
        System.out.println("jackson-core 2.10+ marker (JsonFactoryBuilder): "
                + locate("com.fasterxml.jackson.core.JsonFactoryBuilder"));
        System.out.println("httpclient       : " + locate("org.apache.http.impl.client.HttpClientBuilder"));
        System.out.println("slf4j-api        : " + locate("org.slf4j.LoggerFactory"));
        System.out.println("rdf4j-model      : " + locate("org.eclipse.rdf4j.model.impl.SimpleValueFactory"));

        RemoteRepositoryManager manager = new RemoteRepositoryManager(server);
        manager.init();
        System.out.println("(1) RemoteRepositoryManager.init OK, repos=" + manager.getAllRepositories().size());

        manager.addRepositoryConfig(
                new RepositoryConfig(REPO_ID, new SailRepositoryConfig(new MemoryStoreConfig())));
        Repository repo = manager.getRepository(REPO_ID);
        try (RepositoryConnection conn = repo.getConnection()) {
            String turtle = "@prefix ex: <http://ajan.gate/> .\n"
                    + "ex:a ex:p ex:b .\n"
                    + "ex:b ex:q \"literal\" .\n";
            conn.add(new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)), "", RDFFormat.TURTLE);
        }
        System.out.println("(1b) upload OK");

        // (2) exakt der Aufruf, an dem Spike A gescheitert ist
        Model result = Repositories.graphQuery(repo,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }", r -> QueryResults.asModel(r));
        if (result.size() != 2) {
            throw new IllegalStateException("(2) graphQuery: expected 2 statements, got " + result.size());
        }
        System.out.println("(2) Repositories.graphQuery OK, statements=" + result.size());

        // (3) Rio round-trip Turtle und JSON-LD. JSON-LD ist in RDF4J 5 ein
        // hasmac/JSON-P-Pfad, KEIN Jackson-Pfad (s. Klassen-Javadoc oben) -- bleibt
        // als eigenstaendiger Rio-Check bestehen, beweist aber nichts ueber Jackson.
        roundTrip(result, RDFFormat.TURTLE);
        roundTrip(result, RDFFormat.JSONLD);

        // (4) Repositories.tupleQuery mit dem unforcierten Default-Ergebnisformat der
        // Session. Statisch per javap verifiziert (s. Klassen-Javadoc): der Default ist
        // SPARQL/XML, nicht Binary, nicht JSON -- dieses Bein beruehrt Jackson daher
        // voraussichtlich NICHT; es prueft trotzdem den vom Produktcode tatsaechlich
        // genutzten SELECT-Pfad (Repositories.tupleQuery / RepositoryConnection).
        System.out.println("(4) note: Default-TupleQueryResultFormat der RDF4JProtocolSession laut javap "
                + "(rdf4j-http-client-5.3.1.jar, SPARQLProtocolSession-Konstruktor) = SPARQL/XML "
                + "(application/sparql-results+xml); kein oeffentlicher Live-Getter ohne Reflection erreichbar.");
        List<BindingSet> defaultRows = Repositories.tupleQuery(repo,
                "SELECT * WHERE { ?s ?p ?o }", QueryResults::asList);
        if (defaultRows.size() != 2) {
            throw new IllegalStateException("(4) tupleQuery: expected 2 rows, got " + defaultRows.size());
        }
        System.out.println("(4) Repositories.tupleQuery (Default-Format) OK, rows=" + defaultRows.size());

        // (5) erzwungener SPARQL-Results-JSON-Roundtrip -- DAS ist der tatsaechliche
        // Jackson-Pfad im RDF4J-5-Client (AbstractSPARQLJSONWriter/-Parser laden laut
        // javap com.fasterxml.jackson.core.{JsonFactoryBuilder,StreamWriteFeature,
        // StreamReadFeature}, alle >= Jackson 2.10, fehlend in 2.6.6).
        checkForcedSparqlJson(repo);

        manager.removeRepository(REPO_ID);
        manager.shutDown();
        System.out.println("GATE E: GREEN");
    }

    private static void checkForcedSparqlJson(final Repository repo) throws Exception {
        ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
        try (RepositoryConnection conn = repo.getConnection()) {
            try (TupleQueryResult tqr = conn.prepareTupleQuery("SELECT * WHERE { ?s ?p ?o }").evaluate()) {
                QueryResultIO.writeTuple(tqr, TupleQueryResultFormat.JSON, jsonOut);
            }
        }
        System.out.println("(5a) QueryResultIO.writeTuple(..., SPARQL-Results-JSON, ...) OK, bytes="
                + jsonOut.size());

        TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
        QueryResultIO.parseTuple(new ByteArrayInputStream(jsonOut.toByteArray()), TupleQueryResultFormat.JSON,
                builder, SimpleValueFactory.getInstance());
        List<BindingSet> parsedRows = QueryResults.asList(builder.getQueryResult());
        if (parsedRows.size() != 2) {
            throw new IllegalStateException("(5b) SPARQL-Results-JSON round-trip: expected 2 rows, got "
                    + parsedRows.size());
        }
        System.out.println("(5b) QueryResultIO.parseTuple(..., SPARQL-Results-JSON, ...) OK, rows="
                + parsedRows.size());
    }

    private static void roundTrip(final Model model, final RDFFormat format) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(model, out, format);
        Model back = Rio.parse(new ByteArrayInputStream(out.toByteArray()), "", format);
        if (!Models.isomorphic(model, back)) {
            throw new IllegalStateException("(3) " + format.getName()
                    + " round-trip not isomorphic to original model (structurally different, not just count)");
        }
        System.out.println("(3) Rio round-trip OK for " + format.getName());
    }

    private static String locate(final String className) {
        try {
            return Class.forName(className).getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (ClassNotFoundException | NullPointerException e) {
            return "<not resolvable: " + e + ">";
        }
    }
}
