package org.aksw.simba.katana.mainPH;

import org.aksw.simba.katana.KBUtils.KBEvaluationHandler;
import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.mainPH.View.TRIPLEStoCONSOLE;
import org.apache.jena.graph.Triple;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        KBEvaluationHandler handler = new KBEvaluationHandler();

        List<Triple> model = SparqlHandler.getResources("http://dbpedia.org/ontology/Person");

        TRIPLEStoCONSOLE.print(model, 15);
        System.out.println("getTriplesFromKB()");
        TRIPLEStoCONSOLE.print(handler.getTriplesFromKB());
        handler.getCBDofResource(0.1);
        System.out.println("kbCBDList()");
        handler.getKbCBDList().forEach(m -> TRIPLEStoCONSOLE.print(m, 3));
        handler.getCorrectLabels().forEach(System.out::println);
    }
}
