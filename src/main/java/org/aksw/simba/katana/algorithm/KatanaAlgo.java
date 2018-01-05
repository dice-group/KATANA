package org.aksw.simba.katana.algorithm;

import jdk.internal.jline.internal.Urls;
import org.aksw.simba.katana.KBUtils.KBEvaluationHandler;
import org.aksw.simba.katana.mainPH.View.JENAtoCONSOLE;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Concrete;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * The KATANA algorithm - the heartbeat!
 *
 * @author Philipp Heinisch
 */
public class KatanaAlgo {

	private Logger log = LogManager.getLogger(KatanaAlgo.class);
	private static final boolean PROVEVALIDITATE = true;
	private boolean executable = true;

	/**
	 * Main database, where the KATANA algorithm should run
	 */
	private List<Triple> triplesFromKB;

	/**
	 * A list of Nodes (Subjects) for that the KATANA algorithm should find out the right label!
	 */
	private List<Node> subjectsWithoutLabels;

	/**
	 * A list for many subjects, that contains foreach subject
	 * a list of Triples, that is something like the CBD auf a certain subject, that contains (at least) one ?s rdfs:label "LABEL"
	 */
	private List<List<Triple>> knowledgeLabelExtraction;

	public KatanaAlgo(List<Triple> triplesFromKB, List<Node> subjectsWithoutLabels, List<List<Triple>> knowledgeLabelExtraction) {
		if (triplesFromKB == null || subjectsWithoutLabels == null || knowledgeLabelExtraction == null) {
			log.error("It's not allowed to forward null-pointers (disable this object " + this.hashCode() + ")");
			executable = false;
		}

		this.triplesFromKB = triplesFromKB;
		this.subjectsWithoutLabels = subjectsWithoutLabels;
		this.knowledgeLabelExtraction = knowledgeLabelExtraction;

		executable = !PROVEVALIDITATE || verify();
	}

	public boolean verify() {
		int invalidCount = 0;

		if (subjectsWithoutLabels.stream().anyMatch(node -> !triplesFromKB.stream().anyMatch(triple -> triple.equals(node)))) {
			log.warn("Not all subjects in subjectsWithoutLabels appear in the triplesFromKB");
			invalidCount++;
		}

		for (List<Triple> subjectEvidences : knowledgeLabelExtraction) {
			if (subjectEvidences.isEmpty()) {
				log.warn("There is an empty triple list in knowledgeLabelExtraction!");
				invalidCount++;
			}
			Node NodeOfSubject = subjectEvidences.get(0).getSubject();
			String URIofSubject = "ERROR-Pointer " + invalidCount;
			try {
				URIofSubject = subjectEvidences.get(0).getSubject().getURI();
			} catch (Exception e) {
				log.error("Cannot determine the subject of a certain knowledgeLabelExtraction-Triple-List with " + subjectEvidences.size() + " Triples!", e);
				invalidCount++;
			}
			log.debug("Found triple list to the object " + URIofSubject);
			if (!subjectsWithoutLabels.stream().anyMatch(node -> node.equals(NodeOfSubject))) {
				log.warn("The URI " + URIofSubject + " in knowledgeLabelExtraction is not found in the subjectsWithoutLabels-List! It's useless!");
				invalidCount++;
			}

			if (subjectEvidences.stream().anyMatch(triple -> !triple.getSubject().equals(NodeOfSubject))) {
				log.warn("The triple-list regarding the " + URIofSubject + " is inconsistent! We found other subjects in this list!");
				invalidCount++;
			}

			Stream<Triple> labels = subjectEvidences.stream().filter(triple -> triple.getPredicate().getURI().equals("rdfs:label"));
			if (labels.count() == 0) {
				log.warn("There is no label for " + URIofSubject);
				invalidCount++;
			} else if (labels.count() > 1) {
				log.warn("There are more than 1 (" + labels.count() + ") labels for " + URIofSubject);
				invalidCount += labels.count() - 1;
			}
		}

		if (invalidCount == 0) {
			log.trace("The data of this instance of class is valid (" + triplesFromKB + ", " + knowledgeLabelExtraction + ")");
			return true;
		}

		log.warn("There are " + invalidCount + " invalid points in the data input!");
		return false;
	}

	public void print() {
		JENAtoCONSOLE.print(triplesFromKB, 25);
		knowledgeLabelExtraction.forEach(subjectEvidences -> JENAtoCONSOLE.print(subjectEvidences, 8));
	}

	/**
	 * A stupid approach (only for testing): picks a random label and match them to a subject
	 *
	 * @return a list of triples. Foreach subjectsWithoutLabels-Node there will be a guess of a label: ?s rdfs:label "LABELGUESS"
	 */
	public List<Triple> matchLabelsRANDOM() {
		List<Triple> ret = new ArrayList<>(subjectsWithoutLabels.size());
		Random r = new Random();

		long timestart = System.currentTimeMillis();

		for (Node subject : subjectsWithoutLabels) {
			List<Triple> selected = knowledgeLabelExtraction.get(r.nextInt(knowledgeLabelExtraction.size()));
			Optional<Triple> selectedTriple = selected.stream().filter(triple -> triple.getPredicate().getURI().equals("rdfs:label")).findFirst();

			if (selectedTriple.isPresent()) {
				log.debug("There is a match for " + subject + ": " + selectedTriple.get().getObject());
			} else {
				log.error("There is a label-triple missing in " + selected + "! Apply no label for " + subject);
			}

			selectedTriple.map(triple -> ret.add(new Triple(subject, triple.getPredicate(), triple.getObject())));
		}

		log.trace("matchLabelsRANDOM is ready and found " + ret.size() + " labels. Took " + (System.currentTimeMillis() - timestart) + "ms");

		return ret;
	}
}
