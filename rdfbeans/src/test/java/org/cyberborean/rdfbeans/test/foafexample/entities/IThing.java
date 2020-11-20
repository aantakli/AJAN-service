package org.cyberborean.rdfbeans.test.foafexample.entities;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

/**
 * IThing.
 *
 * @author alex
 *
 */
@RDFNamespaces({
	"owl = http://www.w3.org/2002/07/owl#", 
	"foaf = http://xmlns.com/foaf/0.1/"
	})
@RDFBean("owl:Thing")
public interface IThing {

	/**
	 * @return the uri
	 */
	@RDFSubject
	String getUri();

	/**
	 * @param uri the uri to set
	 */
	void setUri(String uri);

	/**
	 * @return the name
	 */
	@RDF("foaf:name")
	String getName();

	/**
	 * @param name the name to set
	 */
	void setName(String name);

	/**
	 * @return the homepage
	 */
	@RDF("foaf:homepage")
	IDocument getHomepage();

	/**
	 * @param homepage the homepage to set
	 */
	void setHomepage(IDocument homepage);

}