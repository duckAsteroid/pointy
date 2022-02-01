package com.asteroid.duck.pointy.indexer.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the optional fields on slides and/or shows that might be indexed
 */
public enum OptionalField {
    SLIDE_NO,
    TITLE,
    CONTENT,
    IMAGE,
    IMAGE_COLOR_SPACE;

    public static Set<OptionalField> all() {
        return new HashSet<>(Arrays.asList(values()));
    }
}
