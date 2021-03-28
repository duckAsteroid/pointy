package com.asteroid.duck.pointy;

import lombok.Data;

import java.nio.file.Path;

/**
 * Represents the configuration of the indexing process (i.e. what elements to index)
 */
@Data
public class Config {
    private final Checksum checksum;
    private final Path database;
}
