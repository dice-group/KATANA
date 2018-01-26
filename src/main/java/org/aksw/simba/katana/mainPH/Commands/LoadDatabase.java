package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.mainPH.Main;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * see help
 *
 * @author Philipp Heinisch
 */
public class LoadDatabase implements Command {

    private Logger log = LogManager.getLogger(LoadDatabase.class);

    public LoadDatabase() {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + LoadDatabase.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + LoadDatabase.class.getName() + "!");
    }

    /**
     * A short description of the command
     *
     * @return the output String
     */
    @Override
    public String getHelp() {
        return "Creates a database or overwrite it. For the, DBpedia with the Sparql-Endpoint is used!";
    }

    /**
     * A long description of the command including the description of the parameters
     *
     * @return the output string
     */
    @Override
    public String getLongHelp() {
        return getHelp() + System.lineSeparator() +
                "Parameters:" + System.lineSeparator() +
                "--f or --force: don't cancel the command, even if there exist already a database" + System.lineSeparator() +
                "--domain <String>: load only tuples from the DBpedia in that certain className" + System.lineSeparator() +
                "--limit <int>: limits the number of subjects" + System.lineSeparator() +
                "--in: search for each subject ingoing edges, too, not only the outgoing one" + System.lineSeparator() +
                "--s or --soft: don't set the internal database, if the result is empty";
    }

    /**
     * Executes the commands
     *
     * @param params the given params
     * @return {@code true}, if the execution succeeded, otherwise {@code false}
     */
    @Override
    public boolean execute(Map<String, String> params) {
        boolean force = Main.Get(params, "--f").isPresent() || Main.Get(params, "--force").isPresent();
        boolean saveDatabaseIfEmpty = !(Main.Get(params, "--s").isPresent() || Main.Get(params, "--soft").isPresent());
        boolean inGoing = Main.Get(params, "--in").isPresent();
        StringBuilder domain = new StringBuilder("http://dbpedia.org/ontology/");
        Main.Get(params, "--domain").ifPresent(m -> domain.append(m.getValue()));
        if (domain.toString().equals("http://dbpedia.org/ontology/")) {
            domain.append("Person");
        }
        int limit = 10;
        if (Main.Get(params, "--limit").isPresent()) {
            try {
                limit = Integer.parseInt(Main.Get(params, "--limit").get().getValue());
            } catch (NumberFormatException e) {
                limit = 100;
                log.warn("Can not convert the input for --limit (" + Main.Get(params, "--limit").get() + "). Cancel the converting process and set the variable to " + limit, e);
            }
        }

        log.debug("All params were read. Your domain is " + domain + " with the limit " + limit + ". You will " + ((force) ? "" : "not") + " force the command.");


        //Load subjects
        //String sparqlQuery = "select distinct ?s ?p ?o" + System.lineSeparator()
        //        + "where {?s ?p ?o." + System.lineSeparator() + "?s a <" + domain + ">." + System.lineSeparator()
        //        + "?p a <http://www.w3.org/2002/07/owl#FunctionalProperty>." + "} LIMIT " + limit;

        String sparqlQuery = "select distinct ?s" + System.lineSeparator()
                + "where {?s ?p ?o." + System.lineSeparator() + "?s a <" + domain + ">." + System.lineSeparator()
                + "} LIMIT " + limit;

        log.trace("Execute the following sparqlQuery: " + sparqlQuery);

        List<Node> resultSubjects = SparqlHandler.executeSingle(sparqlQuery);
        log.info("The Query to get subjects was executed. Result contains " + resultSubjects.size() + " triples.");

        if (params.containsKey("--domain") && resultSubjects.isEmpty()) {
            log.info("Your result is empty. Please look again to your --domain input! E.g., if you write \"person\" instead of \"Person\", you get nothing. Try it out e.g. with \"Software\" :)");
        }

        //load triples for each subjects
        List<List<Triple>> result = new ArrayList<>(resultSubjects.size());
        for (Node s : resultSubjects) {
            log.trace("Search for triples for " + s.getURI() + ". First OUT-Edges:");
            String sparqlQuery2 = "select distinct ?s ?p ?o" + System.lineSeparator()
                    + "where {?s ?p ?o." + System.lineSeparator() + "?p a <http://www.w3.org/2002/07/owl#ObjectProperty>." + System.lineSeparator()
                    + "FILTER(?s = <" + s.getURI() + ">)."
                    + "}";
            List<Triple> resultSpecificSubject = SparqlHandler.executeTriple(sparqlQuery2);
            if (inGoing) {
                log.trace("Search for triples for " + s.getURI() + ". Now IN-Edges:");
                sparqlQuery2 = "select distinct ?s ?p ?o" + System.lineSeparator()
                        + "where {?s ?p ?o." + System.lineSeparator() + "?p a <http://www.w3.org/2002/07/owl#ObjectProperty>." + System.lineSeparator()
                        + "FILTER(?o = <" + s.getURI() + ">)."
                        + "}";
                resultSpecificSubject.addAll(SparqlHandler.executeTriple(sparqlQuery2));
            }
            log.trace("Search for triples for " + s.getURI() + ". Now Labels:");
            sparqlQuery2 = "select distinct ?s ?p ?o" + System.lineSeparator()
                    + "where {?s ?p ?o." + System.lineSeparator()
                    + "FILTER(?s = <" + s.getURI() + ">)."
                    + "FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>)." + System.lineSeparator()
                    + "} LIMIT 1";
            resultSpecificSubject.addAll(SparqlHandler.executeTriple(sparqlQuery2));
            log.debug("Found " + resultSpecificSubject.size() + " Triples including 1 label for " + s.getURI());

            result.add(resultSpecificSubject);
        }

        if (saveDatabaseIfEmpty || !result.isEmpty()) {
            if (force || Main.database == null) {
                Main.database = result;
                Main.subjects = resultSubjects;
                log.trace("Database was saved...");
                return true;
            } else {
                log.warn("There is already a database, use the \"--force\"-command to overwrite them!");
                return false;
            }
        } else {
            log.info("Your result is empty and you want to abort the command therefore. It's ok!");
            return true;
        }
    }
}