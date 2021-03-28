package com.asteroid.duck.pointy.indexer.image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Histogram {
    private final ColourSpace space;
    private final ArrayList<AtomicLong> counts;

    public Histogram(ColourSpace space) {
        this.space = space;
        // FIXME Init counts based on colour space
        this.counts = new ArrayList<>(space.indexSize());
        for (int i = 0; i < space.indexSize(); i++) {
            counts.add(i, new AtomicLong(0));
        }
    }

    public void addPixel(Pixel pixel) {
        int index = space.index(pixel);
        AtomicLong count = counts.get(index);
        count.incrementAndGet();
    }

    public Histogram addAll(Histogram other) {
        if (!other.space.equals(this.space)) {
            throw new IllegalArgumentException("Colour space must be the same");
        }
        for (int i = 0; i < other.counts.size(); i++) {
            AtomicLong current = counts.get(i);
            current.addAndGet(other.counts.get(i).get());
        }
        return this;
    }

    public int size() {
        return counts.size();
    }

    public LongStream values() {
        return counts.stream().mapToLong(AtomicLong::get);
    }

    public List<Long> result() {
        return counts.stream().map(AtomicLong::get).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Take a result vector and normalise it - so that most frequent bin is 1.0
     * @param histogram the histogram result vector
     * @return the normalised vector (0.0 - 1.0)
     */
    public static List<Double> normalise(List<Long> histogram) {
        double max = histogram.stream().mapToLong(Long::longValue).max().orElse(0);
        return histogram.stream().map(value -> value / max).collect(Collectors.toList());
    }

    /**
     * Used in {@link #distance(List, List)}
     */
    private final static class Pair {
        // just two longs in a structure
        public final Number p, q;

        private Pair(Number p, Number q) {
            this.p = p;
            this.q = q;
        }
    }

    /**
     * Calculate the Euclidean distance between two histogram result vectors
     * @param p the first histogram result vector
     * @param q the seconds
     * @return the distance between them
     */
    public static double distance(final List<? extends Number> p, final List<? extends Number> q) {
        if (p.size() != q.size()) throw new IllegalArgumentException("Histograms must be same size");
        // a stream of the indexes in both lists
        IntStream indexStream = IntStream.range(0, p.size());
        // walk that stream
        double sum = indexStream
                // create a pair from each list
                .mapToObj(i -> new Pair(p.get(i), q.get(i)))
                // difference
                .mapToDouble(pair -> (pair.q.doubleValue() - pair.p.doubleValue()))
                // square
                .map(d -> d * d)
                // sum of squares
                .sum();
        // get square root of sum of squares
        return Math.sqrt(sum);
    }
}
