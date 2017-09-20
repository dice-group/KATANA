package org.aksw.simba.katana.BENGAL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.simba.bengal.controller.BengalController;
import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.apache.commons.io.IOUtils;

public class DocumentGenrator {
	private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 1;
	private static final SelectorType SELECTOR_TYPE = SelectorType.PATH;
	private static final boolean USE_PARAPHRASING = true;
	private static final boolean USE_PRONOUNS = false;
	private static final boolean USE_SURFACEFORMS = true;
	private static final boolean USE_AVATAR = false;

	
	public static void main(String[] args) {
		
	

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
		BengalController.generateCorpus(new HashMap<String, String>(), "http://dbpedia.org/sparql", corpusName);

		NIFParser parser = new TurtleNIFParser();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(corpusName);
			parser.parseNIF(fin);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fin);
		}
	}

}
