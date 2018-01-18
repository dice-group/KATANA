package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.mainPH.Main;
import org.aksw.simba.katana.mainPH.View.JENAtoCONSOLE;
import org.apache.log4j.*;

import java.util.Map;

/**
 * see getHelp()
 *
 * @author Philipp Heinisch
 */
public class PrintDatabase implements Command {

    private static Logger log = LogManager.getLogger(PrintDatabase.class);

    public PrintDatabase() {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + PrintDatabase.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + PrintDatabase.class.getName() + "!");
    }

    /**
     * A short description of the command
     *
     * @return the output String
     */
    @Override
    public String getHelp() {
        return "Shows you the loaded database";
    }

    /**
     * A long description of the command including the description of the parameters
     *
     * @return the output string
     */
    @Override
    public String getLongHelp() {
        return getHelp() + System.lineSeparator() +
                "--limit <number>: Limits the number of output lines";
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
        int limit;
        if (Main.Get(params, "--limit").isPresent()) {
            try {
                limit = Integer.parseInt(Main.Get(params, "--limit").get().getValue());
            } catch (NumberFormatException e) {
                log.error("Error while parsing the double from the \"--limit\" param: " + Main.Get(params, "--limit").get(), e);
                return false;
            }
        } else {
            limit = 20;
        }

        log.trace("Will print subject triple list by subject triple list with an output limit for each triple list of " + limit);
        Main.database.forEach(sd -> JENAtoCONSOLE.print(sd, limit));

        return true;
    }
}
