package org.aksw.simba.katana.mainPH.Commands;

import edu.stanford.nlp.util.ArrayMap;
import org.aksw.simba.katana.algorithm.EvaluationHandler;

import java.util.*;

public abstract class RunKatanaReturn {
    private static Map<AbstractMap.SimpleEntry<Integer, Double>, List<EvaluationHandler>> results = Collections.synchronizedMap(new HashMap<>());

    public static void put(Integer forgottenLabels, Double randomPropertyLabels, EvaluationHandler handler) {
        AbstractMap.SimpleEntry<Integer, Double> key = new AbstractMap.SimpleEntry<>(forgottenLabels, randomPropertyLabels);
        if (results.containsKey(key)) {
            List<EvaluationHandler> value = results.get(key);
            value.add(handler);
            results.replace(key, value);
        } else {
            results.put(key, new ArrayList<>(Collections.singletonList(handler)));
        }
    }

    public static int CountOfCurrentEntries() {
        return results.size();
    }

    public static double getAccuracy(AbstractMap.SimpleEntry<Integer, Double> key) {
        if (results.containsKey(key)) {
            double result = 0;
            for (EvaluationHandler handler : results.get(key)) {
                result += (handler == null) ? 0 : handler.calculateAccuracy();
            }
            return result / results.get(key).size();
        }

        return 0;
    }

    public static Set<AbstractMap.SimpleEntry<Integer, Double>> getKeys() {
        return results.keySet();
    }

    public static void clear() {
        results.clear();
    }
}
