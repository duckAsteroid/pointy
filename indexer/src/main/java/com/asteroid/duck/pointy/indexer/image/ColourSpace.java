package com.asteroid.duck.pointy.indexer.image;

import java.util.Objects;

/**
 * How to convert RGB integer pixel values into a simplified 3 dimensional space - N bins per dimension
 */
public class ColourSpace {
    /**
     * A sixty four bin colour space
     */
    public static final ColourSpace D64 = new ColourSpace(4);

    private final int stepCount;
    private final double stepSize;

    public ColourSpace(int bins) {
        this.stepCount = bins;
        this.stepSize = (256.0 / stepCount) - 1;
    }

    /**
     * Given a pixel determine which "cube" of the colourspace it fits in. Alpha is ignored
     * @param pixel the pixel (with an aRGB colour)
     * @return the cube index
     */
    public int index(Pixel pixel) {
        final int max = stepCount - 1;
        int r = Math.min(max, (int) (pixel.extract(Channel.red) / stepSize));
        int g = Math.min(max, (int) (pixel.extract(Channel.green) / stepSize));
        int b = Math.min(max, (int) (pixel.extract(Channel.blue) / stepSize));
        return r + (g * stepCount) + (b * stepCount * stepCount);
    }

    public int stepCount() {
        return stepCount;
    }

    public int indexSize() { return stepCount * stepCount * stepCount; }

    /**
     * Create an aRGB colour value representing the mid point of the indexed cube
     * @param index the colour space cube index
     * @return the aRGB colour
     */
    public int midPointColour(int index) {
        int value = (int)((index * stepSize) + (stepSize / 2));
        return Channel.red.project(value) + Channel.green.project(value) + Channel.blue.project(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColourSpace that = (ColourSpace) o;
        return stepCount == that.stepCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepCount);
    }

    @Override
    public String toString() {
        return "ColourSpace{" +
                "stepCount=" + stepCount +
                ", stepSize=" + stepSize +
                '}';
    }
}
