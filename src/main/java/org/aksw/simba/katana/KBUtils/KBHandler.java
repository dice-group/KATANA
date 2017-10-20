package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

public class KBHandler {
	SparqlHandler queryHandler;
	List<Triple> triplesfromKB;
	List<Triple> triplesLabelKB;
	List<Model> kbCBDList;

	List<Statement> correctLabels;

	public KBHandler() {
		// only resource from one class that have functional properties
		this.triplesfromKB = new ArrayList<Triple>();
		this.queryHandler = new SparqlHandler();
		this.kbCBDList = new ArrayList<Model>();
		this.correctLabels = new ArrayList<Statement>();
		this.triplesfromKB = queryHandler.getFunctionalPropertyResources("http://dbpedia.org/ontology/Person");
		
		this.getCBDofResource();

	}

	public void getCBDofResource() {
		List<Triple> forgottenLabel = new LinkedList<Triple>(this.triplesfromKB);
		// selecting random 5 triples
		Collections.shuffle(forgottenLabel);
		forgottenLabel.subList(0, 5);

		for (Triple triple : forgottenLabel) {
			kbCBDList.add(this.queryHandler.getCBD(triple.getSubject().getURI()));
		}

		for (Model modelKB : this.kbCBDList) {
			// saving the correct label info
			StmtIterator iter = modelKB.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
				public boolean selects(Statement s) {
					return (s.getPredicate().equals(RDFS.label));
				}
			});
			while (iter.hasNext()) {
				this.correctLabels.add(iter.next());
			}
			// Forgetting all the triples with label info
			modelKB.removeAll(null, RDFS.label, null);
		}
	}

	public List<Triple> getTriplesfromKB() {
		return triplesfromKB;
	}

	public void setTriplesfromKB(List<Triple> triplesfromKB) {
		this.triplesfromKB = triplesfromKB;
	}

	public List<Triple> getTriplesLabelKB() {
		return triplesLabelKB;
	}

	public void setTriplesLabelKB(List<Triple> triplesLabelKB) {
		this.triplesLabelKB = triplesLabelKB;
	}

	public List<Model> getKbCBDList() {
		return kbCBDList;
	}

	public void setKbCBDList(List<Model> kbCBDList) {
		this.kbCBDList = kbCBDList;
	}

	public List<Statement> getCorrectLabels() {
		return correctLabels;
	}

	public void setCorrectLabels(List<Statement> correctLabels) {
		this.correctLabels = correctLabels;
	}
}
