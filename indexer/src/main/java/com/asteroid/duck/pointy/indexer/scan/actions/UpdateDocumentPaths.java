package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.asteroid.duck.pointy.indexer.PipelineStage.FILENAME_FIELD;

/**
 * An action to update the list of paths associated with a hashed file
 */
public class UpdateDocumentPaths extends IndexAction {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateDocumentPaths.class);

    private final Collection<String> paths;

    public UpdateDocumentPaths(String checksum, Collection<String> paths) {
        super(checksum);
        this.paths = paths;
    }

    @Override
    public void process(IndexContext ctx) throws IOException {
        Document doc = document(ctx).orElseThrow(() -> new IllegalArgumentException("Can't find document with ID="+getChecksum()));
        // create a collection containing everything but the filenames
        List<IndexableField> fields = new ArrayList<>(doc.getFields()).stream()
                .filter(f -> !f.name().equals(FILENAME_FIELD)).collect(Collectors.toList());
        // create a list of fields containing the new paths
        List<StringField> newPathFields = paths.stream()
                .map(path -> new StringField(FILENAME_FIELD, path, Field.Store.YES))
                .collect(Collectors.toList());
        // add them in
        fields.addAll(newPathFields);
        // update the index
        ctx.getWriter().updateDocument(getDocumentID(), fields);
    }
}
