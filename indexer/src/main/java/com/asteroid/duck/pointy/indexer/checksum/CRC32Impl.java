package com.asteroid.duck.pointy.indexer.checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CRC32Impl implements Checksum {
    private static final Logger LOG = LoggerFactory.getLogger(CRC32Impl.class);
    private final CRC32 crc = new CRC32();
    public String compute(Path path) throws IOException {
        crc.reset();
        try (
                CheckedInputStream checkedInputStream = new CheckedInputStream(new FileInputStream(path.toFile()), crc)) {
            checkedInputStream.transferTo(OutputStream.nullOutputStream());
            final String hex = Long.toHexString(checkedInputStream.getChecksum().getValue());
            LOG.debug(path.toString() + ": " + hex);
            return hex;
        }
    }
}
