package com.asteroid.duck.pointy.indexer.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestData {
    public static final List<Integer> LEVELS = Arrays.asList(0x00, 0x7F, 0xFF);

    public static final List<Integer> TEST_DATA = Arrays.asList(
            0x000000, 0x7F0000, 0xFF0000, // R 0 1 2, others 0
            0x007F00, 0x7F7F00, 0xFF7F00, // G 1 1 1, R 0 1 2
            0x00FF00, 0x7FFF7F, 0xFFFFFF); // R 0 1 2 , G 2 2 2, B 0 1 2


    public static final BufferedImage basn2c16png;

    static {
        try {
            basn2c16png = ImageIO.read(ColourUtilsTest.class.getResourceAsStream("/images/basn2c16.png"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load resource", e);
        }
    }
}
