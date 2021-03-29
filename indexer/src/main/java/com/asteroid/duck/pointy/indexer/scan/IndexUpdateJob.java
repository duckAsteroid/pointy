package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.scan.actions.AddFileToDocument;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexAction;
import com.asteroid.duck.pointy.indexer.scan.actions.NewFile;
import com.asteroid.duck.pointy.indexer.scan.actions.RemoveFileFromDocument;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * A single instance of index updating (to match file system)
 * Takes the current index (documents) and a stream of Paths
 * From this calculates what actions need to be performed in this job
 */
public class IndexUpdateJob {
    /** Filenames, and their hashes - that we already have in the index */
    private final Map<String, String> currentFileHashes;
    private final Set<String> currentHashes;

    private final Collection<IndexAction> actions = new LinkedList<>();

    public IndexUpdateJob(Map<String, String> currentFileHashes) {
        this.currentFileHashes = currentFileHashes;
        this.currentHashes = new HashSet<>(currentFileHashes.values());
    }

    public void process(Stream<Candidate> candidateStream) {
        candidateStream.forEach(this::process);
    }

    public void process(Candidate c) {
        final String filename = c.getPath().toString();
        final String checksum = c.getChecksum();
        if(!currentFileHashes.containsKey(filename)) {
            newOrUpdatedFile(checksum, filename);
        }
        else {
            final String currentHash = currentFileHashes.get(filename);
            // remove from current
            actions.add(new RemoveFileFromDocument(currentHash, filename));
            newOrUpdatedFile(checksum, filename);
        }
    }

    private void newOrUpdatedFile(String checksum, String filename) {
        if (currentHashes.contains(checksum)) {
            actions.add(new AddFileToDocument(checksum, filename));
        }
        else {
            actions.add(new NewFile(checksum, filename));
        }
    }
}
