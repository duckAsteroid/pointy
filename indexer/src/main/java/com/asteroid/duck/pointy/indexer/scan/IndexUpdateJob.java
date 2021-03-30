package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.scan.actions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A single instance of index updating (to match file system)
 * Takes the current index (documents) and a stream of Paths
 * From this calculates what actions need to be performed in this job
 */
public class IndexUpdateJob {
    private static final Logger LOG = LoggerFactory.getLogger(IndexUpdateJob.class);
    /** The files in the index, by their file content hash */
    Map<String, List<String>> filesByHash;
    /** the resulting index actions for each file */
    private final Collection<IndexAction> actions = new LinkedList<>();

    public IndexUpdateJob(Map<String, List<String>> filesByHash) {
        this.filesByHash = filesByHash;
    }

    public void process(Stream<Candidate> candidateStream) {
        Map<String, List<Path>> scanResult = Candidate.scanResult(candidateStream);
        for(Map.Entry<String, List<Path>> entry : scanResult.entrySet()) {
            String hash = entry.getKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Indexing content hash="+hash+" to locations: "+entry.getValue().stream().map(Path::toString).collect(Collectors.joining(",","[","]")));
            }
            if (filesByHash.containsKey(hash)) {
                // hash exists in current index
                // check the filenames
                List<String> filenames = entry.getValue().stream().map(IndexUpdateJob::pathString).collect(toList());
                List<String> currentFiles = filesByHash.get(hash);
                if(filenames.containsAll(currentFiles) && currentFiles.containsAll(filenames)) {
                    // same lists ...
                    actions.add(new DoNothingAction(hash));
                }
                else {
                    actions.add(new UpdateDocumentPaths(hash, filenames));
                }
            }
            else {
                // new file
                actions.add(new NewFileAction(hash, entry.getValue()));
            }
        }
        Set<String> newHashes = scanResult.keySet();
        Set<String> hashes = new HashSet<>(filesByHash.keySet());
        // by taking the "new hashes" away from the old - we are left with those that can be purged
        boolean difference = hashes.removeAll(newHashes);
        actions.addAll(hashes.stream().map(RemoveFromIndexAction::new).collect(toList()));
    }

    public static String pathString(Path path) {
        return path.toString();
    }

    public Stream<IndexAction> getActions() {
        return actions.stream();
    }
}
