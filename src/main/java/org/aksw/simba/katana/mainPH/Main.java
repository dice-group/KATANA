package org.aksw.simba.katana.mainPH;

import org.aksw.simba.katana.KBUtils.KBEvaluationHandler;
import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.mainPH.Commands.*;
import org.aksw.simba.katana.mainPH.View.JENAtoCONSOLE;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class);
    public static Map<String, Command> commands = new HashMap<>();
    public static List<List<Triple>> database = null;
    public static List<Node> subjects = null;

    public static void main(String[] args) {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + Main.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + Main.class.getName() + "!");

        //Command power
        commands.put("exit", new Exit());
        commands.put("help", new Help());
        commands.put("load", new LoadDatabase());
        commands.put("edit", new EditDatabase());
        commands.put("print", new PrintDatabase());

        boolean allowInput = true;
        log.trace("Application is loaded...");
        log.debug("Java version " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + "/" + System.getProperty("os.version") + System.getProperty("os.arch"));
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        while (allowInput) {
            String input = null;
            try {
                input = bufferRead.readLine();
            } catch (IOException e) {
                log.fatal("Failed to read the user input", e);
                e.printStackTrace();
                return;
            }
            String[] parts = input.split("\\s");
            Optional<Map.Entry<String, Command>> com = Get(parts[0].trim());
            if (com.isPresent()) {
                Map<String, String> params = new HashMap<>();
                String paramCommandString = null;
                StringBuilder paramArgumentString = new StringBuilder("");
                ArrayList<String> paramsList = new ArrayList<>(Arrays.asList(parts));
                paramsList.remove(0);
                for (String part : paramsList) {
                    if (part.startsWith("--")) {
                        if (paramCommandString != null) {
                            params.putIfAbsent(paramCommandString, paramArgumentString.toString().trim());
                            paramArgumentString = new StringBuilder("");
                        }
                        paramCommandString = part;
                    } else {
                        paramArgumentString.append(part + " ");
                    }
                }
                if (paramCommandString != null) {
                    params.putIfAbsent(paramCommandString, paramArgumentString.toString().trim());
                } else if (paramArgumentString.length() > 0) {
                    params.put("general", paramArgumentString.toString().trim());
                }

                if (com.get().getValue().execute(params)) {
                    log.debug("Your command line " + input + " succeeded");
                } else {
                    log.error(input + " >>> fails. Please try it again / another command or contact the developer!");
                }
            } else {
                log.warn("There is no such command " + input + ". Type \"help\" to see the list of aviable commands!");
            }
        }


        KBEvaluationHandler handler = new KBEvaluationHandler();

        List<Triple> model = SparqlHandler.getResources("http://dbpedia.org/ontology/Person");

        JENAtoCONSOLE.print(model, 15);
        System.out.println("getTriplesFromKB()");
        JENAtoCONSOLE.print(handler.getTriplesFromKB());
        handler.getCBDofResource(0.1);
        System.out.println("kbCBDList()");
        handler.getKbCBDList().forEach(m -> JENAtoCONSOLE.print(m, 3));
        handler.getCorrectLabels().forEach(System.out::println);
    }

    public static Optional<Map.Entry<String, Command>> Get(String key) {
        return Get(commands, key);
    }

    public static <K, V> Optional<Map.Entry<K, V>> Get(Map<K, V> map, String key) {
        return map.entrySet().stream().filter(entry -> entry.getKey().equals(key)).findFirst();
    }
}
