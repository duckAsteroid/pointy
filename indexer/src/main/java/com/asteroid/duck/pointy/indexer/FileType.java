package com.asteroid.duck.pointy.indexer;

public enum FileType {

    PPT(".ppt"), PPTX(".pptx");

    private final String suffix;

    FileType(String suffix) {
        this.suffix = suffix;
    }

}
