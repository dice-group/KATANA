package org.aksw.simba.katana.mainPH.Commands;

import java.util.Map;

public class Exit implements Command {
    @Override
    public String getHelp() {
        return "Exit the program...";
    }

    @Override
    public String getLongHelp() {
        return getHelp();
    }

    @Override
    public boolean execute(Map<String, String> params) {
        System.exit(1);
        return true;
    }
}
