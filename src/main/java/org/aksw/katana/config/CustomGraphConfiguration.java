package org.aksw.katana.config;

//import org.springframework.context.annotation.Profile;

import org.aksw.katana.evaluation.benchmark.GraphProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GraphProperties.class)
public class CustomGraphConfiguration {
}
