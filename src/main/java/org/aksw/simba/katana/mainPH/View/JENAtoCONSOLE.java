package org.aksw.simba.katana.mainPH.View;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;

public abstract class JENAtoCONSOLE {

    public static void print(List<Triple> triples) {
        print(triples, 50);
    }

    public static void print(List<Triple> triples, int limit) {
        int counter = 0;
        for (Triple triple : triples) {
            if (counter >= limit)
                break;
            System.out.println(print(triple.getSubject()) + " - " + print(triple.getPredicate()) + " - " + print(triple.getObject()));
            counter++;
        }
        if (triples.size() > limit) {
            System.out.println("Not all triples were printed, because they are too many. Unprinted: " + (triples.size() - limit));
        }
    }

    private static String print(Node node) {
        if (node.isBlank()) {
            return "[Blank node " + node.getBlankNodeId() + "|" + node.getBlankNodeLabel() + "]";
        }
        if (node.isLiteral()) {
            if (node.getLiteralDatatype().toString().endsWith("int")) {
                return node.getLiteral().getLexicalForm().toString();
            } else if (node.getLiteralDatatype().toString().endsWith("double")) {
                return node.getLiteral().getLexicalForm() + "d";
            }
            return "\"" + node.getLiteral().getLexicalForm() + "\"";
        }
        if (node.isURI()) {
            return "<" + node.getURI() + ">";
        }

        return node.toString();
    }

    public static void print(Model model) {
        print(model, 50);
    }

    public static void print(Model model, int limit) {
        StmtIterator iterator = model.listStatements();
        int counter;
        for (counter = 0; counter < limit && iterator.hasNext(); counter++) {
            Statement statement = iterator.nextStatement();
            System.out.println(print(statement.getSubject().asNode()) + " - " + print(statement.getPredicate().asNode()) + " - " + print(statement.getObject().asNode()));
        }
        if (counter >= limit) {
            System.out.println("Not the whole model  was printed, because they are too big: " + model.size());
        }
    }

    public static void printLabel(Model model) {
        StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
            public boolean selects(Statement s) {
                return (s.getPredicate().equals(RDFS.label));
            }
        });
        if (iter.hasNext()) {
            Statement st = iter.next();
            System.out.println("Label for " + print(st.getSubject().asNode()) + " (model) is " + print(st.getObject().asNode()));
        } else {
            System.out.println("No label found in the model:");
            print(model, 1);
        }
    }
}
