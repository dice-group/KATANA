package org.aksw.simba.KB;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class KBCBDBuilder {

	private String endpoint = "http://dbpedia.org/sparql";
	private String graph = "http://dbpedia.org";

	public List<Resource> getNResources(String classname) {
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

	public static void main(String[] args) {
		String classname = "http://dbpedia.org/ontology/City";
		KBCBDBuilder c = new KBCBDBuilder();
		Model m = ModelFactory.createDefaultModel();
		List<Resource> x = c.getNResources(classname);

		m.add(c.getCBD(x.get(0)));
		/*
		 * for (Resource r : c.getNResources(classname)) { m.add(c.getCBD(r)); }
		 */
		m.write(System.out, "TTL");

	}

}
