package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides a depth first recursive stream of files from a given starting path
 */
public class FileScanner {
    private static final Logger LOG = LoggerFactory.getLogger(FileScanner.class);

    private static final Predicate<Path> fileFilter = path -> FileType.match(path).isPresent();

    public static Stream<Path> listFiles(Path path) {
        if (Files.isDirectory(path)) {
            try { return Files.list(path).filter(fileFilter).flatMap(FileScanner::listFiles); }
            catch (Exception e) { return Stream.empty(); }
        } else {
            return Stream.of(path);
        }
    }
}
