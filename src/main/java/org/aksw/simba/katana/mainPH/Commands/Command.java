package org.aksw.simba.katana.mainPH.Commands;

import java.util.Map;

/**
 * Command pattern
 *
 * @author Philipp Heinisch
 */
public interface Command {

    /**
     * A short description of the command
     *
     * @return the output String
     */
    public String getHelp();

    /**
     * A long description of the command including the description of the parameters
     *
     * @return the output string
     */
    public String getLongHelp();

    /**
     * Executes the commands
     *
     * @param params the given params
     * @return {@code true}, if the execution succeeded, otherwise {@code false}
     */
    public boolean execute(Map<String, String> params);
}
