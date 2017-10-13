package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.aksw.simba.katana.model.RDFTriple;
import org.aksw.simba.katana.nlsimulator.DocumentTripleExtractor;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

public class EvaluationHandler {
	private static final boolean USE_AVATAR = false;
	private static final SelectorType SELECTOR_TYPE = SelectorType.STAR;
	private static final boolean USE_PARAPHRASING = true;
	private static final boolean USE_PRONOUNS = false;
	private static final boolean USE_SURFACEFORMS = true;
	private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 100;
	List<Triple> triplesfromKB;
	List<Triple> triplesLabelKB;
	Model modelKB;
	Model modelDocument;
	SparqlHandler queryHandler;
	List<Statement> correctLabels;

	public EvaluationHandler() {

		this.queryHandler = new SparqlHandler();
		this.triplesfromKB = queryHandler.getFunctionalPropertyResources("http://dbpedia.org/ontology/Person");
		this.modelKB = ModelFactory.createDefaultModel();
		this.modelDocument = ModelFactory.createDefaultModel();
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

		//Forgetting all the triples with label info
		modelKB.removeAll(null, RDFS.label, null);
		// System.out.println("Afterrrrrrrrr");
		// modelKB.write(System.out, "TURTLE");

	}

	public double calculateAccuracy(List<Triple> resultKatana) {
		double numberOfCorrectResults = 0;
		double totalNumberofLabels = resultKatana.size();
		for (Triple triple : resultKatana) {
			if (triplesLabelKB.contains(triple))
				numberOfCorrectResults++;
		}

		return (numberOfCorrectResults / totalNumberofLabels);

	}

	public void getDocumentCBD() {
		String typeSubString = "";
		if (USE_AVATAR) {
			typeSubString = "summary";
		} else {
			switch (SELECTOR_TYPE) {
			case STAR: {
				typeSubString = "star";
				break;
			}
			case HYBRID: {
				typeSubString = "hybrid";
				break;
			}
			case PATH: {
				typeSubString = "path";
				break;
			}
			case SIM_STAR: {
				typeSubString = "sym";
				break;
			}
			}
		}
		String corpusName = "bengal_" + typeSubString + "_" + (USE_PRONOUNS ? "pronoun_" : "")
				+ (USE_SURFACEFORMS ? "surface_" : "") + (USE_PARAPHRASING ? "para_" : "")
				+ Integer.toString(DEFAULT_NUMBER_OF_DOCUMENTS) + ".ttl";

		DocumentTripleExtractor dc = new DocumentTripleExtractor();
		dc.generateCorpus(new HashMap<String, String>(), "http://dbpedia.org/sparql", corpusName);
		this.modelDocument.add(dc.getModel());
	}

}
