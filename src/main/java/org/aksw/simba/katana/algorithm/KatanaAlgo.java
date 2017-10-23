package org.aksw.simba.katana.algorithm;

import org.aksw.simba.katana.KBUtils.KBHandler;
import org.aksw.simba.katana.nlsimulator.BENGALHandler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

public class KatanaAlgo {

	Model modelKB;
	Model modelDocument;
	BENGALHandler  nlDataHandler;
	KBHandler  kbDataHandler;
	
	

	public KatanaAlgo() {
		// TODO Auto-generated constructor stub
		this.modelKB = ModelFactory.createDefaultModel();
		this.modelDocument = ModelFactory.createDefaultModel();
		this.nlDataHandler = new BENGALHandler();
		this.kbDataHandler = new KBHandler();
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
