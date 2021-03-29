package com.asteroid.duck.pointy.indexer.scan.actions;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

public class RemoveFileFromDocument extends IndexAction {
    public RemoveFileFromDocument(String checksum, String filename) {
        super(checksum, filename);
    }

    @Override
    public void process(IndexWriter writer, Document doc) throws IOException {
        // FIXME Implement process
        throw new UnsupportedOperationException("Not implemented");
    }
}
