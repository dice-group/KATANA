package org.aksw.katana.evaluation.benchmark;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@Component
@Getter
@Setter
@ConfigurationProperties("graph")
public class GraphProperties {
    @Min(1)
    private int allEntities;

    @Min(0)
    @Max(100)
    private int exactlyTheSamePO_percentage;
    @Min(0)
    @Max(100)
    private int shareSomePO_percentage;
    @Min(1)
    private int numberOfShareCandidate;
    @Min(1)
    private int sizeOfOneSubsetOfShareSome;
    @Min(1)
    private int sizeOfOneSubsetOfAllTheSame;
    @Min(1)
    private int numberOfProperties;

    private String outputFilePath;
}
