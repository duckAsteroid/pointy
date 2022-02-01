package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.scan.Candidate;
import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import com.asteroid.duck.pointy.indexer.scan.actions.IndexActionContext;
import io.github.duckasteroid.progress.ProgressMonitor;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.asteroid.duck.pointy.indexer.metadata.CoreFields.CHECKSUM_FIELD;

/**
 * Performs indexing using a supplied configuration - either updating
 * or creating the index as required.
 */
public class Indexer implements AutoCloseable, IndexActionContext {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final Config config;

    private final IndexWriter writer;

    private DirectoryReader reader;

    private IndexSearcher searcher;

    public Indexer(Config cfg) throws IOException {
        this.config = cfg;
        Path indexFolder = cfg.getIndexFolder();
        Directory indexDirectory = FSDirectory.open(indexFolder);
        IndexWriterConfig indexConfig= new IndexWriterConfig(cfg.getLuceneAnalyzer());
        this.writer = new IndexWriter(indexDirectory, indexConfig);
        this.reader = DirectoryReader.open(writer);
        this.searcher = new IndexSearcher(reader);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Optional<Document> getCurrentDocument(String hashcode) {
        try {
            TopDocs topDocs = searcher.search(new TermQuery(getChecksumTerm(hashcode)), 1);
            if (topDocs.totalHits.value > 0) {
                return Optional.of(reader.document(topDocs.scoreDocs[0].doc));
            }
        }
        catch(IOException e) {
            LOG.error("Error processing index action", e);
        }
        return Optional.empty();
    }

    public Term getChecksumTerm(String checksum) {
        return new Term(CHECKSUM_FIELD.getFieldName(), checksum);
    }

    @Override
    public void add(Document document) throws IOException {
        writer.addDocument(document);
    }

    @Override
    public void updateDocument(String checksum, List<IndexableField> fields) throws IOException {
        writer.updateDocument(getChecksumTerm(checksum), fields);
    }

    @Override
    public void delete(String hashcode) throws IOException {
        writer.deleteDocuments(getChecksumTerm(hashcode));
    }

    private void reloadReader() throws IOException {
        this.reader = DirectoryReader.openIfChanged(reader);
        this.searcher = new IndexSearcher(reader);
    }

    public void index(ProgressMonitor monitor) throws IOException {
        monitor.setSize(4);
        Stream<Path> allFiles = config.getScanRoots().stream()
                .flatMap(root -> new FileScanner(config.getFileTypes()).listFiles(root));
        Stream<Candidate> candidates = Candidate.candidates(allFiles, config.getChecksum());

        ProgressMonitor currentIndexState = monitor.newSubTask("Read current index state");
        PointyIndexReader pointyIndexReader = new PointyIndexReader(reader);
        SetValuedMap<String, String> currentIndex = pointyIndexReader.currentIndex(currentIndexState);

        ProgressMonitor calculateIndexActions = monitor.newSubTask("Calculate index update jobs");
        IndexUpdateJob indexUpdateJob = new IndexUpdateJob(currentIndex);
        indexUpdateJob.process(candidates, calculateIndexActions);

        ProgressMonitor processUpdateActions = monitor.newSubTask("Process update actions", 2);
        //processUpdateActions.setSize(indexUpdateJob.size());
        indexUpdateJob.getActions().parallel().forEach(action -> action.safeProcess(this, processUpdateActions.newSubTask(action.getName(),1)));
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
