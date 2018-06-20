package org.aksw.katana.evaluation;

import com.google.common.collect.ImmutableMap;
import org.aksw.katana.service.InMemoryTripleStore;
import org.aksw.katana.service.SparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.SelectorImpl;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@Scope("prototype")
public class SparqlUtility {

    private static final Logger logger = LoggerFactory.getLogger(SparqlUtility.class);

//    private static final ImmutableMap<String, String> PREFIXES = ImmutableMap.<String, String>builder()
//            .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
//            .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
//            .put("owl", "http://www.w3.org/2002/07/owl#")
//            .build();

    private final SparQL sparQL;
    private final InMemoryTripleStore inMemoryTripleStore;

    @Autowired
    public SparqlUtility(SparQL sparQL, InMemoryTripleStore inMemoryTripleStore) {
        this.sparQL = sparQL;
        this.inMemoryTripleStore = inMemoryTripleStore;
    }

    public Map<String, HashSet<Pair<Property, RDFNode>>> generateExtractedKnowledgeBase(Map<Resource, String> allLabels) {
        Map<String, HashSet<Pair<Property, RDFNode>>> EKB = new HashMap<String, HashSet<Pair<Property, RDFNode>>>();

        allLabels.forEach((resource, label) -> {
            EKB.put(label, new HashSet<>());
//            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
//                    "SELECT ?property ?object {\n" +
//                    "  ?subject ?property ?object .\n" +
//                    "  FILTER (?property!=rdfs:label && ?property!=owl:sameAs)\n" +
//                    "  FILTER(!isLiteral(?object) || lang(?object)=\"\" || lang(?object)=\"en\")\n" +
//                    "} ");
//
//            pss.setNsPrefixes(PREFIXES);
//
//            pss.setParam("subject", resource);
//
//            try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
//                ResultSet results = exec.execSelect();

            StmtIterator results = inMemoryTripleStore.getModel().listStatements(resource, null, (RDFNode)null);

            while (results.hasNext()) {
                Statement solution = results.nextStatement();
                Property property = solution.getPredicate();
                RDFNode object = solution.getObject();
                EKB.get(label).add(Pair.of(property, object));
            }
//            } catch (Exception e) {
//                logger.error(Arrays.toString(e.getStackTrace()));
//            }
        });

        return EKB;
    }

    public Map<Resource, String> GetAllLabels() {
        Map<Resource, String> allLabels = new HashMap<Resource, String>();
//        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
//                "SELECT DISTINCT * WHERE {\n" +
//                "  ?subject rdfs:label ?label .\n" +
//                "  FILTER ( LANG(?label)=\"\" || LANG(?label)=\"en\" )\n" +
//                "}");
//
//        pss.setNsPrefixes(PREFIXES);


//        try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
//            ResultSet results = exec.execSelect();

        ResIterator results = inMemoryTripleStore.getModel().listSubjectsWithProperty(RDFS.label);

        while (results.hasNext()) {
                Resource resource = results.nextResource();
                allLabels.put(resource, inMemoryTripleStore.getModel().listObjectsOfProperty(resource, RDFS.label).next().asLiteral().toString());
            }
//        } catch (Exception e) {
//            logger.error(Arrays.toString(e.getStackTrace()));
//        }
        return allLabels;
    }
}