package org.aksw.katana.algorithm;

import com.google.common.collect.ImmutableMap;
import org.aksw.katana.service.SparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


//for each candidate in EKB(Extracted Knowledge Base) calculate its Possible Target Resources (name it possibleTargetResources)
//then for each target resource calculate score(candidate, targetResource) and the one with higher score is the most accurate result
//if there are more than one target resource arbitrary one will be chosen
// TODO: 26.04.18 for multiple result set a better rule

@Component
public class KATANA {

    private static final Logger logger = LoggerFactory.getLogger(KATANA.class);
    private static final double EPS = 1e-8;

    private final SparQL sparQL;

    @Autowired
    public KATANA(SparQL sparQL){

        this.sparQL = sparQL;
    }

    public Map<String, Resource> run(Map<String, HashSet<Pair<Property, RDFNode>>> EKB) {

        Map<String, Resource> result = new HashMap<>();

        EKB.entrySet().forEach(candidate -> {
            Set<Resource> possibleTargetResources = calculatePossibleTargetResources(candidate);
            Resource resource = extractMostPossibleTarget(possibleTargetResources, candidate);
            result.put(candidate.getKey(), resource);
        });

        return result;
    }

    private Resource extractMostPossibleTarget(Set<Resource> possibleTargetResources,
                                               Map.Entry<String, HashSet<Pair<Property, RDFNode>>> candidate) {
        double maxPossibleScore = -1;
        ArrayList<Resource> targetResource = new ArrayList<>();
        for (Resource s : possibleTargetResources) { // TODO: 26.04.18 Can be parallelled
            Set<Pair<Property, RDFNode>> M = calculateM_c_s(candidate, s);

            logger.debug("extractMostPossibleTarget, candidate: {}, s:{}, M: {} ", candidate.getKey(), M);

            AtomicReference<Double> tempProduct = new AtomicReference<>((double) 1);
            M.forEach(po -> {
                double psi = calculatePsi(po);
                tempProduct.updateAndGet(v -> v * psi);
            });
            double score = 1 - tempProduct.get(); //if M is an empty set then score is 0

            logger.debug("extractMostPossibleTarget, candidate: {}, possible Target Resource: {}, score: {}", candidate.getKey(), s, score);

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
            logger.info(" candidate {} , non unique targetResources are {}",candidate, Arrays.toString(targetResource.toArray()));
        }
        if (targetResource.size() == 0)
            return null;
        return targetResource.get(new Random().nextInt(targetResource.size()));
    }

    private double calculatePsi(Pair<Property, RDFNode> po) {

        Property property = po.getLeft();
        RDFNode object = po.getRight();

        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "SELECT (COUNT(*) AS ?cnt) {\n" +
                "?subject ?property ?object\n" +
                "} ");

        pss.setNsPrefixes(ImmutableMap.<String, String>builder()
                .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                .build());

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
            Map.Entry<String, HashSet<Pair<Property, RDFNode>>> candidate, Resource s) {

        Set<Pair<Property, RDFNode>> M = new HashSet<>();

        for (Pair<Property, RDFNode> po : candidate.getValue()) {
            Property property = po.getLeft();
            RDFNode object = po.getRight();

            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                    "ASK {\n" +
                    "?subject ?property ?object\n" +
                    "} ");

            pss.setNsPrefixes(ImmutableMap.<String, String>builder()
                    .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                    .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                    .build());

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

    private Set<Resource> calculatePossibleTargetResources(Map.Entry<String, HashSet<Pair<Property, RDFNode>>> candidate) {
        Set<Resource> possibleTargetResources = new HashSet<>();
        candidate.getValue().forEach(po -> {
            //select all s that have po explicitly
            Property property = po.getLeft();
            RDFNode object = po.getRight();

            ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                    "SELECT DISTINCT ?subject WHERE {\n" +
                    "  ?subject ?property ?object .\n" +
                    "}");

            pss.setNsPrefixes(ImmutableMap.<String, String>builder()
                    .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                    .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
                    .build());

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
        logger.debug("possible target resources for candidate {} are {}", candidate, Arrays.toString(possibleTargetResources.toArray()));
        return possibleTargetResources;
    }

}
