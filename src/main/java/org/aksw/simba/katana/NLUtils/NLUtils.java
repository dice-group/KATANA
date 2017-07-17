package org.aksw.simba.katana.NLUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.simba.katana.model.RDFProperty;
import org.aksw.simba.katana.model.RDFResource;

import com.google.common.io.Files;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.hcoref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class NLUtils {

	protected StanfordCoreNLP pipeline;

	public NLUtils() {
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma,ner, depparse,parse,natlog,openie, mention,dcoref");
		props.put("dcoref.score", true);
		this.pipeline = new StanfordCoreNLP(props);
	}

	public Annotation getAnnotatedText(String text) {
		Annotation document = new Annotation(text);
		this.pipeline.annotate(document);
		return document;
	}

	public void corefResoultion(Annotation document) {
		
	}

	public List<CoreMap> getSentence(String documentText) {

		Annotation document = new Annotation(documentText);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		return sentences;
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

	public List<String> lemmatize(Annotation document) {
		List<String> lemmas = new LinkedList<String>();
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}
		return lemmas;
	}

	public void getTriplesfromNL(Annotation document) {

		List<RelationTriple> triples = new ArrayList<>();
		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			triples.addAll(sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class));
		}
		for (RelationTriple triple : triples) {
			System.out.println(
					triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
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
		NLUtils nlp = new NLUtils();
		Annotation doc = nlp.getAnnotatedText(text);
		

		nlp.getTriplesfromNL(doc);

	}
}
