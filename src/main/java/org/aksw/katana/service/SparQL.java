package org.aksw.katana.service;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.springframework.stereotype.Service;

@Service
public interface SparQL {

    QueryExecution createQueryExecution(Query query);

}
