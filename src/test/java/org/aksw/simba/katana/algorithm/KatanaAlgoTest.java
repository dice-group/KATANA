package org.aksw.simba.katana.algorithm;

import junit.framework.TestCase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.List;

public class KatanaAlgoTest extends TestCase {

    private static final String URITOLABEL = "rdfs:label";
    private KatanaAlgo handler;

    private Node pascal = NodeFactory.createURI("http://dbpedia.org/resource/Blaise_Pascal");
    private Node galileo = NodeFactory.createURI("http://dbpedia.org/resource/Galileo_Galilei");
    private Node einstein = NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein");
    private Triple pascalWasChrist, galileoWasChrist;
    private Triple pascalBorn, galileoBorn;
    private Triple pascalTriple, galileoTriple;

    public void setUp() throws Exception {
        super.setUp();

        Node label = NodeFactory.createURI(URITOLABEL);
        Node relation = NodeFactory.createURI("http://dbpedia.org/dbo/related");
        Node born = NodeFactory.createURI("http://dbpedia.org/dbo/born");
        Node christ = NodeFactory.createURI("http://dbpedia.org/resource/Christ");
        pascalWasChrist = new Triple(pascal, relation, christ);
        galileoWasChrist = new Triple(galileo, relation, christ);
        pascalBorn = new Triple(pascal, born, NodeFactory.createLiteral("19.06.1623"));
        galileoBorn = new Triple(galileo, born, NodeFactory.createLiteral("15.02.1564"));
        pascalTriple = new Triple(pascal, label, NodeFactory.createLiteral("Blaise Pascal"));
        galileoTriple = new Triple(galileo, label, NodeFactory.createLiteral("Blaise Pascal"));

        List<Triple> triplesFromKB = new ArrayList<>(5);
        triplesFromKB.add(pascalBorn);
        triplesFromKB.add(galileoBorn);
        triplesFromKB.add(pascalWasChrist);
        triplesFromKB.add(galileoWasChrist);
        triplesFromKB.add(new Triple(einstein, NodeFactory.createURI("http://dbpedia.org/dbo/profession"), NodeFactory.createURI("http://dbpedia.org/resource/scientist")));
        List<Node> canidates = new ArrayList<>(3);
        canidates.add(pascal);
        canidates.add(galileo);
        //canidates.add(einstein);
        List<List<Triple>> knowledgeLabelExtraction = new ArrayList<>(1);
        List<Triple> i = new ArrayList<>(3);
        i.add(pascalTriple);
        i.add(pascalWasChrist);
        i.add(pascalBorn);
        knowledgeLabelExtraction.add(i);
        List<Triple> ii = new ArrayList<>(3);
        ii.add(galileoTriple);
        ii.add(new Triple(einstein, NodeFactory.createURI("http://dbpedia.org/dbo/profession"), NodeFactory.createURI("http://dbpedia.org/resource/scientist")));

        handler = new KatanaAlgo(triplesFromKB, canidates, knowledgeLabelExtraction);
    }

    public void testVerify() {
        assertEquals("Input should be valid", true, handler.verify());
    }

    public void testMatchLabelsKATANAv1() {
        List<Triple> result = handler.matchLabelsKATANAv1();
        assertEquals("2 candidates - 2 guesses", 3, result.size());
        if (!result.isEmpty()) {
            assertEquals("Pascal is obvious :)", pascalTriple, result.get(0));
            assertEquals("Second guess has no evidencies :/", galileoTriple, result.get(Math.min(1, result.size() - 1)));
        }
    }
}