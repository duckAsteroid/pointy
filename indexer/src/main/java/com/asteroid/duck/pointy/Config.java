package com.asteroid.duck.pointy;

import com.asteroid.duck.pointy.indexer.OptionalField;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataField;
import lombok.Data;

import java.nio.file.Path;
import java.util.Set;

/**
 * Represents the configuration of the indexing process (i.e. what elements to index)
 */
@Data
public class Config {
    private final Checksum checksum;
    private final Path database;
    private final Set<Path> scanRoots;
    private final Set<String> suffixes;
    private final Set<MetaDataField> metaDataFields;
    private final Set<OptionalField> slideFields;
    private final Set<OptionalField> showFields;

}
