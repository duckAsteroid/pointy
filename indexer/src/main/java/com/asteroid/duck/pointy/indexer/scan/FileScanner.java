package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides a depth first recursive stream of files that end with a desired suffix from a given starting path.
 * i.e. A way to find all powerpoint files under a directory
 */
public class FileScanner {
    private static final Logger LOG = LoggerFactory.getLogger(FileScanner.class);
    /** Used to filter out only the files of interest */
    private final Predicate<Path> fileFilter;

    public FileScanner(final Set<FileType> suffixes) {
        this.fileFilter = path -> {
            String pathName = path.getFileName().toString().toLowerCase(Locale.ROOT);
            for(FileType type : suffixes) {
                String suffix = type.getSuffix().toLowerCase(Locale.ROOT);
                if (pathName.endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Create a stream of files matching the filters starting at the given path root
     * @param path the root path to index from
     * @return the paths that match the filter
     */
    public Stream<Path> listFiles(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path).flatMap(this::listFiles).filter(fileFilter);
            }
            catch (IOException e) {
                LOG.error("Error reading "+path, e);
                return Stream.empty();
            }
        } else {
            return Stream.of(path);
        }
    }
}
