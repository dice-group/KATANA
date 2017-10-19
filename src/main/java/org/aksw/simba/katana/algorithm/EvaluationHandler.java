package org.aksw.simba.katana.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class EvaluationHandler {

	List<Model> kbCBDList;
	Model modelKB;

	public EvaluationHandler() {

		this.modelKB = ModelFactory.createDefaultModel();

		this.kbCBDList = new ArrayList<Model>();
	}

}

/*
 * public double calculateAccuracy(List<Triple> resultKatana) { double
 * numberOfCorrectResults = 0; double totalNumberofLabels = resultKatana.size();
 * for (Triple triple : resultKatana) { if (triplesLabelKB.contains(triple))
 * numberOfCorrectResults++; }
 * 
 * return (numberOfCorrectResults / totalNumberofLabels);
 * 
 * }
 * 
 */
