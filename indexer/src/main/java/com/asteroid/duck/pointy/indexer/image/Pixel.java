package com.asteroid.duck.pointy.indexer.image;

/**
 * A single RGB colour pixel value
 */
public class Pixel {
    private final int value;

    public Pixel(int value) {
        this.value = value;
    }

    public int extract(Channel ch) {
        return ch.extract(value);
    }
}
