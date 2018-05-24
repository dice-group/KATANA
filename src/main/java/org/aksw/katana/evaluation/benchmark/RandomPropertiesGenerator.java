package org.aksw.katana.evaluation.benchmark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile({"random"})
@PropertySource("classpath:application-benchmark.properties")
public class RandomPropertiesGenerator implements PropertiesGenerator {
    private static final Random random = new Random();

    //    @Value("${graph.numberOfShareCandidate}")
//    private int numberOfShareCandidate;
//    @Value("${graph.sizeOfOneSubsetOfShareSome}")
//    private int sizeOfOneSubsetOfShareSome;
//    @Value("${graph.sizeOfOneSubsetOfAllTheSame}")
//    private int sizeOfOneSubsetOfAllTheSame;
//    @Value("${graph.numberOfProperties}")
//    private int numberOfProperties;
    private final GraphProperties graphProperties;

    @Autowired
    public RandomPropertiesGenerator(GraphProperties graphProperties) {
        this.graphProperties = graphProperties;
    }

    @Override
    public List<Integer> getShareCandidateIndices(int i, int numberOfShareSomePO) {
        Set<Integer> ret = new HashSet<>();
        int numberOfShareCandidate = getNumberOfShareCandidate(i, numberOfShareSomePO);

        for (int j = 0; j < numberOfShareCandidate; ) {
            int temp = Math.max(1 + i, random.nextInt(numberOfShareSomePO - i) + i);
            if (ret.contains(temp)) continue;
            ret.add(temp);
            j++;
        }

        List<Integer> integers = new ArrayList<>(ret);
        return integers;
    }

    @Override
    public List<Integer> getSharePO_Indices(int numberOfProperties) {
        Set<Integer> ret = new HashSet<>();
        int subSetSize = getSizeOfOneSubsetOfShareSome(numberOfProperties);

        for (int i = 0; i < subSetSize; ) {
            int temp = random.nextInt(numberOfProperties);
            if (ret.contains(temp)) continue;
            ret.add(temp);
            i++;
        }

        List<Integer> integers = new ArrayList<>(ret);
        return integers;
    }

    private int getNumberOfShareCandidate(int i, int numberOfShareSomePO) {
        int max = Math.max(1, random.nextInt(numberOfShareSomePO - i));
        return Math.min(graphProperties.getNumberOfShareCandidate(), max);
    }

    private int getSizeOfOneSubsetOfShareSome(int numberOfProperties) {
        int max = Math.max(1, random.nextInt(numberOfProperties + 1));
        return Math.min(graphProperties.getSizeOfOneSubsetOfShareSome(), max);
    }

    @Override
    public int getSizeOfOneSubsetOfAllTheSame() {
        return random.nextInt(graphProperties.getSizeOfOneSubsetOfAllTheSame()) + 2;
    }

    @Override
    public int getNumberOfProperties() {
        return random.nextInt(graphProperties.getNumberOfProperties()) + 1;
    }

}
