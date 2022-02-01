package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.scan.actions.*;
import io.github.duckasteroid.progress.ProgressMonitor;
import org.apache.commons.collections4.SetValuedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A single instance of index updating process (trying to match what is now in file system).
 * Takes the current index (documents) and a stream of {@link Candidate}s (files)
 * From this calculates what actions need to be performed in this job
 */
public class IndexUpdateJob {
    private static final Logger LOG = LoggerFactory.getLogger(IndexUpdateJob.class);
    /** The files currently in the index, by their file content hash */
    SetValuedMap<String, String> filesByHash;
    /** the resulting index actions for each file */
    private final Collection<IndexAction> actions = new LinkedList<>();

    public IndexUpdateJob(SetValuedMap<String, String> filesByHash) {
        this.filesByHash = filesByHash;
    }

    public void process(Stream<Candidate> candidateStream, ProgressMonitor monitor) {
        // convert candidate into a map of the paths for each file by its hash
        Map<String, List<Path>> scanResult = Candidate.scanResult(candidateStream);
        Set<Map.Entry<String, List<Path>>> entries = scanResult.entrySet();
        monitor.setSize(entries.size());
        // iterate each entry
        for(Map.Entry<String, List<Path>> entry : entries) {
            String hash = entry.getKey();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Indexing content hash="+hash+" to locations: "+entry.getValue().stream().map(Path::toString).collect(Collectors.joining(",","[","]")));
            }
            if (filesByHash.containsKey(hash)) {
                // hash exists in current index
                // check the filenames
                List<String> filenames = entry.getValue().stream().map(IndexUpdateJob::pathString).collect(toList());
                Set<String> currentFiles = filesByHash.get(hash);
                if(filenames.containsAll(currentFiles) && currentFiles.containsAll(filenames)) {
                    // same lists ... do nothing
                    actions.add(new DoNothingAction(hash));
                }
                else {
                    // need to update the document
                    actions.add(new UpdateDocumentPaths(hash, filenames));
                }
            }
            else {
                // new file
                actions.add(new NewFileAction(hash, entry.getValue()));
            }
            monitor.worked(1);
        }
        Set<String> newHashes = scanResult.keySet();
        Set<String> hashes = new HashSet<>(filesByHash.keySet());
        // by taking the "new hashes" away from the old - we are left with those that can be purged
        boolean difference = hashes.removeAll(newHashes);
        actions.addAll(hashes.stream().map(RemoveFromIndexAction::new).collect(toList()));
    }

    /**
     * A library function that turns filesystem paths into strings we use in the index
     * @param path the path value to index
     * @return the string form
     */
    public static String pathString(Path path) {
        return path.toString();
    }

    public int size() {
        return actions.size();
    }

    public Stream<IndexAction> getActions() {
        return actions.stream();
    }
}
