package org.aksw.simba.katana.algorithm.copy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.nlsimulator.DocumentTripleExtractor;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
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

	List<Model> kbCBDList;
	Model modelKB;
	Model modelDocument;

	List<Statement> correctLabels;

	public EvaluationHandler() {

		this.modelKB = ModelFactory.createDefaultModel();
		this.modelDocument = ModelFactory.createDefaultModel();
		this.correctLabels = new ArrayList<Statement>();
		this.kbCBDList = new ArrayList<Model>();
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

	/*
	 * public double calculateAccuracy(List<Triple> resultKatana) { double
	 * numberOfCorrectResults = 0; double totalNumberofLabels = resultKatana.size();
	 * for (Triple triple : resultKatana) { if (triplesLabelKB.contains(triple))
	 * numberOfCorrectResults++; }
	 * 
	 * return (numberOfCorrectResults / totalNumberofLabels);
	 * 
	 * }
	 * 
	 */

}
