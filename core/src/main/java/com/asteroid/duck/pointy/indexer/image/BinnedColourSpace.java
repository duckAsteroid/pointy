package com.asteroid.duck.pointy.indexer.image;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Objects;

@Data
@Builder
@Jacksonized
@EqualsAndHashCode(callSuper = false)
public class BinnedColourSpace extends ColourSpace {
	/**
	 * A sixty four bin colour space (4 per dimension)
	 */
	public static final BinnedColourSpace D64 = BinnedColourSpace.create(4);
	/**
	 * A twenty seven bin colour space (3 per dimension)
	 */
	public static final BinnedColourSpace D27 = BinnedColourSpace.create(3);

	private final int stepCount;

	private transient final double stepSize;

	public BinnedColourSpace(int bins, double stepSize) {
		this.stepCount = bins;
		this.stepSize = stepSize;
	}

	public static BinnedColourSpace create(int bins) {
		return BinnedColourSpace.builder().stepCount(bins).stepSize((256.0 / bins) - 1).build();
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
		int[] coords = new int[]{0, 0, 0, count};
		System.arraycopy(coordinates(index), 0, coords, 0, 3);
		return coords;
	}

	public int[] coordinates(int index) {
		int b = Math.floorDiv(index, (stepCount * stepCount));
		index = index - (b * stepCount * stepCount);
		int g = Math.floorDiv(index, stepCount);
		index = index - (g * stepCount);
		int r = index;
		return new int[]{r, g, b};
	}

}
