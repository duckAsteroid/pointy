package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class ColourSpaceTest {

    private final ColourSpace subject2 = BinnedColourSpace.create(3);
    private List<Integer> expected = Arrays.asList(0,
            1,
            2,
            3,
            4,
            5,
            6,
            16,
            26);

    @Test
    void index() {
        List<Integer> result = TestData.TEST_DATA.stream().map(Pixel::new).map(subject2::index).collect(Collectors.toList());
        assertIterableEquals(expected, result);
    }

    @Test
    void indexSize() {
        assertEquals(8,  BinnedColourSpace.create(2).indexSize());
        assertEquals(27,  BinnedColourSpace.create(3).indexSize());
        assertEquals(1,  BinnedColourSpace.create(1).indexSize());
    }

    @Test
    void coordinates() {
        assertArrayEquals(new int[]{2,2,2}, BinnedColourSpace.D27.coordinates(26));
        assertArrayEquals(new int[]{1,1,1}, BinnedColourSpace.D27.coordinates(13));
        assertArrayEquals(new int[]{0,0,2}, BinnedColourSpace.D27.coordinates(18));
        assertArrayEquals(new int[]{0,0,0}, BinnedColourSpace.D27.coordinates(0));
    }

    @Test
    void rgbUnity() {
        assertEquals(12345, ColourSpace.RGB.index(new Pixel(12345)));
    }
}