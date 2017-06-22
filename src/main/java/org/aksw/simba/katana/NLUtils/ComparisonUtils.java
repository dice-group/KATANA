package org.aksw.simba.katana.NLUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.model.RDFProperty;

import com.google.common.io.Files;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class ComparisonUtils {
	SparqlHandler queryHandler;
	NLParser nlHandler;

	public ComparisonUtils() {

		this.queryHandler = new SparqlHandler();
		this.nlHandler = new NLParser();

	}

	public List<CoreMap> filterSentences(String text) {
		List<CoreMap> sentences = nlHandler.getSentence(text);
		List<CoreMap> nlText = new ArrayList<CoreMap>();
		ArrayList<RDFProperty> propertiesList = queryHandler.getPropertyList();
		System.out.println(propertiesList);
		// Random addition for check
		propertiesList.add(new RDFProperty("http://dbpedia.org/page/Berlin", "Berlin"));
		for (RDFProperty ele : propertiesList) {
			for (CoreMap sentence : sentences) {
				if (sentence.get(CoreAnnotations.TextAnnotation.class).contains(ele.getLabel())) {
					nlText.add(sentence);
				}

			}
		}
		return nlText;
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
		ComparisonUtils slem = new ComparisonUtils();
		System.out.println(slem.filterSentences(text));
	}
}
