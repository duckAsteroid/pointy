package com.asteroid.duck.pointy;

public enum FileType {

    PPT(".ppt"), PPTX(".pptx");

    private final String suffix;

    FileType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
