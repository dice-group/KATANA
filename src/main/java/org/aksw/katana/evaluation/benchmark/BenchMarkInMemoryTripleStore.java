package org.aksw.katana.evaluation.benchmark;

import org.aksw.katana.service.InMemoryTripleStore;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Profile({"benchmark"})
public class BenchMarkInMemoryTripleStore implements InMemoryTripleStore {

    private final KnowledgeBaseGenerator knowledgeBaseGenerator;
    private Model model;

    @Autowired
    public BenchMarkInMemoryTripleStore(KnowledgeBaseGenerator knowledgeBaseGenerator) {
        this.knowledgeBaseGenerator = knowledgeBaseGenerator;
    }

    @PostConstruct
    public void populateModel(){
        model = knowledgeBaseGenerator.generate();
    }

    @Override
    public Model getModel() {
        return model;
    }
}
