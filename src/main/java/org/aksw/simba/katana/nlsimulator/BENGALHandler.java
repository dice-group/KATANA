package org.aksw.simba.katana.nlsimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.apache.jena.rdf.model.Model;

public class BENGALHandler {
	private static final boolean USE_AVATAR = false;
	private static final SelectorType SELECTOR_TYPE = SelectorType.STAR;
	private static final boolean USE_PARAPHRASING = true;
	private static final boolean USE_PRONOUNS = false;
	private static final boolean USE_SURFACEFORMS = true;
	private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 2;

	List<Model> docCBDList;

	public BENGALHandler() {

		this.docCBDList = new ArrayList<Model>();
		this.getDocumentCBD();
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
		this.docCBDList = dc.docCBDList;
		
		
	}

	public List<Model> getDocCBDList() {
		return docCBDList;
	}


}
