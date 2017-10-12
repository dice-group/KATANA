package org.aksw.simba.katana.KBUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.aksw.simba.katana.model.RDFTriple;
import org.aksw.simba.katana.nlsimulator.DocumentTripleExtractor;
import org.apache.jena.graph.Triple;

public class EvaluationHandler {
	private static final boolean USE_AVATAR = false;
	private static final SelectorType SELECTOR_TYPE = SelectorType.STAR;
	private static final boolean USE_PARAPHRASING = true;
	private static final boolean USE_PRONOUNS = false;
	private static final boolean USE_SURFACEFORMS = true;
	private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 100;
	List<Triple> triplesfromKB;
	List<Triple> triplesLabelKB;

	public static List<RDFTriple> pickNRandomTriples(List<RDFTriple> lst, int n) {
		List<RDFTriple> forgottenLabel = new LinkedList<RDFTriple>(lst);
		Collections.shuffle(forgottenLabel);
		return forgottenLabel.subList(0, n);
	}

	public EvaluationHandler() {

		SparqlHandler queryHandler = new SparqlHandler();
		this.triplesfromKB = queryHandler.getResources("http://dbpedia.org/ontology/Person");
	}

	public void generateTripleLabelList() {
		for (Triple triple : triplesfromKB) {

			triplesLabelKB.add(triple);
		}
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

	public static void main(String args[]) {
		EvaluationHandler eh = new EvaluationHandler();

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

	}
}
