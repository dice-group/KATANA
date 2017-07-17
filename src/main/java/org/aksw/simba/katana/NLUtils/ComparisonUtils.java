package org.aksw.simba.katana.NLUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.model.RDFProperty;
import org.aksw.simba.katana.model.RDFResource;

import com.google.common.io.Files;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ComparisonUtils {
	SparqlHandler queryHandler;
	NLUtils nlHandler;
	Map<RDFProperty, ArrayList<RDFResource>> kbPropResourceMap;

	public ComparisonUtils() {

		this.queryHandler = new SparqlHandler();
		this.nlHandler = new NLUtils();
		this.kbPropResourceMap = queryHandler.getPropertyResourceMap();

	}

	public List<CoreMap> addLabels(String text, Map<RDFProperty, ArrayList<RDFResource>> map) {
		Annotation document = new Annotation(text);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (RDFProperty prop : map.keySet()) {
			for (CoreMap sentence : sentences) {
				String sen = sentence.get(CoreAnnotations.TextAnnotation.class);
				if (sen.contains(prop.getLabel())) {
					ArrayList<RDFResource> resourceList = map.get(prop);
					for (RDFResource ele : resourceList) {
						// Implement Lemma search between KB label and sentence
					}
				}

			}
		}
		return sentences;

	}

	public static void main(String[] args) {

		File inputFile = new File("src/main/resources/abc.txt");
		String text = null;
		try {
			text = Files.toString(inputFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
