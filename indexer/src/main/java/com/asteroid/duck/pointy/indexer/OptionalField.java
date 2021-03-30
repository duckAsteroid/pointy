package com.asteroid.duck.pointy.indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent optional fields on slides and/or shows
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
