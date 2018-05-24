package org.aksw.katana.evaluation.benchmark;

import com.google.common.collect.ImmutableMap;
import org.aksw.katana.algorithm.KATANA;
import org.aksw.katana.service.SparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

//Get all labels (rdfs:label) from the given KB
//Arbitrary separate some of them into  Deleted Label set
//for all statements of KB those have at least one of the subject or object such that their label is in the
//  Deleted Label set, generate new Triple by replacing URIs with their equivalent label in the Deleted Label set,
//  and call it  EKB(Extracted Knowledge Base)
//remove the rdfs:label from KB in which their label is in the Deleted Label set, and call it SKB(Shrunk Knowledge Base)
//by using KATANA (SKB, EKB) try to reach the DELETED Labels


@Component
@Profile({"benchmark","test"})
@Scope("prototype")
public class BenchMarkEvaluation implements Callable<Pair<Integer, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(BenchMarkEvaluation.class);

    private final SparQL sparQL;
    private final KATANA katana;

    @Autowired
    public BenchMarkEvaluation(SparQL sparQL, KATANA katana) {
        this.sparQL = sparQL;
        this.katana = katana;
    }

    @Override
    public Pair<Integer, Integer> call() {

        List<Pair<Resource, String>> allLabels = GetAllLabels();
        Map<Resource, String> deletedLabels = RemoveSomeLabelsArbitrary(allLabels);
        Map<String, HashSet<Pair<Property, RDFNode>>> EKB = generateExtractedKnowledgeBaseAndShrankKnowledgeBase(deletedLabels);

        Map<String, Resource> result = katana.run(EKB);

        return checkResult(deletedLabels, result);
    }

    private Pair<Integer, Integer> checkResult(Map<Resource, String> deletedLabels, Map<String, Resource> result) {
        AtomicInteger cntTruePositive = new AtomicInteger();
        result.forEach((label, entity) -> {
            if (deletedLabels.containsKey(entity))
                cntTruePositive.addAndGet((deletedLabels.get(entity).equals(label)) ? 1 : 0);
            else
                logger.info("not found with the label {}, and entity {}", label, entity);
        });

        logger.debug("Deleted labels: ");
        deletedLabels.forEach((x, y) -> logger.debug("label: {}, resource: {}", y, x));

        logger.debug("Found labels: ");
        result.forEach((x, y) -> logger.debug("label: {}, resource: {}", x, y));
        logger.info("#True Positive: {}", cntTruePositive);
        logger.info("#All deleted labels: {}", deletedLabels.size());
        return Pair.of(cntTruePositive.get(), deletedLabels.size());
    }

    private Map<String, HashSet<Pair<Property, RDFNode>>> generateExtractedKnowledgeBaseAndShrankKnowledgeBase(Map<Resource, String> deletedLabels) {
        Map<String, HashSet<Pair<Property, RDFNode>>> EKB = new HashMap<>();

        deletedLabels.forEach((resource, label) -> {
            EKB.put(label, new HashSet<>());
            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                    "SELECT ?property ?object {\n" +
                    "  ?subject ?property ?object .\n" +
                    "  FILTER (?property!=rdfs:label && ?property!=owl:sameAs)\n" +
                    "  FILTER(!isLiteral(?object) || lang(?object)=\"\" || lang(?object)=\"en\")\n" +
                    "} ");

            pss.setNsPrefixes(ImmutableMap.<String, String>builder()
                    .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                    .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                    .put("owl", "http://www.w3.org/2002/07/owl#")
                    .build());

            pss.setParam("subject", resource);

            try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
                ResultSet results = exec.execSelect();
                if (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    Property property = ResourceFactory.createProperty(solution.getResource("property").getURI());
                    RDFNode object = solution.get("object");
                    EKB.get(label).add(Pair.of(property, object));
                }
            } catch (Exception e) {
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        });

        return EKB;
    }

    private Map<Resource, String> RemoveSomeLabelsArbitrary(List<Pair<Resource, String>> allLabels) {
//        Random rnd = new Random();

        Map<Resource, String> deletedLabels = new HashMap<>();

        allLabels.forEach(x -> {
//            if (rnd.nextDouble() < 0.3)// TODO: 24.04.18 make 0.3 a variable
                deletedLabels.put(x.getKey(), x.getValue());
        });
        return deletedLabels;
    }

    private List<Pair<Resource, String>> GetAllLabels() {
        List<Pair<Resource, String>> allLabels = new ArrayList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "SELECT DISTINCT * WHERE {\n" +
                "  ?subject rdfs:label ?label .\n" +
                "  FILTER ( LANG(?label)=\"\" || LANG(?label)=\"en\" )\n" +
                "}");

        pss.setNsPrefixes(ImmutableMap.<String, String>builder()
                .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                .put("owl", "http://www.w3.org/2002/07/owl#")
                .build());


        try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
            ResultSet results = exec.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                allLabels.add(Pair.of(solution.getResource("subject"), solution.getLiteral("label").getLexicalForm()));
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        return allLabels;
    }


}
