package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.usermodel.Slide;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SlideImageIndexer implements PipelineStage {
    private final Path slideImageFolder;
    private final Slide<?, ?> slide;

    public SlideImageIndexer( Path slideImageFolder, Slide<?, ?> slide) {
        this.slideImageFolder = slideImageFolder;
        this.slide = slide;
    }

    @Override
    public List<IndexableField> index(Config cfg) {
        return Collections.emptyList();
    }

    private Path thumbnailPath(Config cfg) {
        // FIXME get image format from CFG
        return slideImageFolder.resolve(slide.getSlideNumber() + ".png");
    }
}
