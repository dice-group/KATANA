package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aksw.simba.katana.mainPH.View.TRIPLEStoCONSOLE;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

public class KBEvaluationHandler {
	private List<Triple> triplesFromKB;
	private List<Triple> triplesLabelKB;
	private List<Model> kbCBDList;

	private List<Statement> correctLabels;

	public KBEvaluationHandler() {
		// only resource from one class that have functional properties
		this.triplesFromKB = new ArrayList<>();
		this.kbCBDList = new ArrayList<>();
		this.correctLabels = new ArrayList<>();
		this.triplesFromKB = SparqlHandler.getFunctionalPropertyResources("http://dbpedia.org/ontology/Person");

		//this.getCBDofResource();

	}

	public void getCBDofResource() {
		getCBDofResource(5);
	}

	public void getCBDofResource(double numberOfLabelsToForget) {
		if (numberOfLabelsToForget < 0d || numberOfLabelsToForget > 1d) {
			throw new IllegalArgumentException("We expect a percent value, so a value between 0 and 1");
		}
		getCBDofResource((int) Math.round(triplesFromKB.size() * numberOfLabelsToForget));
	}

	public void getCBDofResource(int numberOfLabelsToForget) {
		List<Triple> forgottenLabel = new LinkedList<>(this.triplesFromKB);
		// selecting random 5 triples
		Collections.shuffle(forgottenLabel);
		if (numberOfLabelsToForget < forgottenLabel.size()) {
			forgottenLabel = forgottenLabel.subList(0, numberOfLabelsToForget);
		}

		for (Triple triple : forgottenLabel) {
			kbCBDList.add(SparqlHandler.getCBD(triple.getSubject().getURI()));
		}

		for (Model modelKB : this.kbCBDList) {
			// saving the correct label info
			TRIPLEStoCONSOLE.printLabel(modelKB);
			StmtIterator iter = modelKB.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
				public boolean selects(Statement s) {
					return (s.getPredicate().equals(RDFS.label));
				}
			});
			if (iter.hasNext()) {
				this.correctLabels.add(iter.next());
			} else {
				System.out.println("WARN: no label found in " + modelKB.listStatements().nextStatement().getString());
			}
			// Forgetting all the triples with label info
			modelKB.removeAll(null, RDFS.label, null);
		}
	}

	public List<Triple> getTriplesFromKB() {
		return triplesFromKB;
	}

	public void setTriplesFromKB(List<Triple> triplesFromKB) {
		this.triplesFromKB = triplesFromKB;
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
