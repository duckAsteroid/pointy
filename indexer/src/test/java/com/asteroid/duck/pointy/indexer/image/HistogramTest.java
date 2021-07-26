package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistogramTest {

    private final BinnedColourSpace space = BinnedColourSpace.create(4);
    private final List<Pixel> pixels = Arrays.asList(
            new Pixel(0x000000), // 0
            new Pixel(0x7F7F7F), //
            new Pixel(0xFFFFFF));

    private Histogram test;


    @BeforeEach
    void setup() {
        test = new Histogram(space);
    }

    @Test
    void addPixel() {
        assertEquals(space.indexSize(), test.size());
        assertEquals(0, test.values().sum());

        test.addPixel(pixels.get(0));
        assertEquals(space.indexSize(), test.size());
        assertEquals(1, test.values().sum());

        test.addPixel(pixels.get(1));
        assertEquals(space.indexSize(), test.size());
        assertEquals(2, test.values().sum());

        test.addPixel(pixels.get(2));
        assertEquals(space.indexSize(), test.size());
        assertEquals(3, test.values().sum());

    }

    @Test
    void addAll() {
        Histogram other = new Histogram(space);
        pixels.forEach(other::addPixel);
        pixels.forEach(test::addPixel);

        test.addAll(other);
        assertEquals(space.indexSize(), test.size());
        assertEquals(6, test.values().sum());
    }

    @Test
    void result() {
        final List<Pixel> pixels = new ArrayList<>(this.pixels);
        pixels.add(new Pixel(0xFFFFFF)); // 2 FFF

        pixels.forEach(test::addPixel);
        List<Long> result = test.result();
        for (int i = 0; i < result.size(); i++) {
            Long aLong = result.get(i);
            if (aLong > 0)
                System.out.println(i+"="+aLong);
        }
        assertEquals(space.indexSize(), result.size());
        assertEquals(1, result.get(0));
        assertEquals(1, result.get(42));
        assertEquals(2, result.get(63));
    }

    @Test
    void normaliseMax() {
        final List<Pixel> pixels = new ArrayList<>(this.pixels);
        pixels.add(new Pixel(0xFFFFFF)); // 2 FFF

        pixels.forEach(test::addPixel);
        List<Long> result = test.result();
        List<Double> norm = Histogram.normaliseMax(result);
        assertEquals(0.5, norm.get(0), 0.0001);
        assertEquals(0.5, norm.get(42), 0.0001);
        assertEquals(1.0, norm.get(63), 0.0001);
    }

    @Test
    void normalisePixelCount() {
        final List<Pixel> pixels = new ArrayList<>(this.pixels);
        pixels.add(new Pixel(0xFFFFFF)); // 2 FFF

        pixels.forEach(test::addPixel);
        List<Long> result = test.result();
        List<Double> norm = Histogram.normalisePixelCount(result);
        assertEquals(0.25, norm.get(0), 0.0001);
        assertEquals(0.25, norm.get(42), 0.0001);
        assertEquals(0.5, norm.get(63), 0.0001);
    }

    @Test
    void euclideanDistance() {
        List<Long> d0 = Arrays.asList(0L,0L);
        List<Long> d10 = Arrays.asList(10L,10L);
        assertEquals(0, Histogram.distance(d0, d0));
        assertEquals(0, Histogram.distance(d10, d10));
        assertEquals(14.14213562, Histogram.distance(d0, d10), 0.001);
    }
}