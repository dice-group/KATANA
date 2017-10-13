package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.katana.model.RDFProperty;
import org.aksw.simba.katana.model.RDFResource;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class SparqlHandler {
	SparqlQueries queryHandler = new SparqlQueries();
	private String endpoint = "http://dbpedia.org/sparql";
	private String graph = "http://dbpedia.org";

	public List<Triple> getResources(String classname) {
		List<Triple> results = new ArrayList<Triple>();
		String sparqlQueryString = queryHandler.getResourceQuery(classname);
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet queryResults = qexec.execSelect();
		while (queryResults.hasNext()) {
			QuerySolution qs = queryResults.nextSolution();
			results.add(new Triple(qs.get("?s").asNode(), qs.get("?p").asNode(), qs.get("?o").asNode()));
		}
		qexec.close();

		return results;
	}

	public Model getCBD(Resource r) {
		String sparqlQueryString = queryHandler.getCBDQuery(r);
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		QueryEngineHTTP qeHttp = (QueryEngineHTTP) qexec;
		qeHttp.setModelContentType("application/rdf+xml");
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public List<Triple> getFunctionalPropertyResources(String classname) {
		List<Triple> results = new ArrayList<Triple>();
		String sparqlQueryString = queryHandler.getFunctionalPropertiesResourcesQuery(classname);
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet queryResults = qexec.execSelect();
		while (queryResults.hasNext()) {
			QuerySolution qs = queryResults.nextSolution();
			results.add(new Triple(qs.get("?s").asNode(), qs.get("?p").asNode(), qs.get("?o").asNode()));
		}
		qexec.close();

		return results;

	}

}
