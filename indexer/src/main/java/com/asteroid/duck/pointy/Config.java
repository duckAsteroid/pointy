package com.asteroid.duck.pointy;

import com.asteroid.duck.pointy.indexer.OptionalField;
import com.asteroid.duck.pointy.indexer.image.BinnedColourSpace;
import com.asteroid.duck.pointy.indexer.image.ColourSpace;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

/**
 * Represents the configuration of the indexing process (i.e. what elements to index)
 */
@Data
@Jacksonized
@Builder(toBuilder = true)
public class Config {
    public enum Analyzer { STANDARD }
    /** What checksum algorithm to use for identity */
    private final Checksum checksum;
    /** Path to the database (root of index and image directory) */
    @JsonIgnore
    private final Path database;
    /** Lucene text analyzer to use for indexing */
    private final Analyzer analyzer;
    /** Root paths to scan for files */
    private final Set<Path> scanRoots;
    /** File types to scan/index */
    private final Set<FileType> fileTypes;
    /** Which file meta data to index */
    private final Set<MetaDataField> metaDataFields;
    /** Which slide fields to index */
    private final Set<OptionalField> slideFields;
    /** Which slideshow fields to index */
    private final Set<OptionalField> showFields;
    /** What scale for slide images */
    private final double imageScale;
    /** What format to store slide images in */
    private final String imageFormat;
    /** Colour space used to analyse image (typically not full RGB) */
    private final ColourSpace imageColourSpace;

    public static final OptionalField[] TEXT = {OptionalField.CONTENT, OptionalField.TITLE};

    public static final OptionalField[] IMAGE = {OptionalField.IMAGE, OptionalField.IMAGE_COLOR_SPACE};

    @JsonIgnore
    public boolean isSlideTextIndexed() {
        return Arrays.stream(TEXT).anyMatch(slideFields::contains);
    }
    @JsonIgnore
    public boolean isSlideImageIndexed() {
        return Arrays.stream(IMAGE).anyMatch(slideFields::contains);
    }
    @JsonIgnore
    public Path getSlideFolder(String checksum) {
        return database.resolve("images/"+checksum);
    }
    @JsonIgnore
    public Path getIndexFolder() {
        return database.resolve("index");
    }
    @JsonIgnore
    public org.apache.lucene.analysis.Analyzer getLuceneAnalyzer() {
        switch (analyzer) {
            default:
            case STANDARD:
                return new StandardAnalyzer();
        }
    }

    public static ConfigBuilder withDefaults() {
        ConfigBuilder builder = builder();
        builder.fileTypes(FileType.all());
        builder.analyzer(Analyzer.STANDARD);
        builder.checksum(Checksum.SHA1);
        builder.metaDataFields(MetaDataField.all());
        builder.showFields(Set.of(OptionalField.TITLE, OptionalField.CONTENT));
        builder.slideFields(OptionalField.all());
        builder.imageScale(0.5);
        builder.imageFormat("JPG");
        builder.imageColourSpace(BinnedColourSpace.D27);
        return builder;
    }

    public static Config readFrom(final Path database) throws IOException {
        if (database == null) throw new IllegalArgumentException("Path cannot be null");
        if (!Files.isDirectory(database)) throw new IllegalArgumentException("Path is not a directory");

        Path cfgFile = database.resolve(".config");
        if (!Files.exists(cfgFile)) throw new IllegalArgumentException("No config file in this directory");

        ObjectMapper mapper = new ObjectMapper();
        try (InputStream stream = Files.newInputStream(cfgFile)) {
            Config cfg = mapper.readValue(stream, Config.class);
            return cfg.toBuilder().database(database).build();
        }
    }
}
