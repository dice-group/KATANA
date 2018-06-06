package org.aksw.katana.evaluation.benchmark;

import org.aksw.katana.algorithm.KATANA;
import org.aksw.katana.service.SparQL;
import org.aksw.katana.service.impl.InMemorySparQL;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.text.DefaultCaret;
import java.io.FileNotFoundException;
import java.io.PrintStream;

@Component
@Profile("benchmark")
public class BenchMark {

    private final ApplicationContext context;


    @Autowired
    public BenchMark(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public Boolean doBenchMark() throws FileNotFoundException {

        PrintStream printStream = new PrintStream("/home/afshin/Desktop/res.csv");

        for(int exactlyTheSamePO_percentage = 0; exactlyTheSamePO_percentage <= 10; exactlyTheSamePO_percentage++)
            for(int shareSomePO_percentage = 0; shareSomePO_percentage < 10; shareSomePO_percentage++) {
                GraphProperties graphProperties = context.getBean(GraphProperties.class);
                graphProperties.setAllEntities(1000);
                graphProperties.setExactlyTheSamePO_percentage(exactlyTheSamePO_percentage);
                graphProperties.setShareSomePO_percentage(shareSomePO_percentage);
                graphProperties.setNumberOfProperties(8);
                graphProperties.setNumberOfShareCandidate(4);
                graphProperties.setSizeOfOneSubsetOfShareSome(5);
                graphProperties.setSizeOfOneSubsetOfAllTheSame(8);
                graphProperties.setNumberOfProperties(8);

                PropertiesGenerator propertiesGenerator = context.getBean(PropertiesGenerator.class, graphProperties);

                KnowledgeBaseGenerator knowledgeBaseGenerator = context.getBean(KnowledgeBaseGenerator.class, propertiesGenerator, graphProperties);

                BenchMarkInMemoryTripleStore inMemoryTripleStore = context.getBean(BenchMarkInMemoryTripleStore.class, knowledgeBaseGenerator);

                inMemoryTripleStore.populateModel();

                InMemorySparQL sparQL = context.getBean(InMemorySparQL.class, inMemoryTripleStore);

                SparqlUtility sparqlUtility = context.getBean(SparqlUtility.class, sparQL);

                KATANA katana = context.getBean(KATANA.class, sparQL);

                BenchMarkEvaluation benchMarkEvaluation = context.getBean(BenchMarkEvaluation.class, katana, sparqlUtility);

                Pair<Integer, Integer> res = benchMarkEvaluation.call();
                System.out.println(res.getLeft() + " of " + res.getRight());
                printStream.println(exactlyTheSamePO_percentage + "," + shareSomePO_percentage + "," +res.getLeft() + "," + res.getRight());
            }
            printStream.close();
        return true;
    }
}
