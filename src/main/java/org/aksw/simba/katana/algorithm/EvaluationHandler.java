package org.aksw.simba.katana.algorithm;

import org.apache.jena.graph.Triple;

import java.util.List;
import java.util.stream.Collectors;

public class EvaluationHandler {

    private List<Triple> labelGuesses;
    private List<Triple> correctLabels;

    public EvaluationHandler(List<Triple> labelGuesses, List<Triple> correctLabels) {
        this.labelGuesses = labelGuesses;
        this.correctLabels = correctLabels;
    }

    /**
     * compares the labelGuesses with the correctLabels and finds mistaces
     *
     * @return a List of all wrong label guesses from labelGuesses
     */
    List<Triple> calculteMistaces() {
        return labelGuesses.stream().filter(triple -> triple.getPredicate().getURI().equals("rdfs:label") && !correctLabels.stream().anyMatch(rightTriple -> rightTriple.getSubject().equals(triple.getSubject()) && rightTriple.getObject().equals(triple.getObject()))).collect(Collectors.toList());
    }

    /**
     * compares the correctLabels with the labelGuesses and finds Triples in correctLabels, that don't appear in labelGuesses
     *
     * @return a List of all missing labels from correctLabels in labelGuesses
     */
    List<Triple> getMissedLabelMatches() {
        return correctLabels.stream().filter(triple -> triple.getPredicate().getURI().equals("rdfs:label") && !labelGuesses.stream().anyMatch(guessTriple -> guessTriple.getSubject().equals(triple.getSubject()) && guessTriple.getObject().equals(triple.getObject()))).collect(Collectors.toList());
    }

    /**
     * Calculates the accuracy of the given dataset
     *
     * @return the accuracy between {@code 1} (good quality) and {@code 0} (nothing is correct)
     */
    public double calculateAccuracy() {
        return (2 - (calculteMistaces().size() / labelGuesses.size()) - (getMissedLabelMatches().size() / correctLabels.size())) / 2d;
    }
}
