package org.aksw.simba.katana.NLUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.simba.katana.model.RDFProperty;
import org.aksw.simba.katana.model.RDFResource;

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

	public void getTriplesfromNL(String text) {
		Annotation doc = new Annotation(text);
		pipeline.annotate(doc);

		List<RelationTriple> triples = new ArrayList<>();
		for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
			triples.addAll(sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class));
		}
		for (RelationTriple triple : triples) {
			System.out.println(
					triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
		}
	}

	public List<CoreMap> addLabels(String text, Map<RDFProperty, ArrayList<RDFResource>> map) {
		Annotation document = new Annotation(text);
		this.pipeline.annotate(document);
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
		NLParser nlp = new NLParser();
		nlp.getTriplesfromNL(text);

	}
}
