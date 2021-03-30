package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.scan.Candidate;
import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import com.asteroid.duck.pointy.indexer.scan.IterableIndex;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexAction;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.duck.asteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Indexer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final IndexWriter writer;
    private final IndexReader reader;
    private final Path outputDir;
    private final Config config;

    public Indexer(Config cfg) {
        this.config = cfg;
        this.reader = null;
        this.writer = null;
        this.outputDir = null;
    }

    public void index(ProgressMonitor monitor) throws IOException {
        Stream<Path> allFiles = config.getScanRoots().stream()
                .flatMap(root -> new FileScanner(config.getFileTypes()).listFiles(root));
        Stream<Candidate> candidates = Candidate.candidates(allFiles, config.getChecksum());
        IndexUpdateJob indexUpdateJob = new IndexUpdateJob(currentIndex());
        indexUpdateJob.process(candidates);

        //
        IndexContext ctx = null;
        indexUpdateJob.getActions().forEach(action -> action.safeProcess(ctx));
    }

    private Map<String, List<String>> currentIndex() {
        Map<String, List<String>> current = new HashMap<>();
        IterableIndex iterableIndex = new IterableIndex(reader);
        for(Document doc : iterableIndex) {
            String hash = doc.get(PipelineStage.CHECKSUM_FIELD);
            String[] filenames = doc.getValues(PipelineStage.FILENAME_FIELD);
            current.put(hash, Arrays.asList(filenames));
        }
        return current;
    }


    @Override
    public void close() throws IOException {
        try {
            if (writer != null) {
                writer.close();
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

    }
}
