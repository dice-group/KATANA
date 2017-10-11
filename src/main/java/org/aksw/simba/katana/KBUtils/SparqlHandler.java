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

	public ArrayList<RDFProperty> getFunctionalProperties() {
		ArrayList<RDFProperty> listOfProperties = new ArrayList<RDFProperty>();
		String sparqlQueryString = queryHandler.getFunctionalPropertiesQuery();
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
		ResultSet funcProperties = qexec.execSelect();
		while (funcProperties.hasNext()) {
			listOfProperties.add(new RDFProperty(funcProperties.next().getResource("p").toString(),
					funcProperties.next().getLiteral("label").getString()));
		}
		return listOfProperties;

	}

	public Map<RDFProperty, ArrayList<RDFResource>> getPropertyResourceMap() {
		ArrayList<RDFProperty> listOfProperties = this.getFunctionalProperties();
		Map<RDFProperty, ArrayList<RDFResource>> map = new HashMap<RDFProperty, ArrayList<RDFResource>>();
		for (RDFProperty prop : listOfProperties) {
			ArrayList<RDFResource> res = new ArrayList<RDFResource>();
			String sparqlQueryString = queryHandler.getResourceMapQuery(prop.getUri());
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
			ResultSet qres = qexec.execSelect();
			while (qres.hasNext()) {
				String uri = qres.next().getResource("s").toString();
				String[] label = uri.split("/");
				String x = label[label.length - 1];
				res.add(new RDFResource(uri, x));
			}
			map.put(prop, res);
		}
		return map;
	}

}
