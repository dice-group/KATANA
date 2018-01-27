package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.mainPH.Main;
import org.apache.log4j.*;

import java.io.*;
import java.util.*;

import static org.aksw.simba.katana.mainPH.Main.Get;

public class RunKatana implements Command {

    private Logger log = LogManager.getLogger(RunKatana.class);

    public RunKatana() {
        //Logger-setup
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + RunKatana.class.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + RunKatana.class.getName() + "!");
    }

    /**
     * A short description of the command
     *
     * @return the output String
     */
    @Override
    public String getHelp() {
        return "The core of this application. Run the [KATANA] algorithm and observe, how the the rematching-labeling-process works!";
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
                "Regarding the algorithm..." + System.lineSeparator() +
                "--version <number>: the version of the algorithm. Version 0 is a random match, version 1 the KATANA algorithm from the paper. No more algorithmn known until yet..." + System.lineSeparator() +
                "Regarding the repetitions..." + System.lineSeparator() +
                "--forgottenLabelsMin <number>, --forgottenLabelsMax <number>" + System.lineSeparator() +
                "--randomPropertyLabelsMin <double 0-1> --randomPropertyLabelsMax <double 0-1>" + System.lineSeparator() +
                "--runs <number>: the number of runs, that you wish (approximately). Notice: a high number here increase the granularity of the result, but increase the runtime, too" + System.lineSeparator() +
                "--reruns <number>: the number of runs for each run (for the same input parameters). A high number ensure a result next to the mathematical expectation (\"Stochastik\"), but increases the runtime!" + System.lineSeparator() +
                "Regarding the output..." + System.lineSeparator() +
                "--noEvaluation: disables the Evaluation. Saves runtime..." + System.lineSeparator() +
                "--file [<path>]: with this parameter the core result will be written to a csv file. If you determine a path, the file will be saved in that path" + System.lineSeparator() +
                "--open: (only in combination with the \"--file\" parameter) open the written file ofter the process. If you have Excel installed or a simular application, it will be open with that application. There you can create a graph.";
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

        //Params
        int algoVersion = 1;
        Optional<Map.Entry<String, String>> getter = Main.Get(params, "--version");
        if (getter.isPresent()) {
            try {
                algoVersion = Integer.parseInt(getter.get().getValue());
                log.debug("The algoVersion was set to " + algoVersion);
            } catch (NumberFormatException e) {
                log.warn("Can't parse the input " + getter.get(), e);
            }
        }
        int forgottenLabelsMin = 1;
        getter = Main.Get(params, "--forgottenLabelsMin");
        if (getter.isPresent()) {
            try {
                forgottenLabelsMin = Integer.parseInt(getter.get().getValue());
                if (forgottenLabelsMin < 0) {
                    log.warn(getter + ": Negative numbers are not allowed! Set to 0!");
                    forgottenLabelsMin = 0;
                }
            } catch (NumberFormatException e) {
                log.warn(getter + ": Can't parse the input " + getter.get(), e);
            }
        }

        int forgottenLabelsMax = Main.subjects.size();
        getter = Main.Get(params, "--forgottenLabelsMax");
        if (getter.isPresent()) {
            try {
                forgottenLabelsMax = Integer.parseInt(getter.get().getValue());
                if (forgottenLabelsMax < 0 || forgottenLabelsMax < forgottenLabelsMin) {
                    log.warn(getter + ": Negative numbers or numbers smalles than \"--forgottenLabelsMin\" are not allowed! Set to " + forgottenLabelsMin + "!");
                    forgottenLabelsMax = forgottenLabelsMin;
                }
            } catch (NumberFormatException e) {
                log.warn(getter + ": Can't parse the input " + getter.get(), e);
            }
        }

        double randomPropertyLabelsMin = 0.1;
        getter = Main.Get(params, "--randomPropertyLabelsMin");
        if (getter.isPresent()) {
            try {
                randomPropertyLabelsMin = Double.parseDouble(getter.get().getValue());
                if (randomPropertyLabelsMin < 0) {
                    log.warn(getter + ": Negative numbers are not allowed! Set to 0!");
                    randomPropertyLabelsMin = 0;
                } else if (randomPropertyLabelsMin > 1) {
                    log.warn(getter + ": Because it's a rate, numbers greater than 1 are not allowed! Set it to 1!");
                    randomPropertyLabelsMin = 1;
                }
            } catch (NumberFormatException e) {
                log.warn("Can't parse the input " + getter.get(), e);
            }
        }

        double randomPropertyLabelsMax = 1;
        getter = Main.Get(params, "--randomPropertyLabelsMax");
        if (getter.isPresent()) {
            try {
                forgottenLabelsMax = Integer.parseInt(getter.get().getValue());
                if (forgottenLabelsMax < 0 || forgottenLabelsMax < forgottenLabelsMin) {
                    log.warn(getter + ": Negative numbers or numbers smalles then \"--randomPropertyLabelsMin\" are not allowed! Set to " + randomPropertyLabelsMin + "!");
                    randomPropertyLabelsMax = forgottenLabelsMin;
                } else if (randomPropertyLabelsMax > 1) {
                    log.warn(getter + ": Because it's a rate, numbers greater than 1 are not allowed! Set it to 1!");
                    randomPropertyLabelsMax = 1;
                }
            } catch (NumberFormatException e) {
                log.warn(getter + ": Can't parse the input " + getter.get(), e);
            }
        }

        int runs = 5;
        getter = Main.Get(params, "--runs");
        if (getter.isPresent()) {
            try {
                runs = Integer.parseInt(getter.get().getValue());
                if (runs <= 0) {
                    log.warn(getter + ": Negative numbers are not allowed! Set to 1!");
                    runs = 1;
                }
            } catch (NumberFormatException e) {
                log.warn(getter + ": Can't parse the input " + getter.get(), e);
            }
        }
        int runsPerAxis = (int) Math.sqrt(runs);
        double stepWidthForgottenLabels = ((double) (forgottenLabelsMax - forgottenLabelsMin)) / ((double) runsPerAxis - 1);
        double stepWidthRandomPropertyLabels = (randomPropertyLabelsMax - randomPropertyLabelsMin) / ((double) runsPerAxis - 1);

        int reruns = 1;
        getter = Main.Get(params, "--reruns");
        if (getter.isPresent()) {
            try {
                reruns = Integer.parseInt(getter.get().getValue());
                if (runs <= 0) {
                    log.warn(getter + ": Negative numbers are not allowed! Set to 10! (" + getter.get() + ")");
                    reruns = 10;
                }
            } catch (NumberFormatException e) {
                log.warn(getter + ": Can't parse the input " + getter.get(), e);
            }
        }

        boolean evaluation = !Main.Get(params, "--noEvaluation").isPresent();

        boolean open = evaluation && Main.Get(params, "--open").isPresent();

        getter = Main.Get(params, "--file");
        boolean writeCSV = getter.isPresent();
        String pathCSV = System.getProperty("user.dir");
        if (writeCSV) {
            if (!evaluation) {
                log.warn("You disabled the Evaluation, so the CSV file will be (nearly) empty!");
            }
            if (getter.get().getValue() != "true") {
                pathCSV = new File(getter.get().getValue()).getAbsolutePath();
            }
            log.info("The CSV result file will be saved in the directory " + pathCSV);
        }

        //RUN!!!!
        log.debug("There are " + RunKatanaReturn.CountOfCurrentEntries() + " entries in the " + RunKatanaReturn.class.getName() + " store - clear it!");
        RunKatanaReturn.clear();

        int expectedSizeOfResultMap = 0;
        double forgottenLabelsRunOld = -1, forgottenLabelsRun = forgottenLabelsMin, randomPropertyLabelsRunOld = -1, randomPropertyLabelsRun = randomPropertyLabelsMin;
        for (int i = 0; i < runsPerAxis; i++) {
            for (int j = 0; j < runsPerAxis; j++) {
                if (Math.round(forgottenLabelsRun) > Math.round(forgottenLabelsRunOld) && randomPropertyLabelsRun > randomPropertyLabelsRunOld) {
                    for (int k = 0; k < reruns; k++) {
                        RunKatanaThread thread = new RunKatanaThread(algoVersion, (int) Math.round(forgottenLabelsRun), randomPropertyLabelsRun, evaluation);
                        log.debug("Start thread at " + i + "|" + j + " (" + k + ". run [see \"--reruns\"])");
                        thread.run();
                    }
                    expectedSizeOfResultMap++;
                }
                randomPropertyLabelsRunOld = randomPropertyLabelsRun;
                randomPropertyLabelsRun += stepWidthRandomPropertyLabels;
            }
            randomPropertyLabelsRunOld = -1;
            randomPropertyLabelsRun = randomPropertyLabelsMin;
            forgottenLabelsRunOld = forgottenLabelsRun;
            forgottenLabelsRun += stepWidthForgottenLabels;
        }
        log.trace("actuallyRuns: " + expectedSizeOfResultMap);

        //Evaluation
        String fileNameCSV = pathCSV + System.getProperty("file.separator") + "katanaResult_" + new Date().getTime() + ".csv";
        FileWriter fileWriterTemp;
        try {
            fileWriterTemp = new FileWriter(fileNameCSV, true);
            fileWriterTemp.write("#forgotten labels; %random property labels deleted; reconstruction rate" + System.lineSeparator());
        } catch (IOException e) {
            fileWriterTemp = null;
            log.error("Can't write the csv file :(", e);
            if (evaluation)
                return false;
        }
        final FileWriter fileWriter = fileWriterTemp;

        int lookupTimes = 0;
        while (RunKatanaReturn.CountOfCurrentEntries() < expectedSizeOfResultMap) {
            try {
                Thread.sleep(100);
                lookupTimes++;
                if (lookupTimes % 10 == 0) {
                    log.info(RunKatanaReturn.CountOfCurrentEntries() + " out of " + expectedSizeOfResultMap + " are already finished...");
                }
            } catch (InterruptedException e) {
                log.trace(e.getMessage(), e);
            }
        }

        RunKatanaReturn.getKeys().forEach(k -> {
            if (evaluation) {
                if (fileWriter != null) {
                    try {
                        fileWriter.write(k.getKey() + ";" + k.getValue().toString().replace('.', ',') + ";" + Double.valueOf(RunKatanaReturn.getAccuracy(k)).toString().replace('.', ',') + System.lineSeparator());
                        log.trace("Result successfully appended to " + fileNameCSV);
                    } catch (IOException e) {
                        log.warn("Can't append the result (" + RunKatanaReturn.getAccuracy(k) + ") for the key " + k + "to the file " + fileNameCSV, e);
                    }
                }
            }
        });
        //FINISH
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                log.warn("Cannot close the FileWrite " + fileWriter, e);
            }
        }
        if (open) {
            try {
                log.trace("Open " + fileNameCSV + "...");
                Process p = Runtime.getRuntime().exec(new String[]{"explorer.exe", "\"" + fileNameCSV + "\""});
            } catch (IOException e) {
                log.info("Can't open the file/ process " + fileNameCSV + ". Do it manually!", e);
                return false;
            }
        } else if (evaluation) {
            log.info("You can find the csv in " + fileNameCSV + " now");
        }

        return true;
    }
}
