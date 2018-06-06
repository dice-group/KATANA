package org.aksw.katana.evaluation.benchmark.enhanced;

import org.aksw.katana.algorithm.KATANA;
import org.aksw.katana.evaluation.SparqlUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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
@Profile({"enhancedBenchmark", "test"})
@Scope("prototype")
public class EnhancedBenchMarkEvaluation implements Callable<Pair<Integer, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedBenchMarkEvaluation.class);

    private final KATANA katana;
    private final SparqlUtility sparqlUtility;

    @Autowired
    public EnhancedBenchMarkEvaluation(KATANA katana, SparqlUtility sparqlUtility) {
        this.katana = katana;
        this.sparqlUtility = sparqlUtility;
    }

    @Override
    public Pair<Integer, Integer> call() {
        Map<Resource, String> allLabels = sparqlUtility.GetAllLabels();
        Map<String, HashSet<Pair<Property, RDFNode>>> EKB = sparqlUtility.generateExtractedKnowledgeBase(allLabels);
        Map<Pair<String, Integer>, HashSet<Pair<Property, RDFNode>>> EEKB = getEnhancedEKB(EKB);
        Map<Triple<String, Integer, Double>, Resource> result = katana.runEnhancedBenchMark(EEKB);
        return checkResult(allLabels, result);
    }

    private Pair<Integer, Integer> checkResult(Map<Resource, String> allLabels, Map<Triple<String, Integer, Double>, Resource> result) {
        AtomicInteger cntTruePositive = new AtomicInteger();
        AtomicInteger all = new AtomicInteger();
        result.forEach((key, entity) -> {
            all.addAndGet(1);
            if (allLabels.containsKey(entity))
                cntTruePositive.addAndGet((allLabels.get(entity).equals(key.getLeft())) ? 1 : 0);
            else
                logger.info("not found with the label {}, and entity {}", key.getLeft(), entity);
        });

        logger.debug("Deleted labels: ");
        allLabels.forEach((x, y) -> logger.debug("label: {}, resource: {}", y, x));

        logger.debug("Found labels: ");
        result.forEach((x, y) -> logger.debug("label: {}, resource: {}", x, y));
        logger.info("#True Positive: {}", cntTruePositive);
        logger.info("#All deleted labels: {}", all);
        return Pair.of(cntTruePositive.get(), all.get());
    }

    private Map<Pair<String, Integer>, HashSet<Pair<Property, RDFNode>>> getEnhancedEKB(Map<String, HashSet<Pair<Property, RDFNode>>> EKB) {
        Map<Pair<String, Integer>, HashSet<Pair<Property, RDFNode>>> EEKB = new HashMap<>();
        EKB.forEach((label, allPOs) -> {
            List<Pair<Property, RDFNode>> pairs = new ArrayList<>(allPOs);
            for (int i = 1; i < (1 << pairs.size()); i++) { //skip empty subset
                HashSet<Pair<Property, RDFNode>> subset = new HashSet<>();
                for (int j = 0; j < pairs.size(); j++)
                    if ((i & j) > 0) {
                        subset.add(pairs.get(j));
                    }
                EEKB.put(Pair.of(label, i), subset);
            }
        });
        return EEKB;
    }


//    private Pair<Integer, Integer> checkResult(Map<Resource, String> deletedLabels, Map<String, Resource> result) {
//        AtomicInteger cntTruePositive = new AtomicInteger();
//        result.forEach((label, entity) -> {
//            if (deletedLabels.containsKey(entity))
//                cntTruePositive.addAndGet((deletedLabels.get(entity).equals(label)) ? 1 : 0);
//            else
//                logger.info("not found with the label {}, and entity {}", label, entity);
//        });
//
//        logger.debug("Deleted labels: ");
//        deletedLabels.forEach((x, y) -> logger.debug("label: {}, resource: {}", y, x));
//
//        logger.debug("Found labels: ");
//        result.forEach((x, y) -> logger.debug("label: {}, resource: {}", x, y));
//        logger.info("#True Positive: {}", cntTruePositive);
//        logger.info("#All deleted labels: {}", deletedLabels.size());
//        return Pair.of(cntTruePositive.get(), deletedLabels.size());
//    }


}
