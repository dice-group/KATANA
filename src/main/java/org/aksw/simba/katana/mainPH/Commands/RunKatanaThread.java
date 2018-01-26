package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.algorithm.EvaluationHandler;
import org.aksw.simba.katana.algorithm.KatanaAlgo;
import org.aksw.simba.katana.mainPH.Main;
import org.aksw.simba.katana.mainPH.TripleHelper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class, that runs a single time the KATANA algorithm
 *
 * @author Pilipp Heinisch
 */
public class RunKatanaThread implements Runnable {

    private final Logger log;
    private volatile static int ID = 0;

    private volatile EvaluationHandler handler = null;

    private final int version;
    final int forgottenLabels;
    final double randomPropertyLabels;
    private final boolean evaluate;

    public RunKatanaThread(int version, int forgottenLabels, double randomPropertyLabels, boolean evaluate) {
        //Logger-setup
        log = LogManager.getLogger(Thread.currentThread().getName() + "[" + ID + "]");
        ID++;
        Layout l = new SimpleLayout();
        Appender appender = new ConsoleAppender(l, ConsoleAppender.SYSTEM_OUT);
        appender.setName("Console - " + log.getName());
        log.addAppender(appender);
        log.trace("Logging is enabled (" + log.getAllAppenders().hasMoreElements() + ") for the class " + RunKatana.class.getName() + "!");

        this.version = version;
        this.forgottenLabels = forgottenLabels;
        this.randomPropertyLabels = randomPropertyLabels;
        this.evaluate = evaluate;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        log.trace("Thread is started // " + log.getName());

        //first calculate the input for the KatanaAlgo
        final List<Node> actualSubjects;
        if (forgottenLabels <= 0) {
            actualSubjects = Collections.EMPTY_LIST;
        } else {
            List<Node> actualSubjectsTemp = new ArrayList<>(Main.subjects.size());
            Collections.copy(actualSubjectsTemp, Main.subjects);
            Collections.shuffle(actualSubjectsTemp);
            actualSubjects = actualSubjectsTemp.subList(0, Math.min(forgottenLabels, actualSubjectsTemp.size()));
        }
        log.debug("Candidates for the KATANArun were calculated. Because of the parameter \"forgottenLabels\" = " + forgottenLabels + ", the size of this list is " + actualSubjects.size());

        List<List<Triple>> actualKnowledgeLabelExtraction = new ArrayList<>(Main.database.size());
        Main.database.stream().forEach(tripleList -> {
            if (actualSubjects.stream().anyMatch(s -> tripleList.stream().anyMatch(t -> t.getSubject().equals(s)))) {
                log.trace("Found a matching triple list to a candidate. Delete triples from that with the probability of " + randomPropertyLabels);
                actualKnowledgeLabelExtraction.add(TripleHelper.deleteByRandom(tripleList, randomPropertyLabels));
            }
        });
        StringBuilder out = new StringBuilder("actualKnowledgeLabelExtraction for the KATANArun was calculated. Contains " + actualKnowledgeLabelExtraction.size() + " triple lists with max. ");
        actualKnowledgeLabelExtraction.stream().max(Comparator.comparingInt(List::size)).ifPresent(l -> out.append(l.size()));
        out.append("XXX");
        log.debug(out);

        List<Triple> actualDatabase = Main.database.stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        log.debug("Created the actualDatabase from the Main.database: " + actualDatabase.size() + " elements");

        //run it!
        KatanaAlgo katanaAlgo = new KatanaAlgo(actualDatabase, actualSubjects, actualKnowledgeLabelExtraction);
        log.trace("Inputs for the KATANA algorithmn are valid? [" + katanaAlgo.verify() + "]");
        List<Triple> labelGuesses;
        switch (version) {
            case 0:
                labelGuesses = katanaAlgo.matchLabelsRANDOM();
                break;
            case 1:
                labelGuesses = katanaAlgo.matchLabelsKATANAv1();
                break;
            default:
                log.error("No algorithmn matches to the version input " + version + "! CANCEL!");
                labelGuesses = Collections.EMPTY_LIST;
                break;
        }

        if (evaluate) {
            katanaAlgo.getEvaluationHandler(labelGuesses);
        }

        log.info(log.getName() + "\t Finished! The algorithm matched " + labelGuesses.size() + " labels!");

        notify();
    }

    public EvaluationHandler getHandler() {
        return handler;
    }
}
