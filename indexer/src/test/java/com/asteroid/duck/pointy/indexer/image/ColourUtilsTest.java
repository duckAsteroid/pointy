package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ColourUtilsTest {

    private static final BinnedColourSpace twoBins =  BinnedColourSpace.create(2);
    private static final BinnedColourSpace fourBins =  BinnedColourSpace.create(4);


    @Test
    void testPixelStream() {
        Stream<Pixel> pixelStream = ColourUtils.pixelStream(TestData.basn2c16png);
        Optional<Pixel> first = pixelStream.findFirst();
        assertTrue(first.isPresent());
        Pixel firstPixel = first.get();

    }

    @Test
    void testHistogram() {
        List<Long> histogram = ColourUtils.histogram(TestData.basn2c16png, twoBins);
        assertEquals(twoBins.indexSize(), histogram.size());
        for (int i = 0; i < twoBins.indexSize(); i++) {
            System.out.println("["+i+"]:0x"+Integer.toHexString(twoBins.midPointColour(i)).toUpperCase()+"="+histogram.get(i));
        }

        histogram = ColourUtils.histogram(TestData.basn2c16png, fourBins);
        assertEquals(fourBins.indexSize(), histogram.size());
        for (int i = 0; i < fourBins.indexSize(); i++) {
            System.out.println("["+i+"]:0x"+Integer.toHexString(fourBins.midPointColour(i)).toUpperCase()+"="+histogram.get(i));
        }
    }
}