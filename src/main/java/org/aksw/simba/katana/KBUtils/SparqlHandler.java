package org.aksw.simba.katana.KBUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
		String sparqlQueryString = "SELECT DISTINCT ?s { ?s a <" + classname + "> } LIMIT 2";
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

	public void generateSampleDataset(ArrayList<String> classNames) throws IOException {
		FileWriter fw = new FileWriter("src/main/resources/abc.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter printWriter = new PrintWriter(bw);

		for (String clas : classNames) {
			List<Resource> resList = new ArrayList<Resource>();
			resList = this.getResources(clas);
			for (Resource res : resList) {
				String sparqlQueryString = " prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n select ?abstract where {<"
						+ res + "> dbpedia-owl:abstract ?abstract. \n filter(langMatches(lang(?abstract),\"en\"))\n}";
				QueryFactory.create(sparqlQueryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQueryString, graph);
				ResultSet queryResults = qexec.execSelect();
				while (queryResults.hasNext()) {
					QuerySolution qs = queryResults.nextSolution();
					printWriter.println(qs.getLiteral("abstract").getString());
				}

			}
		}
		bw.close();
		fw.close();
	}

	public static void main(String[] args) {
		ArrayList<String> classNames = new ArrayList<String>(Arrays.asList("http://dbpedia.org/ontology/person",
				"http://dbpedia.org/ontology/City", "http://dbpedia.org/ontology/Building"));
		SparqlHandler sh = new SparqlHandler();
		try {
			sh.generateSampleDataset(classNames);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
