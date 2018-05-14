package org.aksw.katana.service;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

@Component
public interface InMemoryTripleStore {

    Model getModel();
}
