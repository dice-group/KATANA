package org.aksw.katana.service.impl;

import org.aksw.katana.service.SparQL;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

//@Service
//@Profile("remote")
//public class RemoteSparQL implements SparQL {
//
//    private final String sparqlService;
//
//    @Autowired
//    public RemoteSparQL(@Value("${SparQL.sparqlService}") String sparqlService) {
//        this.sparqlService = sparqlService;
//    }
//
//    @Override
//    public QueryExecution createQueryExecution(Query query) {
//        return QueryExecutionFactory.sparqlService(sparqlService, query);
//    }
//}
