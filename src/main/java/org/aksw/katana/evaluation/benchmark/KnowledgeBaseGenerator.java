package org.aksw.katana.evaluation.benchmark;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile({"benchmark", "test"})
public class KnowledgeBaseGenerator {

    private static int cntResource = 0;
    private static int cntProperty = 0;
    private static int cntObject = 0;

    @Value("${graph.allEntities}")
    private int numberOfAllEntities;

    @Value("${graph.shareSomePO_percentage}")
    private int shareSomePO_Percentage;
    @Value("${graph.exactlyTheSamePO_percentage}")
    private int exactlyTheSamePO_Percentage;

    @Value("${graph.outputFilePath}")
    private String outputFilePath;

    private final PropertiesGenerator propertiesGenerator;

    @Autowired
    public KnowledgeBaseGenerator(PropertiesGenerator propertiesGenerator) {
        this.propertiesGenerator = propertiesGenerator;
    }


    public Model generate() {
        Model model = ModelFactory.createDefaultModel();
        model.add(shareSomePO((int) (shareSomePO_Percentage / 100.0 * numberOfAllEntities)));
        model.add(exactlyTheSamePO((int) (exactlyTheSamePO_Percentage / 100.0 * numberOfAllEntities)));
        int distinctPO_Percentage = 100 - (exactlyTheSamePO_Percentage + shareSomePO_Percentage);
        model.add(distinctPO((int) (distinctPO_Percentage / 100.0 * numberOfAllEntities)));

        //you can save it for later usages
//            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilePath));
//            model.write(out, "TURTLE");

        return model;
    }

    private List<Statement> shareSomePO(int numberOfShareSomePO) {
        List<Statement> ret = new ArrayList<>();
        Resource[] generatedResources = new Resource[numberOfShareSomePO];
        for (int i = 0; i < numberOfShareSomePO; i++) {
            Resource s = ResourceFactory.createResource("http://example.de/resource" + cntResource);
            ret.add(new StatementImpl(s, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
            generatedResources[i] = s;
            cntResource++;
        }

        //for the last one there is no new PO but it has at least some common with the penultimate resources
        for (int i = 0; i < numberOfShareSomePO - 1; i++) {

            //first add some PO
            //then share it with the following resources

            int numberOfProperties = propertiesGenerator.getNumberOfProperties();
            List<Pair<Property, RDFNode>> POs = new ArrayList<>();
            for (int k = 0; k < numberOfProperties; k++) {
                Property p = ResourceFactory.createProperty("http://example.de/property" + cntProperty++);
                RDFNode o = ResourceFactory.createStringLiteral("" + cntObject++);
                POs.add(Pair.of(p, o));
                ret.add(new StatementImpl(generatedResources[i], p, o));
            }


            List<Integer> shareCandidateIndices = propertiesGenerator.getShareCandidateIndices(i, numberOfShareSomePO);
            shareCandidateIndices.forEach(candidateIndex -> {
                List<Integer> sharedPO_Indices = propertiesGenerator.getSharePO_Indices(numberOfProperties);
                sharedPO_Indices.forEach(sharedPO_index -> ret.add(new StatementImpl(
                        generatedResources[candidateIndex],
                        POs.get(sharedPO_index).getLeft(),
                        POs.get(sharedPO_index).getRight())));
            });
        }
        return ret;
    }

    private List<Statement> distinctPO(int numberOfDistinctPO) {
        List<Statement> ret = new ArrayList<>();
        for (int i = 0; i < numberOfDistinctPO; i++) {
            Resource s = ResourceFactory.createResource("http://example.de/resource" + cntResource);
            ret.add(new StatementImpl(s, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
            cntResource++;
            int numberOfProperties = propertiesGenerator.getNumberOfProperties();//maximum 8 po and at least one
            for (int k = 0; k < numberOfProperties; k++) {
                Property p = ResourceFactory.createProperty("http://example.de/property" + cntProperty++);
                RDFNode o = ResourceFactory.createStringLiteral("" + cntObject++);
                ret.add(new StatementImpl(s, p, o));
            }
        }
        return ret;
    }

    private List<Statement> exactlyTheSamePO(int numberOfExactlyTheSamePO) {
        List<Statement> ret = new ArrayList<>();
        for (int i = 0; i < numberOfExactlyTheSamePO; ) {
            int sizeOfSubset = propertiesGenerator.getSizeOfOneSubsetOfAllTheSame(); //at least 2 entities with the same (P, O)s
            if (i + sizeOfSubset >= numberOfExactlyTheSamePO - 1)
                sizeOfSubset = numberOfExactlyTheSamePO - i; //for not having single subset (or more than expected) at the end
            Resource[] generatedResources = new Resource[sizeOfSubset];

            for (int j = 0; j < sizeOfSubset; j++) {
                Resource s = ResourceFactory.createResource("http://example.de/resource" + cntResource);
                ret.add(new StatementImpl(s, RDFS.label, ResourceFactory.createStringLiteral(Integer.toString(cntResource))));
                generatedResources[j] = s;
                cntResource++;
            }
            int numberOfProperties = propertiesGenerator.getNumberOfProperties();//maximum 8 po and at least one
            for (int k = 0; k < numberOfProperties; k++) {
                Property p = ResourceFactory.createProperty("http://example.de/property" + cntProperty++);
                RDFNode o = ResourceFactory.createStringLiteral("" + cntObject++);
                Arrays.stream(generatedResources).forEach(s -> ret.add(new StatementImpl(s, p, o)));
            }
            i += sizeOfSubset;
        }
        return ret;
    }
}
