package com.asteroid.duck.pointy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The file types to index and their file suffixes
 */
public enum FileType {

    PPT(".ppt"), PPTX(".pptx");

    private final String suffix;

    FileType(String suffix) {
        this.suffix = suffix;
    }

    public static Set<FileType> all() {
        return new HashSet<>(Arrays.asList(values()));
    }

    public String getSuffix() {
        return suffix;
    }
}
