package org.aksw.katana.algorithm;

import com.google.common.collect.ImmutableMap;
import org.aksw.katana.service.SparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


//for each candidate in EKB(Extracted Knowledge Base) calculate its Possible Target Resources (name it possibleTargetResources)
//then for each target resource calculate score(candidate, targetResource) and the one with higher score is the most accurate result
//if there are more than one target resource arbitrary one will be chosen
// TODO: 26.04.18 for multiple result set a better rule

@Component
@Scope("prototype")
public class KATANA {

    private static final Logger logger = LoggerFactory.getLogger(KATANA.class);
    private static final double EPS = 1e-8;
    private static final ImmutableMap<String, String> PREFIXES = ImmutableMap.<String, String>builder()
            .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
            .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
            .put("owl", "http://www.w3.org/2002/07/owl#")
            .build();

    private final SparQL sparQL;

    @Autowired
    public KATANA(SparQL sparQL){

        this.sparQL = sparQL;
    }

    public Map<String, Resource> runBenchmark(Map<String, HashSet<Pair<Property, RDFNode>>> EKB) {

        Map<String, Resource> result = new HashMap<>();

        EKB.entrySet().forEach(candidate -> {
            Set<Resource> possibleTargetResources = calculatePossibleTargetResources(candidate.getValue());
            logger.debug("possible target resources for candidate {} are {}", candidate, Arrays.toString(possibleTargetResources.toArray()));
            Resource resource = extractMostPossibleTarget(candidate.getKey(), possibleTargetResources, candidate.getValue());
            result.put(candidate.getKey(), resource);
        });

        return result;
    }

    public Map<Triple<String, Integer, Double>, Resource> runEnhancedBenchMark
            (Map<Pair<String, Integer>, HashSet<Pair<Property, RDFNode>>> EKB) {

        Map<Triple<String, Integer, Double>, Resource> result = new HashMap<>();

        EKB.forEach((key, value) -> {
            Set<Resource> possibleTargetResources = calculatePossibleTargetResources(value);
            Resource resource = extractMostPossibleTarget(key.getLeft(), possibleTargetResources, value);
            double score = calculateScore(value);
            result.put(Triple.of(key.getLeft(), key.getRight(), score), resource);
        });

        return result;
    }

    private Resource extractMostPossibleTarget(String label, Set<Resource> possibleTargetResources,
                                               HashSet<Pair<Property, RDFNode>> allPOs) {
        double maxPossibleScore = -1;
        ArrayList<Resource> targetResource = new ArrayList<>();
        for (Resource s : possibleTargetResources) { // TODO: 26.04.18 Can be parallelled
            Set<Pair<Property, RDFNode>> M = calculateM_c_s(s, allPOs);

            logger.debug("extractMostPossibleTarget, candidate: {}, s:{}, M: {} ", label, M);

            double score = calculateScore(M);

            logger.debug("extractMostPossibleTarget, candidate: {}, possible Target Resource: {}, score: {}", label, s, score);

            if (Math.abs(score - maxPossibleScore) < EPS) {
                targetResource.add(s);
            } else if (score > maxPossibleScore) {
                maxPossibleScore = score;
                targetResource = new ArrayList<>();
                targetResource.add(s);
            }
        }
        logger.debug("extractMostPossibleTarget, result: Score: {} Resource: {}", maxPossibleScore, targetResource);
        if(targetResource.size() != 1){
            logger.info(" label {}, POs {} , non unique targetResources are {}",label, allPOs, Arrays.toString(targetResource.toArray()));
        }
        if (targetResource.size() == 0)
            return null;
        return targetResource.get(new Random().nextInt(targetResource.size()));
    }

    private double calculateScore(Set<Pair<Property, RDFNode>> m) {
        AtomicReference<Double> tempProduct = new AtomicReference<>((double) 1);
        m.forEach(po -> {
            double psi = calculatePsi(po);
            tempProduct.updateAndGet(v -> v * psi);
        });
        return 1 - tempProduct.get();
    }

    private double calculatePsi(Pair<Property, RDFNode> po) {

        Property property = po.getLeft();
        RDFNode object = po.getRight();

        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "SELECT (COUNT(*) AS ?cnt) {\n" +
                "?subject ?property ?object\n" +
                "} ");

        pss.setNsPrefixes(PREFIXES);

        pss.setParam("property", property);
        pss.setParam("object", object);

        try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
            ResultSet results = exec.execSelect();
            int cnt = 1;
            if (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                cnt = solution.get("cnt").asLiteral().getInt();
            }

            logger.debug("calculatePsi, property: {}, object:{}, count:{}", property, object, cnt);

            return 1 - 1.0 / cnt;
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        return 1;
    }

    private Set<Pair<Property, RDFNode>> calculateM_c_s(
            Resource s, HashSet<Pair<Property, RDFNode>> allPOs) {

        Set<Pair<Property, RDFNode>> M = new HashSet<>();

        for (Pair<Property, RDFNode> po : allPOs) {
            Property property = po.getLeft();
            RDFNode object = po.getRight();

            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                    "ASK {\n" +
                    "?subject ?property ?object\n" +
                    "} ");

            pss.setNsPrefixes(PREFIXES);

            pss.setParam("subject", s);
            pss.setParam("property", property);
            pss.setParam("object", object);


            try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
                if (exec.execAsk())
                    M.add(po);
            } catch (Exception e) {
                logger.debug("Query: {}", pss.toString());
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        }

        return M;
    }

    private Set<Resource> calculatePossibleTargetResources(HashSet<Pair<Property, RDFNode>> allPOs) {
        Set<Resource> possibleTargetResources = new HashSet<>();
        allPOs.forEach(po -> {
            //select all s that have po explicitly
            Property property = po.getLeft();
            RDFNode object = po.getRight();

            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                    "SELECT DISTINCT ?subject WHERE {\n" +
                    "  ?subject ?property ?object .\n" +
                    "}");

            pss.setNsPrefixes(PREFIXES);

            pss.setParam("property", property);
            pss.setParam("object", object);


            try (QueryExecution exec = sparQL.createQueryExecution(pss.asQuery())) {
                ResultSet results = exec.execSelect();
                while (results.hasNext()) {
                    QuerySolution solution = results.next();
                    possibleTargetResources.add(solution.getResource("subject"));
                }
            } catch (Exception e) {
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        });
        return possibleTargetResources;
    }

}
