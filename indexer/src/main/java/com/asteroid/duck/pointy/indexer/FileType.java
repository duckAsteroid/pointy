package com.asteroid.duck.pointy.indexer;

import java.nio.file.Path;
import java.util.Optional;

public enum FileType {
    PPT, PPTX;

    public static Optional<FileType> match(final Path p) {
        if (p != null) {
            final String name = p.toString().toLowerCase();
            if (name.endsWith(".ppt")) {
                return Optional.of(PPT);
            }
            else if (name.endsWith(".pptx")) {
                return Optional.of(PPTX);
            }
        }
        return Optional.empty();
    }
}
