package com.asteroid.duck.pointy.indexer.scan.actions;

import org.apache.lucene.document.Document;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;

import static com.asteroid.duck.pointy.indexer.PipelineStage.CHECKSUM_FIELD;


public abstract class IndexAction {
    protected final String checksum;
    protected final String filename;

    protected IndexAction(String checksum, String filename) {
        this.checksum = checksum;
        this.filename = filename;
    }

    public String getChecksum() {
        return checksum;
    }

    public Term getDocumentID() {
        return new Term(CHECKSUM_FIELD, getChecksum());
    }

    public abstract void process(IndexWriter writer, Document doc) throws IOException;
}
