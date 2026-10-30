package de.dfki.asr.ajan.pluginsystem.opcuaplugin.extensions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Map;
import java.util.TreeMap;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.reflect.RDFBeanInfo;
import org.cyberborean.rdfbeans.reflect.RDFProperty;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.testng.annotations.Test;

/**
 * The OPCUA nodes share their behaviour through OPCUANode / OPCUAMethodCallNode, but RDFBeans
 * must still map every annotated field of the concrete classes.
 */
public class OPCUANodeRdfTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  /** Bean property name -> mapped predicate. */
  private static Map<String, IRI> properties(Class<?> cls) throws Exception {
    Map<String, IRI> found = new TreeMap<>();
    for (RDFProperty p : RDFBeanInfo.get(cls).getProperties()) {
      found.put(p.getPropertyDescriptor().getName(), p.getUri());
    }
    assertNotNull(RDFBeanInfo.get(cls).getSubjectProperty(), cls + ": @RDFSubject");
    return found;
  }

  private static <T> T load(Class<T> cls, Map<String, String> literals) throws Exception {
    RDFBeanInfo info = RDFBeanInfo.get(cls);
    Map<String, IRI> props = properties(cls);
    IRI subject = VF.createIRI("http://example.org/nodes#" + cls.getSimpleName());
    SailRepository repo = new SailRepository(new MemoryStore());
    repo.init();
    try (RepositoryConnection conn = repo.getConnection()) {
      conn.add(subject, RDF.TYPE, info.getRDFType());
      for (Map.Entry<String, String> e : literals.entrySet()) {
        IRI predicate = props.get(e.getKey());
        assertNotNull(predicate, cls.getSimpleName() + " maps " + e.getKey());
        conn.add(
            subject,
            predicate,
            e.getValue().equals("true") ? VF.createLiteral(true) : VF.createLiteral(e.getValue()));
      }
      return new RDFBeanManager(conn).get(subject, cls);
    } finally {
      repo.shutDown();
    }
  }

  @Test
  public void allAnnotatedFieldsAreMapped() throws Exception {
    assertEquals(
        properties(OPCUAEnableAssetNode.class).keySet().toString(),
        "[enabled, endpointUrl, label, targetBase]");
    assertEquals(
        properties(OPCUASelectJointNode.class).keySet().toString(),
        "[endpointUrl, jointId, label, targetBase]");
    assertEquals(
        properties(OPCUAStartJoiningNode.class).keySet().toString(),
        "[deselect, endpointUrl, label, targetBase]");
    assertEquals(
        properties(OPCUASetupNode.class).keySet().toString(),
        "[endpointUrl, label, targetBase, venvPath]");
  }

  @Test
  public void methodNodesDeserializeAndBuildTheirCall() throws Exception {
    OPCUAEnableAssetNode enable =
        load(
            OPCUAEnableAssetNode.class,
            Map.of("label", "Enable", "endpointUrl", "opc.tcp://h:4840", "enabled", "true"));
    assertEquals(enable.getLabel(), "Enable");
    assertEquals(enable.getEndpointUrl(), "opc.tcp://h:4840");
    assertEquals(enable.method(), "enable_asset");
    assertEquals(enable.options(), new String[] {"--enable", "true"});
    assertEquals(enable.toString(), "OPCUAEnableAssetNode (Enable)");

    OPCUASelectJointNode select =
        load(
            OPCUASelectJointNode.class,
            Map.of("label", "Select", "endpointUrl", "opc.tcp://h:1", "jointId", "J7"));
    assertEquals(select.getEndpointUrl(), "opc.tcp://h:1");
    assertEquals(select.method(), "select_joint");
    assertEquals(select.options(), new String[] {"--joint-id", "J7"});

    OPCUAStartJoiningNode start =
        load(OPCUAStartJoiningNode.class, Map.of("label", "Start", "deselect", "true"));
    assertEquals(start.method(), "start_selected_joining");
    assertEquals(start.options(), new String[] {"--deselect", "true"});

    OPCUASetupNode setup =
        load(OPCUASetupNode.class, Map.of("label", "Setup", "endpointUrl", "opc.tcp://s:2"));
    assertEquals(setup.getEndpointUrl(), "opc.tcp://s:2");
    assertEquals(setup.toString(), "OPCUASetupNode (Setup)");
  }
}
