package com.asteroid.duck.pointy.indexer;

import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.usermodel.Slide;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The interface to a stage in the indexing pipeline
 */
public interface PipelineStage {
    /**
     * Given a slide in a powerpoint extract some field for the index
     * @param slide the slide to index
     * @return the field for the document index
     */
    Stream<IndexableField> extract(Slide<?,?> slide);
}
