package org.aksw.simba.katana.NLUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aksw.simba.katana.model.RDFProperty;

import com.google.common.io.Files;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class NLParser {

	protected StanfordCoreNLP pipeline;

	public NLParser() {
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma,ner, depparse,parse,natlog,openie");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<CoreMap> getSentence(String documentText) {

		Annotation document = new Annotation(documentText);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		return sentences;
	}

	public void getTriplesfromNL(String text) {
		Annotation doc = new Annotation(text);
		pipeline.annotate(doc);

		List<RelationTriple> triples = new ArrayList<>();
		for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
			triples.addAll(sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class));
		}
		for (RelationTriple triple : triples) {
			System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t"
					+ triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
		}
	}

	public List<CoreMap> filterSentences(String text, ArrayList<RDFProperty> propertiesList) {
		List<CoreMap> sentences = this.getSentence(text);
		List<CoreMap> nlText = new ArrayList<CoreMap>();

		System.out.println(propertiesList);
		for (RDFProperty ele : propertiesList) {

			for (CoreMap sentence : sentences) {
				if (sentence.get(CoreAnnotations.TextAnnotation.class).contains(ele.getLabel())) {
					System.out.println("Label : " + ele.getLabel());
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
		NLParser nlp = new NLParser();
		nlp.getTriplesfromNL(text);

	}
}
