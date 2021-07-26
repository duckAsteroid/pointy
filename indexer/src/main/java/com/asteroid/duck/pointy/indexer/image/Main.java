package com.asteroid.duck.pointy.indexer.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            BinnedColourSpace colourSpace = BinnedColourSpace.D64;
            if ("HIST".equalsIgnoreCase(args[0])) {
                for (int i = 1; i < args.length; i++) {
                    System.out.println("Image:"+args[i]);
                    List<Long> h = histogram(args[i], colourSpace );
                    print(h, colourSpace).forEach(System.out::println);
                }

            } else if ("DIST".equalsIgnoreCase(args[0])) {
                List<Double> first = Histogram.normaliseMax(histogram(args[1], colourSpace));
                List<Double> second = Histogram.normaliseMax(histogram(args[2], colourSpace));
                System.out.println("Distance "+Histogram.distance(first,second));
                return;
            }
        }


        System.err.println("Expected operation [HIST, DIST]");

    }

    public static List<Long> histogram(String fileName, ColourSpace colorSpace) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));
        int bins = 4;
        BinnedColourSpace space = BinnedColourSpace.create(bins);
        return ColourUtils.histogram(image, space);
    }

    public static List<String> print(List<Long> histogram, BinnedColourSpace space) {
        return IntStream.range(0, histogram.size())
                .mapToObj(i ->  i + ",0x" + Integer.toHexString(space.midPointColour(i)).toUpperCase() + "," + histogram.get(i))
                .collect(Collectors.toList());
    }
}
