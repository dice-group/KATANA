package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class SparqlHandler {

	private String endpoint = "http://dbpedia.org/sparql";
	private String graph = "http://dbpedia.org";

	public List<Resource> getResources(String classname) {
		List<Resource> results = new ArrayList<Resource>();
		String sparqlQueryString = "SELECT DISTINCT ?s { ?s a <" + classname + "> }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet queryResults = qexec.execSelect();
		while (queryResults.hasNext()) {
			QuerySolution qs = queryResults.nextSolution();
			results.add(qs.getResource("?s"));
		}
		qexec.close();
		return results;
	}

	public Model getCBD(Resource r) {
		String sparqlQueryString = "DESCRIBE <" + r + ">";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		QueryEngineHTTP qeHttp = (QueryEngineHTTP) qexec;
		qeHttp.setModelContentType("application/rdf+xml");
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public ResultSet getFunctionalProperties() {
		List<Resource> results = new ArrayList<Resource>();
		String sparqlQueryString = "select distinct ?p where {?p a <http://www.w3.org/2002/07/owl#FunctionalProperty>}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet cbds = qexec.execSelect();
		return cbds;

	}

	public static void main(String[] args) {
		String classname = "http://dbpedia.org/ontology/City";
		SparqlHandler c = new SparqlHandler();
		Model m = ModelFactory.createDefaultModel();
		List<Resource> x = c.getResources(classname);
		ResultSet a = c.getFunctionalProperties();
		ResultSetFormatter.outputAsJSON(System.out, a);
		// m.add(c.getCBD(x.get(0)));
		/*
		 * for (Resource r : c.getNResources(classname)) { m.add(c.getCBD(r)); }
		 */
		// m.write(System.out, "TTL");

	}

}
