package com.asteroid.duck.pointy.indexer.image;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Used to collect a histogram from a stream of pixels
 */
public class HistogramCollector implements Collector<Pixel, Histogram, List<Long>> {
    /**
     * The colour space used to calculate the histogram
     */
    private final ColourSpace space;

    public HistogramCollector(ColourSpace space) {
        this.space = space;
    }

    @Override
    public Supplier<Histogram> supplier() {
        return () -> new Histogram(space);
    }

    @Override
    public BiConsumer<Histogram, Pixel> accumulator() {
        return Histogram::addPixel;
    }

    @Override
    public BinaryOperator<Histogram> combiner() {
        return Histogram::addAll;
    }

    @Override
    public Function<Histogram, List<Long>> finisher() {
        return Histogram::result;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.CONCURRENT);
    }
}
