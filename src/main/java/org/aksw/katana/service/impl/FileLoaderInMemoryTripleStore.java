package org.aksw.katana.service.impl;

import org.aksw.katana.service.InMemoryTripleStore;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Profile("InMemory")
public class FileLoaderInMemoryTripleStore implements InMemoryTripleStore {// TODO: 5/14/18 Better name

//    @Value("${info.SparQL.FileAddress}")
//    private String sparqlFileAddress;

    private Model model;

    public FileLoaderInMemoryTripleStore(@Value("${info.SparQL.FileAddress}") String sparqlFileAddress) {
        model = RDFDataMgr.loadModel(sparqlFileAddress);
    }

    @Override
    public Model getModel() {
        return model;
    }

//    @PostConstruct
//    public void constructInMemoryTripleStore() {
//        model = RDFDataMgr.loadModel(sparqlFileAddress);
//    }
}
