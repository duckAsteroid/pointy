package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Checksum;
import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.FileType;
import com.asteroid.duck.pointy.indexer.image.ColourSpace;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataField;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length >= 2) {
            Path outputDir = Paths.get(args[0]);
            Config.ConfigBuilder builder = Config.builder();
            builder.fileTypes(FileType.all());
            builder.analyzer(new StandardAnalyzer());
            builder.checksum(Checksum.SHA1);
            builder.metaDataFields(MetaDataField.all());
            builder.showFields(Set.of(OptionalField.TITLE, OptionalField.CONTENT));
            builder.slideFields(OptionalField.all());
            builder.imageScale(0.5);
            builder.imageFormat("JPG");
            builder.imageColourSpace(ColourSpace.BinnedColourSpace.D27);

            builder.database(outputDir);

            Set<Path> paths = Stream.of(args).skip(1).map(Paths::get).collect(Collectors.toSet());
            builder.scanRoots(paths);

            Config config = builder.build();
            try(Indexer indexer = new Indexer(config)) {
                indexer.index(null);
            }
        }
        else {
            System.out.println("Usage: outputFolder, [inputFolder, ...]");
        }
    }
}
