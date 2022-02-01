package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.index.IndexableField;

import java.util.List;

/**
 * The interface to a stage in the indexing pipeline - something that can create fields
 */
public interface IndexFieldProvider {

    List<IndexableField> index(Config cfg);
}
