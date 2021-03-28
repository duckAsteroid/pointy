package com.asteroid.duck.pointy.indexer.image;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Data
public class ColourUtils {

    public static int[] split(int pixel) {
        int[] result = new int[4];
        for(Channel ch : Channel.values()) {
            result[ch.index()] = ch.extract(pixel);
        }
        return result;
    }

    public static Stream<Integer> pixelValueStream(BufferedImage image) {
        PixelIterator iterator = new PixelIterator(image);
        // use the iterator to create a sized stream
        return StreamSupport.stream(
                Spliterators.spliterator(iterator, iterator.size(),
                        Spliterator.ORDERED | Spliterator.SIZED),true);
    }

    public static Stream<Pixel> pixelStream(BufferedImage image) {
        return pixelValueStream(image).map(Pixel::new);
    }

    public static List<Long> histogram(BufferedImage image, ColourSpace space) {
        return pixelStream(image).collect(new HistogramCollector(space));
    }
}
