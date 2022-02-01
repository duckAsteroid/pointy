package com.asteroid.duck.pointy;

import com.asteroid.duck.pointy.indexer.image.BinnedColourSpace;
import com.asteroid.duck.pointy.indexer.image.ColourSpace;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataField;
import com.asteroid.duck.pointy.indexer.metadata.OptionalField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the configuration of the indexing process (i.e. what elements to index)
 */
@Data
@Jacksonized
@Builder(toBuilder = true)
public class Config {
    /** SLF4j */
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    public static final String CONFIG_FILE = "pointy.config";

    public enum Analyzer { STANDARD }
    /** What checksum algorithm to use for identity */
    private final Checksum checksum;
    /** Path to the database (root of index and image directory) */
    @JsonIgnore // since the path is loaded
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

    /**
     * A builder pre-populated with default values
     */
    public static ConfigBuilder withDefaults() {
        ConfigBuilder builder = builder();
        builder.fileTypes(FileType.all());
        builder.analyzer(Analyzer.STANDARD);
        builder.checksum(Checksum.SHA1);
        builder.metaDataFields(MetaDataField.all());
        builder.showFields(Set.of(OptionalField.TITLE, OptionalField.CONTENT));
        builder.slideFields(OptionalField.all().stream().filter(f -> f !=OptionalField.IMAGE_COLOR_SPACE).collect(Collectors.toSet()));
        builder.imageScale(0.5);
        builder.imageFormat("JPG");
        builder.imageColourSpace(BinnedColourSpace.D27);
        return builder;
    }

    /**
     * Create a config in the given location, possibly overriding the indexed folders
     * @param database the path to the database folder (might already exist and be populated)
     * @param indexFolders the additional paths to index into the database (might be empty)
     * @return The configuration
     */
    public static Config readFrom(final Path database, Set<Path> indexFolders) {
        if (database == null) throw new IllegalArgumentException("Path cannot be null");
        if (!Files.isDirectory(database)) throw new IllegalArgumentException("Path is not a directory");

        Path cfgFile = database.resolve(CONFIG_FILE);
        ConfigBuilder config = Config.withDefaults();
        boolean writeConfig = true;
        if (Files.exists(cfgFile)) {
            try {
                config = readFile(cfgFile).toBuilder();
            } catch (IOException e) {
                LOG.warn("Error reading config file="+cfgFile.toAbsolutePath(), e);
            }
        }

        // check if scan roots modified to include index folders?
        if (config.scanRoots == null) {
            config.scanRoots( new HashSet<>());
        }
        writeConfig = config.scanRoots.addAll(indexFolders);

        Config result = config.database(database).build();

        if (writeConfig) {
            try {
                Config.write(result, cfgFile);
            } catch (IOException e) {
                LOG.error("Unable to write config file="+cfgFile.toAbsolutePath(), e);
            }
        }

        return result;
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private static void write(Config cfg, Path cfgFile) throws IOException {
        try(OutputStream stream = Files.newOutputStream(cfgFile, StandardOpenOption.CREATE)) {
            mapper.writeValue(stream, cfg);
        }
    }

    public static Config readFile(final Path cfgFile) throws IOException {
        try (InputStream stream = Files.newInputStream(cfgFile)) {
            return mapper.readValue(stream, Config.class);
        }
    }
}
