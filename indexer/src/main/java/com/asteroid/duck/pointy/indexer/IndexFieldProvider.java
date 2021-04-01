package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.index.IndexableField;

import java.util.List;

/**
 * The interface to a stage in the indexing pipeline
 */
public interface IndexFieldProvider {
    String CHECKSUM_FIELD = "checksum";
    String FILENAME_FIELD = "filename";

    List<IndexableField> index(Config cfg);
}
