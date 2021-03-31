package com.asteroid.duck.pointy.indexer.image;

import java.util.Objects;

/**
 * How to convert RGB integer pixel values into a simplified 3 dimensional space - N bins per dimension
 */
public abstract class ColourSpace {

    public static final ColourSpace UNITY = new ColourSpace() {
        @Override
        public int index(Pixel pixel) {
            return pixel.getValue() & 0x00FFFFFF;
        }

        @Override
        public int indexSize() {
            return 0x00FFFFFF + 1;
        }

        @Override
        public int[] coordinates(int index, int count) {
            return new int[] {
                    Channel.red.extract(index),
                    Channel.green.extract(index),
                    Channel.blue.extract(index),
                    count};
        }

        @Override
        public boolean equals(Object o) {
            return o == UNITY;
        }
    };

    /**
     * Given a pixel determine which "cube" of the colourspace it fits in. Alpha is ignored
     * @param pixel the pixel (with an aRGB colour)
     * @return the cube index
     */
    public abstract int index(Pixel pixel);

    public abstract int indexSize();

    public abstract int[] coordinates(int index, int count);

    public static class BinnedColourSpace extends ColourSpace {
        /**
         * A sixty four bin colour space
         */
        public static final BinnedColourSpace D64 = new BinnedColourSpace(4);
        public static final BinnedColourSpace D27 = new BinnedColourSpace(3);

        private final int stepCount;
        private final double stepSize;

        public BinnedColourSpace(int bins) {
            this.stepCount = bins;
            this.stepSize = (256.0 / stepCount) - 1;
        }

        /**
         * Given a pixel determine which "cube" of the colourspace it fits in. Alpha is ignored
         *
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

        public int indexSize() {
            return stepCount * stepCount * stepCount;
        }

        /**
         * Create an aRGB colour value representing the mid point of the indexed cube
         *
         * @param index the colour space cube index
         * @return the aRGB colour
         */
        public int midPointColour(int index) {
            int value = (int) ((index * stepSize) + (stepSize / 2));
            return Channel.red.project(value) + Channel.green.project(value) + Channel.blue.project(value);
        }

        @Override
        public int[] coordinates(int index, int count) {
            int b = Math.floorDiv(index , (stepCount * stepCount));
            index = index - (b * stepCount * stepCount);
            int g = Math.floorDiv(index , stepCount);
            index = index - (g * stepCount);
            int r = index;
            return new int[]{r,g,b, count};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BinnedColourSpace that = (BinnedColourSpace) o;
            return stepCount == that.stepCount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stepCount);
        }

        @Override
        public String toString() {
            return "BinnedColourSpace{" +
                    "stepCount=" + stepCount +
                    ", stepSize=" + stepSize +
                    '}';
        }
    }
}
