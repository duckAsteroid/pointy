package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.image.*;
import com.asteroid.duck.pointy.indexer.metadata.CoreFields;
import com.asteroid.duck.pointy.indexer.metadata.OptionalField;
import org.apache.commons.math3.util.Pair;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.usermodel.Slide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Extracts images from the slides for subsequent analysis (and to help with the UI).
 */
public class SlideImageIndexer implements IndexFieldProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SlideImageIndexer.class);

    private final Path slideImageFolder;
    private final Slide<?, ?> slide;

    public SlideImageIndexer( Path slideImageFolder, Slide<?, ?> slide) {
        this.slideImageFolder = slideImageFolder;
        this.slide = slide;
    }

    @Override
    public List<IndexableField> index(Config cfg) {
        List<IndexableField> result = new LinkedList<>();
        ImageExtractor extractor = new ImageExtractor(cfg.getImageScale(), slide.getSlideShow().getPageSize());
        try {
            BufferedImage bufferedImage = extractor.render(slide);
            if (cfg.getSlideFields().contains(OptionalField.IMAGE_COLOR_SPACE)) {
                ColourSpace space = cfg.getImageColourSpace();
                List<Long> histogram = ColourUtils.histogram(bufferedImage, space);
                List<Double> counts = Histogram.normalisePixelCount(histogram);
                List<Integer> ranged = counts.stream().map(d -> (int) (d * 255)).collect(Collectors.toUnmodifiableList());
                List<Pair<Integer, Integer>> indexData = IntStream.range(0, ranged.size())
                        .filter(i -> ranged.get(i) > 0)
                        .mapToObj(i -> Pair.create(i, ranged.get(i)))
                        .collect(toList());
                indexData.stream()
                        .map(pair -> space.coordinates(pair.getKey(), pair.getValue()))
                        .map(coords -> new IntPoint("colours", coords))
                        .forEach(result::add);
                String histogramRender = indexData.stream().map(pair -> pair.getKey() + ":" + pair.getValue())
                        .collect(Collectors.joining(", ", "[", "]"));
                result.add(new StoredField("histogram", histogramRender));
            }

            if (cfg.getSlideFields().contains(OptionalField.IMAGE)) {
                Path thumbnailPath = thumbnailPath(cfg);
                try {
                    Files.createDirectories(thumbnailPath.getParent());
                    ImageIO.write(bufferedImage, cfg.getImageFormat(), Files.newOutputStream(thumbnailPath));
                    result.add(new StoredField(CoreFields.THUMBNAIL_PATH_FIELD.getFieldName(), thumbnailPath.toString()));
                    String checksum = cfg.getChecksum().apply(thumbnailPath).get();
                    result.add(new StringField(CoreFields.CHECKSUM_FIELD.getFieldName(), checksum, Field.Store.YES));
                } catch (IOException ioe) {
                    LOG.error("Unable to write thumbnail file", ioe);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error computing checksum", e);
                }
            }
        }
        catch(Throwable ioe) {
            LOG.error("Error extracting image", ioe);
        }
        return result;
    }

    private Path thumbnailPath(Config cfg) {
        return slideImageFolder.resolve(slide.getSlideNumber() + "."+cfg.getImageFormat().toLowerCase(Locale.ROOT));
    }
}
