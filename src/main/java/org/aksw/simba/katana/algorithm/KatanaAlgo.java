package org.aksw.simba.katana.algorithm;

import org.aksw.simba.katana.mainPH.View.JENAtoCONSOLE;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.log4j.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The KATANA algorithm - the heartbeat!
 *
 * @author Philipp Heinisch
 */
public class KatanaAlgo {

	private Logger log = LogManager.getLogger(KatanaAlgo.class);
	private static final boolean PROVEVALIDITATE = true;
	private static final String URITOLABEL = "rdfs:label";
	private boolean executable = true;

	/**
	 * Main database, where the KATANA algorithm should run
	 */
	private List<Triple> triplesFromKB;

	/**
	 * A list of Nodes (Subjects) for that the KATANA algorithm should find out the right label!
	 */
    private List<Node> candidates;

	/**
	 * A list for many subjects, that contains foreach subject
     * a list of Triples, that is something like the CBD of a certain subject, that contains (at least) one ?s rdfs:label "LABEL"
	 */
	private List<List<Triple>> knowledgeLabelExtraction;

	public KatanaAlgo(List<Triple> triplesFromKB, List<Node> subjectsWithoutLabels, List<List<Triple>> knowledgeLabelExtraction) {
		if (triplesFromKB == null || subjectsWithoutLabels == null || knowledgeLabelExtraction == null) {
			log.error("It's not allowed to forward null-pointers (disable this object " + this.hashCode() + ")");
			executable = false;
		}

		this.triplesFromKB = triplesFromKB;
        this.candidates = subjectsWithoutLabels;
		this.knowledgeLabelExtraction = knowledgeLabelExtraction;

		executable = !PROVEVALIDITATE || verify();

        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + KatanaAlgo.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + KatanaAlgo.class.getName() + "!");

	}

	/**
	 * Checks, if all 3 parameters in combination are valid
	 * triplesFromKB: Javadoc-TODO
	 * canidates: Javadoc-TODO
	 * knowledgeLabelExtraction: Javadoc-TODO
	 *
	 * @return {@code true}, if the model of the KATANA-Algo is valid, otherwise {@code false}
	 */
	public boolean verify() {
		int invalidCount = 0;

        if (candidates.stream().anyMatch(node -> !triplesFromKB.stream().anyMatch(triple -> triple.getSubject().equals(node)))) {
            log.warn("Not all subjects in candidates appear in the triplesFromKB");
            //invalidCount++;
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
            if (!candidates.stream().anyMatch(node -> node.equals(NodeOfSubject))) {
				log.warn("The URI " + URIofSubject + " in knowledgeLabelExtraction is not found in the subjectsWithoutLabels-List! It's useless!");
				invalidCount++;
			}

			if (subjectEvidences.stream().anyMatch(triple -> !triple.getSubject().equals(NodeOfSubject))) {
				log.warn("The triple-list regarding the " + URIofSubject + " is inconsistent! We found other subjects in this list!");
				invalidCount++;
			}

            long labelsCount = subjectEvidences.stream().filter(triple -> triple.getPredicate().getURI().equals(URITOLABEL)).count();
            if (labelsCount == 0) {
				log.warn("There is no label for " + URIofSubject);
				invalidCount++;
            } else if (labelsCount > 1) {
                log.warn("There are more than 1 (" + labelsCount + ") labels for " + URIofSubject);
                invalidCount += labelsCount - 1;
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
        List<Triple> ret = new ArrayList<>(candidates.size());
		if (!checkExecutionPermission())
			return ret;
		Random r = new Random();

		long timeStart = System.currentTimeMillis();

        for (Node subject : candidates) {
			List<Triple> selected = knowledgeLabelExtraction.get(r.nextInt(knowledgeLabelExtraction.size()));
			Optional<Triple> selectedTriple = selected.stream().filter(triple -> triple.getPredicate().getURI().equals(URITOLABEL)).findFirst();

			if (selectedTriple.isPresent()) {
				log.debug("There is a match for " + subject + ": " + selectedTriple.get().getObject());
			} else {
				log.error("There is a label-triple missing in " + selected + "! Apply no label for " + subject);
			}

			selectedTriple.map(triple -> ret.add(new Triple(subject, triple.getPredicate(), triple.getObject())));
		}

		log.trace("matchLabelsRANDOM is ready and found " + ret.size() + " labels. Took " + (System.currentTimeMillis() - timeStart) + "ms");

		return ret;
	}

	/**
	 * The approach, that is presented in the paper "KATANA - Knowledge Base Augmentation for Named Entity Surface forms" from Rene Speck, Kunal Jha, Ricardo Usbeck and Axel-Cyrille Ngonga Ngomo
	 * The scoring foreach candidate to a certain label-subject is calculated via a non-intelligence Psi- and Scorefunction
	 *
	 * @return a list of triples. Foreach subjectsWithoutLabels-Node there will be a guess of a label: ?s rdfs:label "LABELGUESS"
	 */
	public List<Triple> matchLabelsKATANAv1() {
		List<Triple> ret = new ArrayList<>(knowledgeLabelExtraction.size());
		if (!checkExecutionPermission())
			return ret;
        Map<AbstractMap.SimpleEntry<Node, String>, Double> score = new HashMap<>(candidates.size() * knowledgeLabelExtraction.size());

		long timeStart = System.currentTimeMillis();

		Map<Triple, Double> psi = calculatePsi();
        for (Node candidate : candidates) {
			for (List<Triple> subjectEvidences : knowledgeLabelExtraction) {
                List<Triple> M = subjectEvidences.stream().filter(triple -> triplesFromKB.stream().anyMatch(t -> t.getSubject().equals(candidate) && t.getPredicate().equals(triple.getPredicate()) && t.getObject().equals(triple.getObject()))).collect(Collectors.toList());
				String label = findLabel(subjectEvidences);
                if (M.size() == 0) {
                    log.trace("For the candidate " + candidate + " there are no evidences for the label " + label);
                    score.put(new AbstractMap.SimpleEntry<>(candidate, label), 0d);
				} else {
                    log.trace("The size of M for the candidate " + candidate + " with the label " + label + " is " + M.size());
					double scoreDiffMult = 1;
                    for (Triple tripleM : M) {
						try {
							double psiValue = psi.get(tripleM);
							scoreDiffMult *= psiValue;
							if (psiValue == 0) {
								break;
							}
						} catch (NullPointerException e) {
							log.error("There was no psi-Entry in " + psi + " for the Triple " + tripleM + "!", e);
						}
					}
					double calculatedScore = 1d - scoreDiffMult;
                    log.trace("For the candidate " + candidate + " there is a evidences rate of " + Math.round(calculatedScore * 100) + "% for the label " + label);
                    score.put(new AbstractMap.SimpleEntry<>(candidate, label), calculatedScore);
				}
			}
		}
        log.debug("Close indexing process. Each candidate has now a score to each label (" + score.size() + " entries). Calculate now the highest score --> candidate for each label!");

		Map<String, AbstractMap.SimpleEntry<Node, Double>> highestScore = new HashMap<>(knowledgeLabelExtraction.size());

		for (Map.Entry<AbstractMap.SimpleEntry<Node, String>, Double> entry : score.entrySet()) {
			String label = entry.getKey().getValue();

            Optional<Map.Entry<String, AbstractMap.SimpleEntry<Node, Double>>> highestScoreEntry = highestScore.entrySet().stream().filter(e -> e.getKey().equals(label)).findFirst();

			if (highestScoreEntry.isPresent()) {
				if (highestScoreEntry.get().getValue().getValue() < entry.getValue()) {
					highestScore.replace(highestScoreEntry.get().getKey(), new AbstractMap.SimpleEntry<>(entry.getKey().getKey(), entry.getValue()));
					log.debug("New highscore for the label " + label + " is found: " + entry.getKey().getValue() + " with score " + entry.getValue());
				}
			} else {
                highestScore.put(label, new AbstractMap.SimpleEntry<>(entry.getKey().getKey(), entry.getValue()));
				log.trace("Fill highestScore: " + highestScore.size() + "/" + knowledgeLabelExtraction.size());
			}
		}
		log.debug("Close comparing process...");

		Node p = NodeFactory.createURI(URITOLABEL);
		for (Map.Entry<String, AbstractMap.SimpleEntry<Node, Double>> entry : highestScore.entrySet()) {
			ret.add(new Triple(entry.getValue().getKey(), p, NodeFactory.createLiteral(entry.getKey())));
		}

        log.debug("matchLabelsKATANAv1 is finished and found " + ret.size() + " labels. Took " + (System.currentTimeMillis() - timeStart) + "ms");

		return ret;
	}

	/**
	 * Calculates the Psi-Function of the KATANA-paper
	 * psi(p,o) = 1-(1/(|{s|(s,p,o)\in KB}|))
	 *
	 * @return a mapping of each triple in knowledgeLabelExtraction to the psi value
	 */
	private Map<Triple, Double> calculatePsi() {
		Map<Triple, Double> dic = new HashMap<>(knowledgeLabelExtraction.size());

		long timesTart = System.currentTimeMillis();

		for (List<Triple> subjectEvidences : knowledgeLabelExtraction) {
			for (Triple triple : subjectEvidences) {
				if (!dic.containsKey(triple)) {
                    double psi = 1d - (1d / (double) (Math.max(1, triplesFromKB.stream().filter(kb -> kb.getPredicate().equals(triple.getPredicate()) && kb.getObject().equals(triple.getObject())).count())));
					log.trace("psi(" + triple.getPredicate() + ", " + triple.getObject() + ") = " + (Math.round(psi * 1000) / 1000));
					dic.put(triple, psi);
				}
			}
		}

		log.debug("Psi function successfully calculated in " + (System.currentTimeMillis() - timesTart) + "ms. Contains " + dic.size() + " entries!");
		return dic;
	}

	/**
	 * Find the Label in a selection
	 *
	 * @param selection the set of triples, including one triple with ?s rdfs:label "LABEL"
	 * @return the label
	 */
	private String findLabel(List<Triple> selection) {
		StringBuilder ret = new StringBuilder("ERROR: no label found...");

		Optional<Triple> selectedTriple = selection.stream().filter(triple -> triple.getPredicate().getURI().equals(URITOLABEL)).findFirst();

        selectedTriple.map(triple -> ret.replace(0, ret.length() - 1, (triple.getObject().isLiteral()) ? triple.getObject().getLiteral().getLexicalForm() : "EXCEPTION: label is no literal ::" + triple.getObject()));

		return ret.toString();
	}

	/**
	 * @return {@code true}, if the Object is runnable in a fine way, otherwise {@code false}
	 */
	private boolean checkExecutionPermission() {
		if (executable)
			return true;

		log.error("You're not allowed to execute this function, because this object " + this + " is not valid!");
		return false;
	}

	/**
	 * Compute/ create an {@link EvaluationHandler}
	 *
	 * @param labelGuesses a set of guessed Labels, e.g from the method matchLabelsKATANAv1
	 * @return an {@link EvaluationHandler}. With that you can determine the goodness of your algorithm
	 */
	public EvaluationHandler getEvaluationHandler(List<Triple> labelGuesses) {
		List<Triple> correctLabels = new ArrayList<>(knowledgeLabelExtraction.size());
		for (List<Triple> subjectEvidences : knowledgeLabelExtraction) {
			Optional<Triple> labelTriple = subjectEvidences.stream().filter(triple -> triple.getPredicate().getURI().equals(URITOLABEL)).findFirst();
			labelTriple.map(triple -> correctLabels.add(triple));
		}
		log.info("Extract the correct labels from knowledgeLabelExtraction. Found " + correctLabels.size() + " of " + knowledgeLabelExtraction.size());
		log.trace("Create an EvaluationHandler out of " + labelGuesses + " and " + correctLabels);
		return new EvaluationHandler(labelGuesses, correctLabels);
	}
}
