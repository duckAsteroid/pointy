package com.asteroid.duck.pointy.indexer.image;

/**
 * The 4 channels in an 32bit aRGB pixel
 */
public enum Channel {
    alpha(3), red(2), green(1), blue(0);

    private final int shift;
    private final int index;

    Channel(int i) {
        this.index = i;
        this.shift = i * 8;
    }

    public int extract(int pixel) {
        return (pixel >> shift) & 0xFF;
    }

    public int project(int value) {
        return (value & 0xFF) << shift;
    }

    public int index() {
        return index;
    }
}
