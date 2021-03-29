package com.asteroid.duck.pointy.indexer;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

public enum IndexDocType {

    SLIDESHOW, SLIDE;

    public static final String FIELD_NAME = "DocType";

    public IndexableField asField() {
        return new StringField(FIELD_NAME, name(), Field.Store.YES);
    }
}
