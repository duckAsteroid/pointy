package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class ColourSpaceTest {

    private final ColourSpace subject2 = new ColourSpace.BinnedColourSpace(3);
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
        assertEquals(8, new ColourSpace.BinnedColourSpace(2).indexSize());
        assertEquals(27, new ColourSpace.BinnedColourSpace(3).indexSize());
        assertEquals(1, new ColourSpace.BinnedColourSpace(1).indexSize());
    }

    @Test
    void coordinates() {
        assertArrayEquals(new int[]{2,2,2}, ColourSpace.BinnedColourSpace.D27.coordinates(26));
        assertArrayEquals(new int[]{1,1,1}, ColourSpace.BinnedColourSpace.D27.coordinates(13));
        assertArrayEquals(new int[]{0,0,2}, ColourSpace.BinnedColourSpace.D27.coordinates(18));
        assertArrayEquals(new int[]{0,0,0}, ColourSpace.BinnedColourSpace.D27.coordinates(0));
    }

    @Test
    void unity() {
        assertEquals(12345, ColourSpace.UNITY.index(new Pixel(12345)));
    }
}