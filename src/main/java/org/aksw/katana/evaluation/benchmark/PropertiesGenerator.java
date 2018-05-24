package org.aksw.katana.evaluation.benchmark;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"benchmark","test"})
@Scope("prototype")
public interface PropertiesGenerator {
    List<Integer> getShareCandidateIndices(int i, int numberOfShareSomePO);

    List<Integer> getSharePO_Indices(int numberOfProperties);

    int getSizeOfOneSubsetOfAllTheSame();

    int getNumberOfProperties();
}
