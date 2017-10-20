package org.aksw.simba.katana.algorithm;

import org.aksw.simba.katana.KBUtils.KBHandler;
import org.aksw.simba.katana.nlsimulator.BENGALHandler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class KatanaAlgo {

	Model modelKB;
	Model modelDocument;
	BENGALHandler  nlDataHandler;
	KBHandler  kbDataHandler;
	Double scoreBetweenCBD = 0.0;
	

	public KatanaAlgo() {
		// TODO Auto-generated constructor stub
		this.modelKB = ModelFactory.createDefaultModel();
		this.modelDocument = ModelFactory.createDefaultModel();
		this. nlDataHandler = new BENGALHandler();
		this.kbDataHandler = new KBHandler();
	}
	
	
	   
}
