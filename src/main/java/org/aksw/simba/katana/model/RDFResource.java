package org.aksw.simba.katana.model;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.aksw.simba.katana.NLUtils.NLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RDFResource  {

    private String uri;
    private String kbLabel;
    private ArrayList<String> lemma;
    private NLUtils nl = new NLUtils();

    public RDFResource(String uri, String label) {

        this.uri = uri;
        this.kbLabel = label;
        this.lemma = new ArrayList<String>();
        this.generateLemmaFromLabel();
    }

    protected void generateLemmaFromLabel() {
		Annotation doc = nl.getAnnotatedText(kbLabel);
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemma.add(token.get(LemmaAnnotation.class));
			}
		}
	}

	public RDFResource(String uri) {

		this.uri = uri;

	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

    public ArrayList<String> getLemmas() {
		return lemma;
	}

    public String getKbLabel() {
        return kbLabel;
    }

    public void setKbLabel(String kbLabel) {
        this.kbLabel = kbLabel;
        this.generateLemmaFromLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDFResource) {
            RDFResource compareObject = (RDFResource) o;

            if ((uri == null && compareObject.getUri() == null) || uri.equals(compareObject.getUri())) {
                if ((kbLabel == null && compareObject.getKbLabel() == null) || kbLabel.equals(compareObject.getKbLabel())) {
                    if (lemma == null && compareObject.getLemmas() == null) {
                        return true;
                    }
                    Stream<String> compareLemmas = compareObject.getLemmas().stream();
                    if (lemma.stream().allMatch(l -> compareLemmas.anyMatch(l2 -> l.equals(l2)))) {
                        return true;
                    }
                    System.out.println("[WARN] The RDFResource " + uri + ", compared with a other nearly equal RDFResource has different lemmas. " + lemma.stream().count() + " <> " + compareLemmas.count() + " Lemmas.");
                }
            }
        }
        return false;
    }
}
