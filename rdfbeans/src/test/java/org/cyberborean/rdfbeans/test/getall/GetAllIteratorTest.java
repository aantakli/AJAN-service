package org.cyberborean.rdfbeans.test.getall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.cyberborean.rdfbeans.exceptions.RDFBeanRuntimeException;
import org.cyberborean.rdfbeans.test.RDFBeansTestBase;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Test;

/** Contract tests for the {@link CloseableIteration} returned by {@code RDFBeanManager.getAll()}. */
public class GetAllIteratorTest extends RDFBeansTestBase {

	private static final String TYPE = "http://example.org/getall#Plain";
	private static final String BROKEN_TYPE = "http://example.org/getall#Broken";

	@RDFBean(TYPE)
	public static class Plain {
	}

	/** Has no default constructor, so unmarshalling must fail with an RDFBeanException. */
	@RDFBean(BROKEN_TYPE)
	public static class Broken {
		public Broken(String required) {
		}
	}

	private static IRI iri(String s) {
		return SimpleValueFactory.getInstance().createIRI(s);
	}

	@Test
	public void getAllIteratorThrowsNoSuchElementWhenExhausted() throws Exception {
		manager.getRepositoryConnection().add(iri("http://example.org/getall#one"), RDF.TYPE, iri(TYPE));
		CloseableIteration<Plain> it = manager.getAll(Plain.class);
		try {
			assertTrue(it.hasNext());
			it.next();
			assertFalse(it.hasNext());
			try {
				it.next();
				fail("next() on an exhausted iterator must throw NoSuchElementException");
			} catch (NoSuchElementException expected) {
				// contract honoured
			}
		} finally {
			it.close();
		}
	}

	@Test
	public void getAllWrapsUnmarshallingFailureInNamedRuntimeException() throws Exception {
		manager.getRepositoryConnection().add(iri("http://example.org/getall#bad"), RDF.TYPE, iri(BROKEN_TYPE));
		CloseableIteration<Broken> it = manager.getAll(Broken.class);
		try {
			assertTrue(it.hasNext());
			try {
				it.next();
				fail("unmarshalling a bean without default constructor must fail");
			} catch (RDFBeanRuntimeException expected) {
				assertTrue(expected.getCause() instanceof RDFBeanException);
			}
		} finally {
			it.close();
		}
	}
}
