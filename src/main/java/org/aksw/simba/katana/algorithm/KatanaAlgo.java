package org.aksw.simba.katana.algorithm;

import org.aksw.simba.katana.KBUtils.KBEvaluationHandler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

public class KatanaAlgo {

	Model modelKB;
	Model modelDocument;
	KBEvaluationHandler kbDataHandler;
	
	

	public KatanaAlgo() {
		// TODO Auto-generated constructor stub
		this.modelKB = ModelFactory.createDefaultModel();
		this.modelDocument = ModelFactory.createDefaultModel();
		this.kbDataHandler = new KBEvaluationHandler();
	}
	
	public Double compareCBDs(Model docCBD, Model kbCBD) 
	{
		Double scoreBetweenCBD = 0.0;
		StmtIterator docStatement = docCBD.listStatements();
		StmtIterator kbStatement = kbCBD.listStatements();
		
		while(docStatement.hasNext())
		{
			while(kbStatement.hasNext()) 
			{
				
			}
		}
		
		
		return scoreBetweenCBD;
	}
	
	   
}
