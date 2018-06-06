package org.aksw.katana.evaluation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile({"static"})
@Scope("prototype")
public class StaticPropertiesGenerator implements PropertiesGenerator {

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
    public StaticPropertiesGenerator(GraphProperties graphProperties) {
        this.graphProperties = graphProperties;
    }

    @Override
    public List<Integer> getShareCandidateIndices(int i, int numberOfShareSomePO) {
        List<Integer> ret = new ArrayList<>();
        int numberOfShareCandidate = getNumberOfShareCandidate(i, numberOfShareSomePO);

        for (int step = 1, j = 0; j < numberOfShareCandidate; step *= 2, j++)
            if (i + step >= numberOfShareSomePO) break;
            else ret.add(i + step);

        return ret;
    }

    @Override
    public List<Integer> getSharePO_Indices(int numberOfProperties) {
        List<Integer> ret = new ArrayList<>();
        int subSetSize = getSizeOfOneSubsetOfShareSome(numberOfProperties);

        for (int i = 0; i < subSetSize; i++)
            ret.add(i);

        return ret;
    }

    private int getNumberOfShareCandidate(int i, int numberOfShareSomePO) {
        return numberOfShareSomePO - i - 1 > graphProperties.getNumberOfShareCandidate() ? graphProperties.getNumberOfShareCandidate() : numberOfShareSomePO - i - 1;
    }

    private int getSizeOfOneSubsetOfShareSome(int numberOfProperties) {
        return graphProperties.getSizeOfOneSubsetOfShareSome();
    }

    @Override
    public int getSizeOfOneSubsetOfAllTheSame() {
        return graphProperties.getSizeOfOneSubsetOfAllTheSame();
    }

    @Override
    public int getNumberOfProperties() {
        return graphProperties.getNumberOfProperties();
    }

}
