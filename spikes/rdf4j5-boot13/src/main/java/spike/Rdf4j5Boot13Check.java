package spike;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
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
 * Geprueft werden genau die drei Pfade, an denen der Produktcode haengt:
 *   (1) Repository-Verwaltung ueber RemoteRepositoryManager,
 *   (2) SPARQL-GraphQuery ueber Repositories.graphQuery  <-- die Spike-A-Bruchstelle,
 *   (3) Rio-Serialisierung Turtle UND JSON-LD            <-- der Jackson-Pfad.
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

        // (3) Rio round-trip Turtle und JSON-LD (JSON-LD zieht Jackson)
        roundTrip(result, RDFFormat.TURTLE);
        roundTrip(result, RDFFormat.JSONLD);

        manager.removeRepository(REPO_ID);
        manager.shutDown();
        System.out.println("GATE E: GREEN");
    }

    private static void roundTrip(final Model model, final RDFFormat format) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(model, out, format);
        Model back = Rio.parse(new ByteArrayInputStream(out.toByteArray()), "", format);
        if (back.size() != model.size()) {
            throw new IllegalStateException("(3) " + format.getName() + " round-trip lost statements: "
                    + model.size() + " -> " + back.size());
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
