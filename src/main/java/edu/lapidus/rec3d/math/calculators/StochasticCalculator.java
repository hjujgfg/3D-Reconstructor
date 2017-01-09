package edu.lapidus.rec3d.math.calculators;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * Created by egor.lapidus on 27/12/2016.
 */
public class StochasticCalculator {
    double average;
    double standardDeviation;
    Collection<Double> values;

    private final static Logger logger = Logger.getLogger(StochasticCalculator.class);

    public StochasticCalculator (Collection<Double> values) {
        if (values == null || values.isEmpty()) throw new IllegalArgumentException("Empty list");
        this.values = values;
        calcAverage();
        calcStandardDeviation();
    }

    private void calcAverage() {
        double summ = 0;
        for (double d : values) {
            summ += d;
        }
        average = summ / values.size();
        logger.info("Calculated average: " + average);
    }

    private void calcStandardDeviation() {
        double summ = 0;
        for (double d : values) {
            summ += (d - average) * (d - average);
        }
        standardDeviation = Math.sqrt(summ / values.size());
        logger.info("Calculated deviation: " + standardDeviation);
    }

    public double getAverage() {
        return average;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public Double getBoundary() {
        return 3 * standardDeviation;
    }
}
