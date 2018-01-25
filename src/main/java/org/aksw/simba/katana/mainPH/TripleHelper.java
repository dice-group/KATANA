package org.aksw.simba.katana.mainPH;

import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Just a HelperClass
 *
 * @author Philipp Heinisch
 */
public abstract class TripleHelper {
    private static Random random = new Random();

    /**
     * A random deletion list operation
     *
     * @param set               a list of e.g. triples
     * @param deleteProbability the probability for each list element to be deleted. Should be between 0 and 1.
     * @return a (maybe) shorter list of e.g. triples. If the List contains a Label-Triple, the return list will have the label-Triple, too
     */
    public static <E> List<E> deleteByRandom(List<E> set, double deleteProbability) {
        List<E> ret = new ArrayList<>(set.size());

        for (E t : set) {
            if (t instanceof Triple && ((Triple) t).getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                ret.add(t);
            } else if (random.nextDouble() > deleteProbability) {
                ret.add(t);
            }
        }

        return ret;
    }
}
