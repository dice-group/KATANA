package org.aksw.simba.katana.BENGAL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.simba.katana.model.RDFTriple;
import org.apache.commons.io.IOUtils;

public class Evaluation {

	public void createTestKB() {
		NIFParser parser = new TurtleNIFParser();
		FileInputStream fin = null;

		try {
			fin = new FileInputStream("");
			parser.parseNIF(fin);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fin);
		}
	}

	public static List<RDFTriple> pickNRandomTriples(List<RDFTriple> lst, int n) {
		List<RDFTriple> forgottenLabel = new LinkedList<RDFTriple>(lst);
		Collections.shuffle(forgottenLabel);
		return forgottenLabel.subList(0, n);
	}

}
