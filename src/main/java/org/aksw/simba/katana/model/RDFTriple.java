package org.aksw.simba.katana.model;

public class RDFTriple {

	public RDFTriple(RDFResource subject, RDFProperty predicate, RDFResource object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	RDFResource subject;
	RDFProperty predicate;
	RDFResource object;

	public RDFResource getSubject() {
		return subject;
	}

	public void setSubject(RDFResource subject) {
		this.subject = subject;
	}

	public RDFProperty getPredicate() {
		return predicate;
	}

	public void setPredicate(RDFProperty predicate) {
		this.predicate = predicate;
	}

	public RDFResource getObject() {
		return object;
	}

	public void setObject(RDFResource object) {
		this.object = object;
	}
}
