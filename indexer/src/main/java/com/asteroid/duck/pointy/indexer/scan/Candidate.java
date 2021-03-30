package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.Checksum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Represents a file in the file system together with it's computed checksum
 */
@Data
public class Candidate {
    private static final Logger LOG = LoggerFactory.getLogger(Candidate.class);

    private final Path path;
    private final String checksum;

    /**
     * Create a stream of candidates from a stream of paths and a checksum algorithm
     * @param paths the paths to process
     * @param checksum the checksum algorithm to use
     * @return a stream of candidates
     */
    public static Stream<Candidate> candidates(Stream<Path> paths, Checksum checksum) {
        return paths.map(path -> {
            Candidate candy = null;
            try {
                String cs = checksum.apply(path).get();
                candy = new Candidate(path, cs);
            } catch (InterruptedException e) {
                LOG.error("Interrupted while creating checksum "+ path, e);
            } catch (ExecutionException e) {
                LOG.error("Error while creating checksum "+ path, e);
            }
            return Optional.ofNullable(candy);
        }).filter(Optional::isPresent).map(Optional::get);
    }

    public static Map<String, List<Path>> scanResult(Stream<Candidate> stream) {
        return stream.collect(groupingBy(Candidate::getChecksum, mapping(Candidate::getPath, toList())));
    }
}
