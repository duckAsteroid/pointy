package com.asteroid.duck.pointy.indexer.checksum;

import java.io.IOException;
import java.nio.file.Path;

public interface Checksum {
    String compute(Path path) throws IOException;
}
