package org.aksw.katana.service;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public interface SparQL {

    QueryExecution createQueryExecution(Query query);

}
