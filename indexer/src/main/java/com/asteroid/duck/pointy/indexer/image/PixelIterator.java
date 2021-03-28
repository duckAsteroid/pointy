package com.asteroid.duck.pointy.indexer.image;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * An iterator over all the pixels in an image by X then by Y
 */
public class PixelIterator implements Iterator<Integer> {
    private final BufferedImage image;
    private int x = 0;
    private int y = 0;

    public PixelIterator(BufferedImage image) {
        this.image = image;
    }

    @Override
    public boolean hasNext() {
         return y < image.getHeight() &&  x < image.getWidth();
    }

    @Override
    public Integer next() {
        if (!hasNext()) throw new NoSuchElementException();
        int pixel = image.getRGB(x, y);
        x += 1;
        if (x >= image.getWidth()) {
            x = 0;
            y += 1;
        }
        return pixel;
    }

    public long size() {
        return (long)image.getWidth() * (long)image.getHeight();
    }

    @Override
    public String toString() {
        return "PixelIterator{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
