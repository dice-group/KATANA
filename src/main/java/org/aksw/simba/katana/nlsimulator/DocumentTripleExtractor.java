package org.aksw.simba.katana.nlsimulator;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.bengal.paraphrasing.ParaphraseService;
import org.aksw.simba.bengal.paraphrasing.Paraphraser;
import org.aksw.simba.bengal.paraphrasing.ParaphraserImpl;
import org.aksw.simba.bengal.paraphrasing.Paraphrasing;
import org.aksw.simba.bengal.selector.TripleSelector;
import org.aksw.simba.bengal.selector.TripleSelectorFactory;
import org.aksw.simba.bengal.selector.TripleSelectorFactory.SelectorType;
import org.aksw.simba.bengal.verbalizer.AvatarVerbalizer;
import org.aksw.simba.bengal.verbalizer.BVerbalizer;
import org.aksw.simba.bengal.verbalizer.NumberOfVerbalizedTriples;
import org.aksw.simba.bengal.verbalizer.SemWeb2NLVerbalizer;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentTripleExtractor {

	public DocumentTripleExtractor() {
		super();
		this.triples = new ArrayList<Triple>();
		this.labeltriples = new ArrayList<Triple>();

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTripleExtractor.class);
	private static final String NUMBEROFDOCS = "numberofdocs";

	private static final long SEED = 21;
	private static final int MIN_SENTENCE = 1;
	private static final int MAX_SENTENCE = 5;

	private static final boolean USE_AVATAR = false;
	private static final SelectorType SELECTOR_TYPE = SelectorType.STAR;
	private static final boolean USE_PARAPHRASING = true;
	private static final boolean USE_PRONOUNS = false;
	private static final boolean USE_SURFACEFORMS = true;
	private static final int DEFAULT_NUMBER_OF_DOCUMENTS = 100;

	private static final boolean USE_ONLY_OBJECT_PROPERTIES = false;
	private static final long WAITING_TIME_BETWEEN_DOCUMENTS = 500;
	public List<Triple> triples;
	public List<Triple> labeltriples;

	public void generateCorpus(Map<String, String> parameters, String endpoint, String corpusName) {
		if (parameters == null) {
			parameters = new HashMap<>();
		}

		// Put names of classes
		Set<String> classes = new HashSet<>();
		classes.add("<http://dbpedia.org/ontology/Person>");

		// instantiate components;
		TripleSelectorFactory factory = new TripleSelectorFactory();
		TripleSelector tripleSelector = null;
		BVerbalizer verbalizer = null;
		AvatarVerbalizer alernativeVerbalizer = null;
		if (USE_AVATAR) {
			alernativeVerbalizer = AvatarVerbalizer.create(classes,
					USE_ONLY_OBJECT_PROPERTIES ? classes : new HashSet<>(), endpoint, null, SEED, false);
			if (alernativeVerbalizer == null) {
				return;
			}
		} else {
			tripleSelector = factory.create(SELECTOR_TYPE, classes,
					USE_ONLY_OBJECT_PROPERTIES ? classes : new HashSet<>(), endpoint, null, MIN_SENTENCE, MAX_SENTENCE,
					SEED);
			verbalizer = new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), USE_PRONOUNS, USE_SURFACEFORMS);
		}
		Paraphraser paraphraser = null;
		if (USE_PARAPHRASING) {
			ParaphraseService paraService = Paraphrasing.create();
			if (paraService != null) {
				paraphraser = new ParaphraserImpl(paraService);
			} else {
				LOGGER.error("Couldn't create paraphrasing service. Aborting.");
				return;
			}
		}

		// Get the number of documents from the parameters
		int numberOfDocuments = DEFAULT_NUMBER_OF_DOCUMENTS;
		if (parameters.containsKey(NUMBEROFDOCS)) {
			try {
				numberOfDocuments = Integer.parseInt(parameters.get(NUMBEROFDOCS));
			} catch (Exception e) {
				LOGGER.error("Could not parse number of documents");
			}
		}
		List<Statement> statements;

		Document document = null;
		List<Document> documents = new ArrayList<>();
		int counter = 0;
		while (documents.size() < numberOfDocuments) {
			if (USE_AVATAR) {
				document = alernativeVerbalizer.nextDocument();
			} else {
				// select triples
				statements = tripleSelector.getNextStatements();

				if ((statements != null) && (statements.size() >= MIN_SENTENCE)) {
					// create document
					for (Statement tripleForOneResource : statements) {
						triples.add(tripleForOneResource.asTriple());

						if (tripleForOneResource.asTriple().getMatchPredicate().getURI().contains("sameAs")) {
							System.out.println(tripleForOneResource.asTriple());
						}
					}

					document = verbalizer.generateDocument(statements);
					if (document != null) {
						List<NumberOfVerbalizedTriples> tripleCounts = document
								.getMarkings(NumberOfVerbalizedTriples.class);
						if ((tripleCounts.size() > 0) && (tripleCounts.get(0).getNumberOfTriples() < MIN_SENTENCE)) {
							LOGGER.error(
									"The generated document does not have enough verbalized triples. It will be discarded.");
							document = null;
						}
					}
					if (document != null) {
						// paraphrase document
						if (paraphraser != null) {
							try {
								document = paraphraser.getParaphrase(document);
							} catch (Exception e) {
								LOGGER.error("Got exception from paraphraser. Using the original document.", e);
							}
						}
					}
				}
			}
			// If the generation and paraphrasing were successful
			if (document != null) {
				LOGGER.info("Created document #" + counter);
				document.setDocumentURI("http://aksw.org/generated/" + counter);
				counter++;
				documents.add(document);
				document = null;
			}
			try {
				if (!USE_AVATAR) {
					Thread.sleep(WAITING_TIME_BETWEEN_DOCUMENTS);
				}
			} catch (InterruptedException e) {
			}
		}

		// generate file name and path from corpus name
		String filePath = corpusName;
		// write the documents
		NIFWriter writer = new TurtleNIFWriter();
		FileOutputStream fout = null;
		int i = 0;
		try {
			fout = new FileOutputStream(filePath);
			for (; i < documents.size(); ++i) {
				writer.writeNIF(documents.subList(i, i + 1), fout);
			}
			// writer.writeNIF(documents, fout);
		} catch (Exception e) {
			System.out.println(documents.get(i));
			LOGGER.error("Error while writing the documents to file. Aborting.", e);
			System.out.println(documents.get(i));
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (Exception e) {
					// nothing to do
				}
			}
		}
	}

	public List<Triple> getTriples() {
		return triples;
	}

	public void setTriples(List<Triple> triples) {
		this.triples = triples;
	}
}