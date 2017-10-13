package org.aksw.simba.katana.KBUtils;

import org.apache.jena.rdf.model.Resource;

public class SparqlQueries {

	public String getResourceQuery(String classname) {
		String sparqlQueryString = "SELECT DISTINCT ?s ?p ?o { ?s ?p ?o. \n ?s a <" + classname + ">. } ";
		System.out.println("Query Executed :  \n " + sparqlQueryString);
		return sparqlQueryString;

	}

	public String getCBDQuery(String resourceURI) {
		String sparqlQueryString = "DESCRIBE <" + resourceURI + ">";
		System.out.println("Query Executed :  \n " + sparqlQueryString);
		return sparqlQueryString;

	}

	public String getFunctionalPropertiesResourcesQuery(String classname) {
		String sparqlQueryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "select ?s ?p ?o  \n"
				+ "where {\n" + "?s ?p ?o. \n" + "?s a <" + classname + ">. \n"
				+ "?p a <http://www.w3.org/2002/07/owl#FunctionalProperty>. \n" + "} LIMIT 10";
		System.out.println("Query Executed :  \n " + sparqlQueryString);
		return sparqlQueryString;

	}

	public String getSampleDatasetQuery(Resource res) {
		String sparqlQueryString = " prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n select ?abstract where {<"
				+ res + "> dbpedia-owl:abstract ?abstract. \n filter(langMatches(lang(?abstract),\"en\"))\n}";
		System.out.println("Query Executed :  \n " + sparqlQueryString);
		return sparqlQueryString;

	}

}
