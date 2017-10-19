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
	Model modelKB;
	List<Statement> correctLabels;

	

	public KBHandler() {
		// TODO Auto-generated constructor stub
		this.triplesfromKB = queryHandler.getFunctionalPropertyResources("http://dbpedia.org/ontology/Person");
		this.queryHandler = new SparqlHandler();
		this.correctLabels = new ArrayList<Statement>();
		
	}

	public void getCBDofResource() {
		List<Triple> forgottenLabel = new LinkedList<Triple>(this.triplesfromKB);
		// selecting random 5 triples
		Collections.shuffle(forgottenLabel);
		forgottenLabel.subList(0, 5);

		for (Triple triple : forgottenLabel) {
			modelKB.add(this.queryHandler.getCBD(triple.getSubject().getURI()));
		}

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
