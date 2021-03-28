package com.asteroid.duck.pointy.indexer.scan;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Iterator;

/**
 * A view of an index that provides an iterator over the documents
 */
public class IterableIndex implements Iterable<Document> {
    private final IndexReader reader;

    public IterableIndex(IndexReader reader) {
        this.reader = reader;
    }

    @Override
    public Iterator<Document> iterator() {
        return new Iterator<Document>() {
            private int doc = 0;

            @Override
            public boolean hasNext() {
                return doc < reader.numDocs();
            }

            @Override
            public Document next() {
                try {
                    return reader.document(doc++);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
