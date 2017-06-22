package org.aksw.simba.katana.KBUtils;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.katana.model.RDFProperty;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

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

		String sparqlQueryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "select ?p  ?label where \n{?p a <http://www.w3.org/2002/07/owl#FunctionalProperty>. \n ?p rdfs:label ?label.\n FILTER (lang(?label) = 'en').}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet funcProperties = qexec.execSelect();
		// ResultSetFormatter.out(System.out, funcProperties);
		return funcProperties;

	}

	public ArrayList<RDFProperty> getPropertyList() {
		ArrayList<RDFProperty> listOfProperties = new ArrayList<RDFProperty>();

		ResultSet funcProp = this.getFunctionalProperties();
		while (funcProp.hasNext()) {
			listOfProperties.add(new RDFProperty(funcProp.next().getResource("p").toString(),
					funcProp.next().getLiteral("label").getString()));
		}
		return listOfProperties;
	}

}
