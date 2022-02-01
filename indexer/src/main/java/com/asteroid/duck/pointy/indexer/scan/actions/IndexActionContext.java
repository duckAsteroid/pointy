package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IndexActionContext {
    Config getConfig();
    Optional<Document> getCurrentDocument(String hashcode);
    void add(Document document) throws IOException;
    void delete(String hashcode) throws IOException;
    void updateDocument(String checksum, List<IndexableField> fields) throws IOException;
}
