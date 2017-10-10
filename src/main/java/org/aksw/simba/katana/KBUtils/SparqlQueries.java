package org.aksw.simba.katana.KBUtils;

import org.apache.jena.rdf.model.Resource;

public class SparqlQueries {

	public String getResourceQuery(String classname) {
		String sparqlQueryString = "SELECT DISTINCT ?s ?p ?o{ s ?p ?o.  ?s a <" + classname + "> } ";
		return sparqlQueryString;

	}

	public String getCBDQuery(Resource r) {
		String sparqlQueryString = "DESCRIBE <" + r + ">";
		return sparqlQueryString;

	}

	public String getFunctionalPropertiesQuery() {
		String sparqlQueryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "Select ?p  ?label where \n{?p a <http://www.w3.org/2002/07/owl#FunctionalProperty>. \n ?p rdfs:label ?label.\n FILTER (lang(?label) = 'en').}";
		return sparqlQueryString;

	}

	public String getResourceMapQuery(String propUri) {
		String sparqlQueryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "select distinct ?s ?label  where  \n{ {?s <" + propUri
				+ "> ?p .\n ?s rdfs:label ?label. FILTER(!isLiteral(?s) && (lang(?label) = 'en'))} \n union  \n  { ?x <"
				+ propUri + "> ?s. \n ?s rdfs:label ?label. FILTER(!isLiteral(?s) && (lang(?label) = 'en'))}}LIMIT 100";
		return sparqlQueryString;

	}

	public String getSampleDatasetQuery(Resource res) {
		String sparqlQueryString = " prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n select ?abstract where {<"
				+ res + "> dbpedia-owl:abstract ?abstract. \n filter(langMatches(lang(?abstract),\"en\"))\n}";
		return sparqlQueryString;

	}

}
