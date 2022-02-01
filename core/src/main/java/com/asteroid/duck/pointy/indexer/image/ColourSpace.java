package com.asteroid.duck.pointy.indexer.image;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * How to convert RGB integer pixel values into a simplified 3 dimensional space - N bins per dimension
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class ColourSpace {

    /**
     * The standard RGB colourspace - no transformation occurs
     */
    public static final ColourSpace RGB = new ColourSpace() {
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
        public int[] coordinates(int index) {
            return new int[] {
                    Channel.red.extract(index),
                    Channel.green.extract(index),
                    Channel.blue.extract(index)};
        }

        @Override
        public boolean equals(Object o) {
            return o == RGB;
        }
    };

    /**
     * Given a pixel determine which "cube" of the colourspace it fits in. Alpha is ignored.
     * @param pixel the pixel (with an aRGB colour)
     * @return the cube index
     */
    public abstract int index(Pixel pixel);

    public abstract int indexSize();

    public abstract int[] coordinates(int index, int count);

    public abstract int[] coordinates(int index);

}
