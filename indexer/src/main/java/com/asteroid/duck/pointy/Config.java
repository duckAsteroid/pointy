package com.asteroid.duck.pointy;

import com.asteroid.duck.pointy.indexer.OptionalField;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataField;
import lombok.Builder;
import lombok.Data;
import org.apache.lucene.analysis.Analyzer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

/**
 * Represents the configuration of the indexing process (i.e. what elements to index)
 */
@Data
@Builder
public class Config {
    private final Checksum checksum;
    private final Path database;
    private final Analyzer analyzer;
    private final Set<Path> scanRoots;
    private final Set<FileType> fileTypes;
    private final Set<MetaDataField> metaDataFields;
    private final Set<OptionalField> slideFields;
    private final Set<OptionalField> showFields;

    public static final OptionalField[] TEXT = {OptionalField.CONTENT, OptionalField.TITLE};

    public static final OptionalField[] IMAGE = {OptionalField.IMAGE, OptionalField.IMAGE_COLOR_SPACE};

    public boolean isSlideTextIndexed() {
        return Arrays.stream(TEXT).anyMatch(slideFields::contains);
    }

    public boolean isSlideImageIndexed() {
        return Arrays.stream(IMAGE).anyMatch(slideFields::contains);
    }

    public Path getSlideFolder(String checksum) {
        return database.resolve("images/"+checksum);
    }

    public Path getIndexFolder() {
        return database.resolve("index");
    }
}
