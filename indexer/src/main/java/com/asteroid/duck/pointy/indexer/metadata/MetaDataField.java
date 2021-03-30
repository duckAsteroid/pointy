package com.asteroid.duck.pointy.indexer.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MetaDataField {
    AUTHOR, TITLE, KEYWORDS, COMMENTS, CREATED_DATE, LAST_SAVED_DATE, LAST_AUTHOR;

    public static Set<MetaDataField> all() {
        return new HashSet<>(Arrays.asList(values()));
    }
}
