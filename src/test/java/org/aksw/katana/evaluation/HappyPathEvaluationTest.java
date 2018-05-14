package org.aksw.katana.evaluation;

import org.aksw.katana.service.InMemoryTripleStore;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class HappyPathEvaluationTest {

    @Autowired
    private HappyPathEvaluation happyPathEvaluation;

    @Autowired
    private InMemoryTripleStore inMemoryTripleStore;

    @Test
    public void run() {
        Model model = generate();
        Mockito.when(inMemoryTripleStore.getModel()).thenReturn(model);
        happyPathEvaluation.run();
    }

//region generate KB
    private final Random random = new Random();

    private int cntResource = 0;
    private int cntProperty = 0;
    private int cntObject = 0;

    @Value("${info.subset.sizeBound}")
    private int bound;
    @Value("${info.localResource.size}")
    private int localSizeOfResources;

    private Model generate() {
        Model model = ModelFactory.createDefaultModel();
        model.add(noShareProperty());
        model.add(someShareProperties());

//                OutputStream out = new BufferedOutputStream(new FileOutputStream("/home/afshin/Desktop/output.ttl"));
//                model.write(out, "TURTLE"); optional (if you want you can have the Model as a file for additional studies)
        return model;
    }

    private List<Statement> someShareProperties() {
        List<Statement> ret = new ArrayList<>();
        ret.addAll(generateSharedProperties(false));
        ret.addAll(generateSharedProperties(true));
        //because for some resources there are some non-inverse functional Properties that have unique values
        // (in which they act like a inverse functional) there is no need to implement the case that have
        // inverseFunctional and non-inverseFunctional as shared properties
        return ret;
    }

    private List<Statement> generateSharedProperties(boolean canBeRepetitive) {
        List<Statement> ret = new ArrayList<>();

        List<Property> sharedProperties = new ArrayList<>();
        generateSomeProperties(sharedProperties);

        ret.addAll(justThisProperties(sharedProperties, canBeRepetitive).getLeft());

        int n = sharedProperties.size();
        for (int i = 0 + 1//Do not add empty subset
             ; i < (1 << n) - 1;//the whole set is generated before
             i++) {

            if (random.nextDouble() > 0.1) continue; //add some (10 percentages) subsets randomly
            List<Property> subset = new ArrayList<>();
            for (int j = 0; j < n; j++)
                if ((i & (1 << j)) > 0) {
                    subset.add(sharedProperties.get(j));
                }
            ret.addAll(atLeastThisSubset(subset, false));
        }

        //there is no need to explicitly tell these properties are InverseFunctional
//        sharedProperties.forEach(property ->
//                ret.add(new StatementImpl(property, RDF.type, OWL2.InverseFunctionalProperty)));

        return ret;
    }

    private void generateSomeProperties(List<Property> sharedInverseFunctionalProperties) {
        for (int i = 0; i < 8; i++)
            sharedInverseFunctionalProperties.add(
                    ResourceFactory.createProperty("http://example.de/property" + cntProperty++));
    }

    private List<Statement> atLeastThisSubset(List<Property> subset, boolean canBeRepetitive) {
        List<Statement> ret = new ArrayList<>();
        Pair<List<Statement>, List<Resource>> listTriple = justThisProperties(subset, canBeRepetitive);
        List<Resource> resources = listTriple.getRight();
        resources.forEach(resource -> generateRandomDifferentPropertyAndObject(ret, resource));
        ret.addAll(listTriple.getLeft());
        return ret;
    }

    private Pair<List<Statement>, List<Resource>> justThisProperties(List<Property> sharedProperties, boolean canBeRepetitive) {
        List<Resource> generatedResources = new ArrayList<>();
        List<Statement> ret = new ArrayList<>();

        Map<Resource, Resource> repetitiveResources = new HashMap<>();
        for (int i = 0; i < localSizeOfResources; i++, cntResource++) {
            Resource s = ResourceFactory.createResource("http://example.de/resource" + cntResource);
            ret.add(new StatementImpl(s, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
            generatedResources.add(s);
            if (canBeRepetitive && random.nextDouble() < 0.3) {//for almost complete similar cases
                cntResource++;
                Resource t = ResourceFactory.createResource("http://example.de/resource" + cntResource);
                ret.add(new StatementImpl(t, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
                repetitiveResources.put(s, t);
            }
        }
        sharedProperties.forEach(p -> {
            List<Integer> generatedObjectValues = new ArrayList<>();//for each p the object values can be repetitive for some entities
            generatedResources.forEach(s -> {
                int objectValue = cntObject;
                if (canBeRepetitive || generatedObjectValues.size() == 0 || random.nextDouble() > 0.2) //20 percentages of values can be repetitive
                    cntObject++;
                else
                    objectValue = generatedObjectValues.get(random.nextInt(generatedObjectValues.size()));
                if (!canBeRepetitive)
                    generatedObjectValues.add(objectValue);
                RDFNode o = ResourceFactory.createStringLiteral("" + objectValue);
                ret.add(new StatementImpl(s, p, o));
                if (canBeRepetitive && repetitiveResources.containsKey(s))
                    ret.add(new StatementImpl(repetitiveResources.get(s), p, o));
            });
        });

        if (canBeRepetitive && repetitiveResources.size() > 0)
            repetitiveResources.forEach((s, t) -> {
                System.out.println("s: " + s + ", t:" + t);
                if (random.nextDouble() < 0.5)
                    generatedResources.remove(s);// in 50 percentage of times removing it from generated resources
                // to prevent s,t have arbitrary random properties
            });


        return Pair.of(ret, generatedResources);
    }

    private List<Statement> noShareProperty() {
        List<Statement> ret = new ArrayList<>();
        for (int i = 0; i < 1024; i++, cntResource++) {
            Resource s = ResourceFactory.createResource("http://example.de/resource" + cntResource);
            ret.add(new StatementImpl(s, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
            generateRandomDifferentPropertyAndObject(ret, s);
        }
        return ret;
    }

    private void generateRandomDifferentPropertyAndObject(List<Statement> ret, Resource s) {
        int randomLength = random.nextInt(bound - 1) + 1;//make sure have at least one nonLabel Property
        for (int j = 0; j < randomLength; j++) {
            Property p = ResourceFactory.createProperty("http://example.de/property" + cntProperty++);
            RDFNode o = ResourceFactory.createStringLiteral("" + cntObject++);
            ret.add(new StatementImpl(s, p, o));
        }
    }
//endregion
}


