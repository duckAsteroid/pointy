package com.asteroid.duck.pointy.indexer;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

public enum IndexDocType {
    SLIDESHOW, SLIDE;

    public IndexableField asField() {
        return new StringField(Fields.TYPE.name(), name(), Field.Store.YES);
    }
}
