package org.aksw.simba.katana.NLUtils;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLParser {

	protected StanfordCoreNLP pipeline;

	public NLParser() {
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma ");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<CoreMap> getSentence(String documentText) {

		Annotation document = new Annotation(documentText);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		return sentences;
	}

	

}
