package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class MappingHelper {

    private MappingHelper() {

    }

    public static String getMappingString(AgentTaskInformation agentInfo, URI mappingURI) {
        TripleDataBase domainDB = agentInfo.getDomainTDB();
        Repository repo = domainDB.getInitializedRepository();

        String mappingString = null;
        Model mapping = null;
        StringWriter writer = new StringWriter();
        try {
            mapping = MappingUtil.getTriplesMaps(repo, mappingURI);

            TurtleWriterFactory turtleWriterFactory = new TurtleWriterFactory();
            TurtleWriter turtleWriter = (TurtleWriter) turtleWriterFactory.getWriter(writer);

            turtleWriter.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
            turtleWriter.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);

            try(RepositoryConnection conn = repo.getConnection()){
                Iterable<Namespace> namespaces = conn.getNamespaces();
                for (Namespace namespace : namespaces) {
                    mapping.setNamespace(namespace.getPrefix(), namespace.getName());
                }
            }
            Rio.write(mapping, turtleWriter);

            mappingString = writer.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return mappingString;
    }
}
