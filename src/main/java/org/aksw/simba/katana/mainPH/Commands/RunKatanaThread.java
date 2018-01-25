package org.aksw.simba.katana.mainPH.Commands;

import org.aksw.simba.katana.algorithm.EvaluationHandler;

/**
 * A class, that runs a single time the KATANA algorithm
 *
 * @author Pilipp Heinisch
 */
public class RunKatanaThread implements Runnable {

    private EvaluationHandler handler = null;

    private int forgottenLabels;
    private double randomPropertyLabels;
    private boolean evaluate;

    public RunKatanaThread(int forgottenLabels, double randomPropertyLabels, boolean evaluate) {
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
        //first calculate the input fpr the KatanaAlgo

        //run it!

        notify();
    }

    public EvaluationHandler getHandler() {
        return handler;
    }
}
