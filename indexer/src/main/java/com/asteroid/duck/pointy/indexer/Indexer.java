package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.scan.Candidate;
import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import com.asteroid.duck.pointy.indexer.scan.IterableIndex;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexAction;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexContext;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.duck.asteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Indexer implements AutoCloseable, IndexContext {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final Config config;

    private final IndexWriter writer;

    private final IndexReader reader;

    public Indexer(Config cfg) throws IOException {
        this.config = cfg;
        Path indexFolder = cfg.getIndexFolder();
        Directory indexDirectory = FSDirectory.open(indexFolder);
        IndexWriterConfig indexConfig= new IndexWriterConfig(cfg.getAnalyzer());
        this.writer = new IndexWriter(indexDirectory, indexConfig);

        this.reader = DirectoryReader.open(writer);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public IndexReader getReader() {
        return reader;
    }

    @Override
    public IndexWriter getWriter() {
        return writer;
    }

    @Override
    public IndexSearcher getSearcher() {
        return new IndexSearcher(reader);
    }

    public void index(ProgressMonitor monitor) throws IOException {
        Stream<Path> allFiles = config.getScanRoots().stream()
                .flatMap(root -> new FileScanner(config.getFileTypes()).listFiles(root));
        Stream<Candidate> candidates = Candidate.candidates(allFiles, config.getChecksum());
        IndexUpdateJob indexUpdateJob = new IndexUpdateJob(currentIndex());
        indexUpdateJob.process(candidates);

        indexUpdateJob.getActions().forEach(action -> action.safeProcess(this));
    }

    private Map<String, List<String>> currentIndex() {
        Map<String, List<String>> current = new HashMap<>();
        if (reader != null) {
            IterableIndex iterableIndex = new IterableIndex(reader);
            for (Document doc : iterableIndex) {
                String hash = doc.get(PipelineStage.CHECKSUM_FIELD);
                String[] filenames = doc.getValues(PipelineStage.FILENAME_FIELD);
                current.put(hash, Arrays.asList(filenames));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current index contains "+current.size()+" pieces of content");
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
