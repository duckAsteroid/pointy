package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.indexer.PipelineStage;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.asteroid.duck.pointy.indexer.PipelineStage.FILENAME_FIELD;

public class AddFileToDocument extends IndexAction {
    private static final Logger LOG = LoggerFactory.getLogger(AddFileToDocument.class);

    public AddFileToDocument(String checksum, String filename) {
        super(checksum, filename);
    }

    @Override
    public void process(IndexWriter writer, Document doc) throws IOException {
        List<IndexableField> fields = new ArrayList<>(doc.getFields());
        if (fields.stream()
                .filter(f -> f.name().equals(FILENAME_FIELD))
                .map(IndexableField::stringValue)
                .anyMatch(filename::equals)) {
            LOG.warn("Already mapped checksum:"+checksum+" to "+filename);
            return; // do not update
        }
        else {
            fields.add(new StringField(FILENAME_FIELD, filename, Field.Store.YES));
        }
        writer.updateDocument(getDocumentID(), fields);
    }
}
