package org.aksw.katana.evaluation.benchmark.enhanced;

import org.aksw.katana.algorithm.KATANA;
import org.aksw.katana.evaluation.*;
import org.aksw.katana.service.impl.InMemorySparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

@Component
@Profile("enhancedBenchmark")
public class EnhancedBenchMark {

    private final ApplicationContext context;


    @Autowired
    public EnhancedBenchMark(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public Boolean doBenchMark() throws FileNotFoundException {

        int max = 100;

        List[][] allResult = new List[max + 1][max + 1];

        for (int exactlyTheSamePO_percentage = 0; exactlyTheSamePO_percentage <= max; exactlyTheSamePO_percentage++) {
            innerLoop(allResult, exactlyTheSamePO_percentage);
        }

        PrintStream printStream = new PrintStream("/home/afshin/Desktop/res.csv");
        for (int exactlyTheSamePO_percentage = 0; exactlyTheSamePO_percentage <= max; exactlyTheSamePO_percentage++)
            for (int shareSomePO_percentage = 0; shareSomePO_percentage <= max - exactlyTheSamePO_percentage; shareSomePO_percentage++) {
                List<Pair<Triple<String, Integer, Double>, Boolean>> res = allResult[exactlyTheSamePO_percentage][shareSomePO_percentage];
                int all = 0;
                int correct = 0;
                for (Pair<Triple<String, Integer, Double>, Boolean> x : res) {
                    all++;
                    correct += x.getValue() ? 1 : 0;
                    printStream.println(String.format("label, %s, difficulty, %f, correctness, %b", x.getKey().getLeft(), x.getKey().getRight(), x.getValue()));
                }
                printStream.println("exactlyTheSamePO_percentage, " + exactlyTheSamePO_percentage + ", shareSomePO_percentage, " + shareSomePO_percentage + ", accuracy, " + (double) (correct) / all);
                printStream.flush();
            }

        printStream.close();
        return true;
    }

    @Async
    public void innerLoop(List[][] allResult, int exactlyTheSamePO_percentage) {
        for (int shareSomePO_percentage = 0; shareSomePO_percentage <= 100 - exactlyTheSamePO_percentage; shareSomePO_percentage++) {
            GraphProperties graphProperties = context.getBean(GraphProperties.class);
            graphProperties.setAllEntities(100);
            graphProperties.setExactlyTheSamePO_percentage(exactlyTheSamePO_percentage);
            graphProperties.setShareSomePO_percentage(shareSomePO_percentage);
            graphProperties.setNumberOfProperties(6);
            graphProperties.setNumberOfShareCandidate(4);
            graphProperties.setSizeOfOneSubsetOfShareSome(5);
            graphProperties.setSizeOfOneSubsetOfAllTheSame(4);

            PropertiesGenerator propertiesGenerator = context.getBean(PropertiesGenerator.class, graphProperties);

            KnowledgeBaseGenerator knowledgeBaseGenerator = context.getBean(KnowledgeBaseGenerator.class, propertiesGenerator, graphProperties);

            BenchMarkInMemoryTripleStore inMemoryTripleStore = context.getBean(BenchMarkInMemoryTripleStore.class, knowledgeBaseGenerator);

            inMemoryTripleStore.populateModel();

            InMemorySparQL sparQL = context.getBean(InMemorySparQL.class, inMemoryTripleStore);

            SparqlUtility sparqlUtility = context.getBean(SparqlUtility.class, sparQL);

            KATANA katana = context.getBean(KATANA.class, sparQL);

            EnhancedBenchMarkEvaluation enhancedBenchMarkEvaluation = context.getBean(EnhancedBenchMarkEvaluation.class, katana, sparqlUtility);

            List<Pair<Triple<String, Integer, Double>, Boolean>> res = enhancedBenchMarkEvaluation.call();

            allResult[exactlyTheSamePO_percentage][shareSomePO_percentage] = res;
        }
    }
}
