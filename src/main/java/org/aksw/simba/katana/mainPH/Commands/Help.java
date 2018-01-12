package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.mainPH.Main;
import org.apache.log4j.*;

import java.util.Map;
import java.util.Optional;

public class Help implements Command {

    private static Logger log = LogManager.getLogger(Help.class);

    public Help() {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + Help.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + Help.class.getName() + "!");
    }

    @Override
    public String getHelp() {
        return "Helps you :).";
    }

    @Override
    public String getLongHelp() {
        return getHelp() + System.lineSeparator() +
                "If you set no arguments, a list of all commands will be returned. To get a specific help, please type the name of the command!";
    }

    @Override
    public boolean execute(Map<String, String> params) {
        if (params.isEmpty()) {
            Main.commands.entrySet().forEach(entry -> log.info(entry.getKey() + "\t" + entry.getValue().getHelp()));
        } else {
            Optional<Map.Entry<String, String>> com = Main.Get(params, "general");
            if (com.isPresent()) {
                String helpCommand = com.get().getValue();
                if (Main.Get(helpCommand).isPresent()) {
                    log.info(Main.Get(helpCommand).get().getValue().getLongHelp());
                } else {
                    log.warn("The command " + com + " doesn't exist!");
                }
            } else {
                log.warn("Please don't write any \"--\" in the help command, just the command name!");
            }
        }
        if (params.size() > 1) {
            log.debug((params.size() - 1) + " found. Discard them!");
        }

        return true;
    }
}
