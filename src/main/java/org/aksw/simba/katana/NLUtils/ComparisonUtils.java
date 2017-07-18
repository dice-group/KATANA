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

import edu.stanford.nlp.ie.util.RelationTriple;
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

	public void addLabels(List<RelationTriple> triplesFromNL, Map<RDFProperty, ArrayList<RDFResource>> map) {

		for (RelationTriple triple : triplesFromNL) {
			if (map.keySet().contains(triple.relationLemmaGloss())) {
				List<RDFResource> res = map.get(triple.relationLemmaGloss());
				String[] subject = triple.subjectLemmaGloss().split(" ");
				String[] object = triple.objectLemmaGloss().split(" ");
				System.out.println("Searching for match");
				this.searchElement(subject, res);
				this.searchElement(object, res);
			}
		}

	}

	public void searchElement(String[] arr, List<RDFResource> res) {
		for (String nlEle : arr) {
			for (RDFResource resource : res) {
				if (resource.getLabels().contains(nlEle)) {
					
					System.out.println(resource.getKbLabel() + ":" + nlEle);
				}
			}
		}
	}

	public static void main(String[] args) {

		File inputFile = new File("src/main/resources/abc.txt");
		String text = null;
		try {
			text = Files.toString(inputFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ComparisonUtils cu = new ComparisonUtils();
		// cu.addLabels(text, cu.kbPropResourceMap);
		NLUtils nlp = new NLUtils();
		Annotation doc = nlp.getAnnotatedText(text);
		//List<RelationTriple> triplesFromNL = nlp.getTriplesfromNL(nlp.filterSentences(doc, cu.kbPropResourceMap));
		//cu.addLabels(triplesFromNL, cu.kbPropResourceMap);
		nlp.corefResoultion(doc);
	}
}
