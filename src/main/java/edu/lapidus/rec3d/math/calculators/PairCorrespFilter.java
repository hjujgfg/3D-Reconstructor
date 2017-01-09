package edu.lapidus.rec3d.math.calculators;

import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by egor.lapidus on 27/12/2016.
 */
public final class PairCorrespFilter {
    private Set<PairCorrespData> result;
    private double xDeviation, yDeviation, zDeviation, xAverage, yAverage, zAverage;

    private final static Logger logger = Logger.getLogger(PairCorrespFilter.class);

    public PairCorrespFilter(Collection<PairCorrespData> points) {
        calcParams(points);
        logger.info("Initial point number: " + points.size());
        result.addAll(points.stream().filter(pairCorrespData -> Math.abs(pairCorrespData.getX()) < xAverage + xDeviation).collect(Collectors.toCollection(HashSet::new)));
        logger.info("After X filtering points number: " + result.size());
        result = result.stream().filter(pairCorrespData -> Math.abs(pairCorrespData.getY()) < yAverage + yDeviation).collect(Collectors.toCollection(HashSet::new));
        logger.info("After Y filtering points number: " + result.size());
        result = result.stream().filter(pairCorrespData -> Math.abs(pairCorrespData.getZ()) < zAverage + zDeviation).collect(Collectors.toCollection(HashSet::new));
        logger.info("After Y filtering points number: " + result.size());
    }

    private void calcParams(Collection<PairCorrespData> points) {
        result = new HashSet<>();
        StochasticCalculator calculator = new StochasticCalculator(points.stream().map(
                pairCorrespData -> pairCorrespData.getX())
                .collect(Collectors.toCollection(ArrayList::new)));
        xDeviation = calculator.getBoundary();
        xAverage = calculator.getAverage();
        result = new HashSet<>();
        calculator = new StochasticCalculator(points.stream().map(
                pairCorrespData -> pairCorrespData.getY())
                .collect(Collectors.toCollection(ArrayList::new)));
        yDeviation = calculator.getBoundary();
        yAverage = calculator.getAverage();
        result = new HashSet<>();
        calculator = new StochasticCalculator(points.stream().map(
                pairCorrespData -> pairCorrespData.getZ())
                .collect(Collectors.toCollection(ArrayList::new)));
        zDeviation = calculator.getBoundary();
        zAverage = calculator.getAverage();
    }

    public Set<PairCorrespData> getResults () {
        return result;
    }
}
