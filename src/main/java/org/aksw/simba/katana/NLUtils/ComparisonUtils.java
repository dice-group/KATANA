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

	public void addLabels(String text, Map<RDFProperty, ArrayList<RDFResource>> map) {
		Annotation document = new Annotation(text);

		for (RDFProperty prop : map.keySet()) {
			System.out.println("Property :  " + prop.getLabel());
			for (RDFResource res : map.get(prop)) {
				System.out.println(res.getKbLabel() + res.getLabels().toString());
			}
			System.out.println("...............");
		}

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
		ComparisonUtils cu = new ComparisonUtils();
		cu.addLabels(text, cu.kbPropResourceMap);

	}
}
