package com.asteroid.duck.pointy.indexer.scan;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * A view of an index that provides an iterator over the current documents.
 */
public class IterableIndex implements Iterable<Document> {
    private final IndexReader reader;
    private final Set<String> loaded;

    public IterableIndex(IndexReader reader, Set<String> loaded) {
        this.reader = reader;
        this.loaded = loaded;
    }

    public IterableIndex(IndexReader reader, String ... fields) {
        this.reader = reader;
        this.loaded = Set.of(fields);
    }

    @Override
    public Iterator<Document> iterator() {
        return new Iterator<>() {
            private int doc = 0;

            @Override
            public boolean hasNext() {
                return doc < reader.numDocs();
            }

            @Override
            public Document next() {
                try {
                    return reader.document(doc++, loaded);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public int size() {
        return reader.numDocs();
    }
}
