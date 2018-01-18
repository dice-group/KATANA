package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public abstract class SparqlHandler {
	private static String endpoint = "http://dbpedia.org/sparql";
	private static String graph = "http://dbpedia.org";

	public static List<Triple> getResources(String className) {
		List<Triple> results = new ArrayList<>();
		String sparqlQueryString = SparqlQueries.getResourceQuery(className);
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

	public static Model getCBD(String resourceURI) {
		String sparqlQueryString = SparqlQueries.getCBDQuery(resourceURI);
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		QueryEngineHTTP qeHttp = (QueryEngineHTTP) qexec;
		qeHttp.setModelContentType("application/rdf+xml");
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public static List<Triple> getFunctionalPropertyResources(String className) {
		List<Triple> results = new ArrayList<>();
		String sparqlQueryString = SparqlQueries.getFunctionalPropertiesResourcesQuery(className);
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

	public static List<Node> executeSingle(String sparqlQuery) {
		List<Node> results = new ArrayList<>();
		QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, graph);
		ResultSet queryResults = qexec.execSelect();
		while (queryResults.hasNext()) {
			QuerySolution qs = queryResults.nextSolution();
			results.add(qs.get("?s").asNode());
		}
		qexec.close();

		return results;

	}

	public static List<Triple> executeTriple(String sparqlQuery) {
		List<Triple> results = new ArrayList<>();
		QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, graph);
		ResultSet queryResults = qexec.execSelect();
		while (queryResults.hasNext()) {
			QuerySolution qs = queryResults.nextSolution();
			results.add(new Triple(qs.get("?s").asNode(), qs.get("?p").asNode(), qs.get("?o").asNode()));
		}
		qexec.close();

		return results;

	}

}
