package org.aksw.simba.katana.algorithm;

import junit.framework.TestCase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.List;

public class EvaluationHandlerTest extends TestCase {

    private static final String URITOLABEL = "rdfs:label";
    private EvaluationHandler handler;
    private Triple pascalTriple;
    private Triple wrongGalileoTriple, rightGalileoTriple;
    private Triple wrongEinsteinTriple, rightEinsteinTriple;

    public void setUp() throws Exception {
        Node pascal = NodeFactory.createURI("http://dbpedia.org/resource/Blaise_Pascal");
        Node label = NodeFactory.createURI(URITOLABEL);
        Node pascalLabel = NodeFactory.createLiteral("Blaise Pascal");
        Node galileo = NodeFactory.createURI("http://dbpedia.org/resource/Galileo_Galilei");
        Node galileoLabel = NodeFactory.createLiteral("Galileo Galilei");
        Node einstein = NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein");
        Node einsteinLabel = NodeFactory.createLiteral("Albert Einstein");

        pascalTriple = new Triple(pascal, label, pascalLabel);
        wrongGalileoTriple = new Triple(galileo, label, einsteinLabel);
        rightGalileoTriple = new Triple(galileo, label, galileoLabel);
        wrongEinsteinTriple = new Triple(einstein, label, galileoLabel);
        rightEinsteinTriple = new Triple(einstein, label, einsteinLabel);

        List<Triple> labelGuesses = new ArrayList<>(3);
        labelGuesses.add(pascalTriple);
        labelGuesses.add(wrongGalileoTriple);
        labelGuesses.add(wrongEinsteinTriple);
        List<Triple> correctLabels = new ArrayList<>(3);
        correctLabels.add(pascalTriple);
        correctLabels.add(rightGalileoTriple);
        correctLabels.add(rightEinsteinTriple);
        handler = new EvaluationHandler(labelGuesses, correctLabels);
    }

    public void testCalculateMistakes() {
        List<Triple> result = handler.calculateMistakes();
        assertEquals("There should be 2 mistakes (the two wrong guesses)", 2, result.size());
        if (!result.isEmpty()) {
            assertSame(wrongGalileoTriple, result.get(0));
            assertSame(wrongEinsteinTriple, result.get(Math.min(1, result.size() - 1)));
        }
    }

    public void testGetMissedLabelMatches() {
        List<Triple> result = handler.getMissedLabelMatches();
        assertEquals("There should be 2 missed labels (the two wrong guesses)", 2, result.size());
        if (!result.isEmpty()) {
            assertSame(rightGalileoTriple, result.get(0));
            assertSame(rightEinsteinTriple, result.get(Math.min(1, result.size() - 1)));
        }
    }

    public void testCalculateAccuracy() {
        assertEquals("1 out of 3 was right", 33, Math.round(handler.calculateAccuracy() * 100));
    }
}