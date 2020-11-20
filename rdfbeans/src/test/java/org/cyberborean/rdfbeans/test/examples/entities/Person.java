package org.cyberborean.rdfbeans.test.examples.entities;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.cyberborean.rdfbeans.annotations.RDFContainer.ContainerType;

@RDFNamespaces({ "foaf = http://xmlns.com/foaf/0.1/",
		"persons = http://rdfbeans.viceversatech.com/test-ontology/persons/" })
@RDFBean("foaf:Person")
public class Person {

	private String id;
	private String name;
	private String email;
	private URI homepage;
	private Date birthday;
	private String[] nick;
	private Collection<Person> knows;

	/** Default no-arg constructor */
	public Person() {
	}

	@RDFSubject(prefix = "persons:")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@RDF("foaf:name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@RDF("foaf:mbox")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@RDF("foaf:homepage")
	public URI getHomepage() {
		return homepage;
	}

	public void setHomepage(URI homepage) {
		this.homepage = homepage;
	}

	@RDF("foaf:birthday")
	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@RDF("foaf:nick")
	@RDFContainer(ContainerType.ALT)
	public String[] getNick() {
		return nick;
	}

	public void setNick(String[] nick) {
		this.nick = nick;
	}

	public String getNick(int i) {
		return nick[i];
	}

	public void setNick(int i, String nick) {
		this.nick[i] = nick;
	}

	@RDF("foaf:knows")
	public Collection<Person> getKnows() {
		return knows;
	}

	public void setKnows(Collection<Person> knows) {
		this.knows = knows;
	}
}
