package org.aksw.katana.service.impl;

import org.aksw.katana.service.InMemoryTripleStore;
import org.aksw.katana.service.SparQL;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InMemorySparQL implements SparQL {

    private final InMemoryTripleStore inMemoryTripleStore;

    @Autowired
    public InMemorySparQL(InMemoryTripleStore inMemoryTripleStore) {
        this.inMemoryTripleStore = inMemoryTripleStore;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return QueryExecutionFactory.create(query, inMemoryTripleStore.getModel());
    }
}
