package org.aksw.katana.evaluation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile({"random"})
public class RandomPropertiesGenerator implements PropertiesGenerator {
    private static final Random random = new Random();

    @Value("${info.numberOfShareCandidate}")
    private int numberOfShareCandidate;
    @Value("${info.sizeOfOneSubsetOfShareSome}")
    private int sizeOfOneSubsetOfShareSome;
    @Value("${info.sizeOfOneSubsetOfAllTheSame}")
    private int sizeOfOneSubsetOfAllTheSame;
    @Value("${info.numberOfProperties}")
    private int numberOfProperties;

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
        return Math.min(numberOfShareCandidate, max);
    }

    private int getSizeOfOneSubsetOfShareSome(int numberOfProperties) {
        int max = Math.max(1, random.nextInt(numberOfProperties + 1));
        return Math.min(sizeOfOneSubsetOfShareSome, max);
    }

    @Override
    public int getSizeOfOneSubsetOfAllTheSame() {
        return random.nextInt(sizeOfOneSubsetOfAllTheSame) + 2;
    }

    @Override
    public int getNumberOfProperties() {
        return random.nextInt(numberOfProperties) + 1;
    }

}
