package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.scan.Candidate;
import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import com.asteroid.duck.pointy.indexer.scan.IterableIndex;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexActionContext;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import io.github.duckasteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Performs indexing using a supplied configuration - either updating
 * or creating the index as required.
 */
public class Indexer implements AutoCloseable, IndexActionContext {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final Config config;

    private final IndexWriter writer;

    private final IndexReader reader;

    public Indexer(Config cfg) throws IOException {
        this.config = cfg;
        Path indexFolder = cfg.getIndexFolder();
        Directory indexDirectory = FSDirectory.open(indexFolder);
        IndexWriterConfig indexConfig= new IndexWriterConfig(cfg.getLuceneAnalyzer());
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

    private SetValuedMap<String, String> currentIndex() {
        SetValuedMap<String, String> current = new HashSetValuedHashMap<>();
        if (reader != null) {
            IterableIndex iterableIndex = new IterableIndex(reader);
            for (Document doc : iterableIndex) {
                String hash = doc.get(IndexFieldProvider.CHECKSUM_FIELD);
                String[] filenames = doc.getValues(IndexFieldProvider.FILENAME_FIELD);
                current.putAll(hash, Arrays.asList(filenames));
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
