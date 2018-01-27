package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.mainPH.Main;
import org.aksw.simba.katana.mainPH.TripleHelper;
import org.apache.jena.graph.Triple;
import org.apache.log4j.*;

import java.util.List;
import java.util.Map;

/**
 * see help
 *
 * @author Philipp Heinisch
 */
public class EditDatabase implements Command {

    private Logger log = LogManager.getLogger(EditDatabase.class);

    public EditDatabase() {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + EditDatabase.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + EditDatabase.class.getName() + "!");
    }

    /**
     * A short description of the command
     *
     * @return the output String
     */
    @Override
    public String getHelp() {
        return "Edit the current loaded database! You can use that e.g. before you execute the KATANA algorithm to have another database base";
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
                "--random <double: 0-1>: deletes with the given random triples from the database" + System.lineSeparator() +
                "--cutHead <number>: cuts the first main subjects with all their triples" + System.lineSeparator() +
                "--cutTail <number>: cuts the last main subjects with all their triples";
    }

    /**
     * Executes the commands
     *
     * @param params the given params
     * @return {@code true}, if the execution succeeded, otherwise {@code false}
     */
    @Override
    public boolean execute(Map<String, String> params) {
        if (Main.database == null || Main.subjects == null) {
            log.warn("Database is empty. Please load first a database!");
            return false;
        }

        int changeOperationsSucceeded = 0;

        if (Main.Get(params, "--random").isPresent()) {
            double probability;
            try {
                probability = Double.parseDouble(Main.Get(params, "--random").get().getValue());
            } catch (NumberFormatException e) {
                log.error("Error while parsing the double from the \"--random\" param: " + Main.Get(params, "--random").get(), e);
                return false;
            }
            for (int i = 0; i < Main.database.size(); i++) {
                List<Triple> triple = Main.database.get(i);
                Main.database.set(i, TripleHelper.deleteByRandom(triple, probability));
                log.debug("Sublist is shorter about " + (triple.size() - Main.database.get(i).size()) + " elements now!");
                changeOperationsSucceeded++;
            }
        }

        if (Main.Get(params, "--cutHead").isPresent()) {
            int number;
            try {
                number = Integer.parseInt(Main.Get(params, "--cutHead").get().getValue());
            } catch (NumberFormatException e) {
                log.error("Error while parsing the double from the \"--cutHead\" param: " + Main.Get(params, "--cutHead").get(), e);
                return false;
            }
            try {
                Main.database = Main.database.subList(number, Main.database.size());
                Main.subjects = Main.subjects.subList(number, Main.subjects.size());
            } catch (IndexOutOfBoundsException e) {
                log.warn("The list contains only " + Main.database.size() + " list elements, not " + number + "! Remove the complete List...", e);
                Main.database.clear();
                Main.subjects.clear();
            } finally {
                changeOperationsSucceeded++;
            }
        }

        if (Main.Get(params, "--cutTail").isPresent()) {
            int number;
            try {
                number = Integer.parseInt(Main.Get(params, "--cutTail").get().getValue());
            } catch (NumberFormatException e) {
                log.error("Error while parsing the double from the \"--cutTail\" param: " + Main.Get(params, "--cutTail").get(), e);
                return false;
            }
            try {
                Main.database = Main.database.subList(0, Main.database.size() - number);
                Main.subjects = Main.subjects.subList(0, Main.subjects.size() - number);
            } catch (IndexOutOfBoundsException e) {
                log.warn("The list contains only " + Main.database.size() + " list elements, not " + number + "! Remove the complete List...", e);
                Main.database.clear();
                Main.subjects.clear();
            } finally {
                changeOperationsSucceeded++;
            }
        }

        if (changeOperationsSucceeded == 0) {
            log.warn("No operations were executed :(. Maybe you missed to type in a parameter like \"--random\", \"--cutHead\" or \"--cutTail\"");
        } else {
            log.debug(changeOperationsSucceeded + " change operations succeeded!");
        }
        return true;
    }
}
