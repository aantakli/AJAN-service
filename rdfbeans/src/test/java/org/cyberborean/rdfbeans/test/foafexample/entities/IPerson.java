package org.cyberborean.rdfbeans.test.foafexample.entities;

import java.util.Set;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.annotations.RDFContainer.ContainerType;

/**
 * IPerson.
 *
 * @author alex
 *
 */
@RDFBean("foaf:Person")
@RDFNamespaces("persons = http://rdfbeans.viceversatech.com/test-ontology/persons/")
public interface IPerson extends IAgent {
	
	@RDFSubject(prefix = "persons:")
	String getId();

	void setId(String id);

	@RDF("foaf:nick")
	@RDFContainer(ContainerType.ALT)
	String[] getNick();

	void setNick(String[] nick);

	String getNick(int i);

	void setNick(int i, String nick);

	@RDF("foaf:knows")
	//@RDFContainer(ContainerType.SEQ)
	Set<IPerson> getKnows();
	
	void setKnows(Set<IPerson> knows);
	
	@RDF(inverseOf="foaf:knows")
	Set<IPerson> getKnownBy();
	
	@RDF("foaf:publications")
	@RDFContainer(ContainerType.SEQ)
	IDocument[] getPublications();
	
	void setPublications(IDocument[] publications); 

}