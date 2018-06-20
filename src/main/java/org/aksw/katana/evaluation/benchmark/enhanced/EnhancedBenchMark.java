package org.aksw.katana.evaluation.benchmark.enhanced;

import org.aksw.katana.algorithm.KATANA;
import org.aksw.katana.evaluation.*;
import org.aksw.katana.service.impl.InMemorySparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Profile("enhancedBenchmark")
public class EnhancedBenchMark {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedBenchMark.class);

    private final ApplicationContext context;

    @Value("${info.outputFileAddress}")
    private String outputFileAddress;

    @Value("${info.maxPercentage}")
    private int maxPercentage;


    @Autowired
    public EnhancedBenchMark(ApplicationContext context) {
        this.context = context;
    }


    @Bean
    public Boolean doBenchMark() {
        try {
            Future[][] allResult = new Future[maxPercentage + 1][maxPercentage + 1];

            ExecutorService executorService = Executors.newSingleThreadExecutor();

            for (int exactlyTheSamePO_percentage = 0; exactlyTheSamePO_percentage <= maxPercentage; exactlyTheSamePO_percentage++) {
                for (int shareSomePO_percentage = 0; shareSomePO_percentage <= maxPercentage; shareSomePO_percentage++) {
                    if (exactlyTheSamePO_percentage + shareSomePO_percentage > 100) continue;
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

                    allResult[exactlyTheSamePO_percentage][shareSomePO_percentage] = executorService.submit(enhancedBenchMarkEvaluation);
                }
            }

            PrintStream printStream = new PrintStream(outputFileAddress);

            for (int exactlyTheSamePO_percentage = 0; exactlyTheSamePO_percentage <= maxPercentage; exactlyTheSamePO_percentage++)
                for (int shareSomePO_percentage = 0; shareSomePO_percentage <= maxPercentage; shareSomePO_percentage++) {
                    if (exactlyTheSamePO_percentage + shareSomePO_percentage > 100) continue;
                    List<Pair<Triple<String, Integer, Double>, Boolean>> res = null;
                    try {
                        res = ((Future<List<Pair<Triple<String, Integer, Double>, Boolean>>>) allResult[exactlyTheSamePO_percentage][shareSomePO_percentage]).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    logger.info("receive result exactlyTheSamePO_percentage: {}, shareSomePO_percentage: {}, HaveResult: {} ", exactlyTheSamePO_percentage, shareSomePO_percentage, res != null);
                    if (res == null) continue;
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

            executorService.shutdown();
        } catch (BeansException | FileNotFoundException e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }

        return true;
    }
}
