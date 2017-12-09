package org.aksw.simba.katana.model;

public class RDFTriple {

	public RDFTriple(RDFResource subject, RDFProperty predicate, RDFResource object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

    private RDFResource subject;
    private RDFProperty predicate;
    private RDFResource object;

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDFTriple) {
            RDFTriple compareTriple = (RDFTriple) o;
            if ((subject == null && compareTriple.getSubject() == null) || subject.equals(compareTriple.getSubject())) {
                if ((predicate == null && compareTriple.getPredicate() == null) || predicate.equals(compareTriple.getPredicate())) {
                    if ((object == null && compareTriple.getObject() == null) || object.equals(compareTriple.getObject())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
