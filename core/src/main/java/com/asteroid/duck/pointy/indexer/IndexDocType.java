package com.asteroid.duck.pointy.indexer;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

/**
 * We index two things - slides and slideshows
 */
public enum IndexDocType {

    SLIDESHOW, SLIDE;

    public static final String FIELD_NAME = "DocType";

    /**
     * A field that we add to the index to represent which "document type" is being stored
     * @return A field to go in the index
     */
    public IndexableField asField() {
        return new StringField(FIELD_NAME, name(), Field.Store.YES);
    }

    public static IndexDocType parse(String name) {
        for(IndexDocType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
