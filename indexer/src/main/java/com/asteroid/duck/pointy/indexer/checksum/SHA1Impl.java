package com.asteroid.duck.pointy.indexer.checksum;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class SHA1Impl implements Checksum {
    private static final Logger LOG = LoggerFactory.getLogger(SHA1Impl.class);

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private final MessageDigest digest;
    public SHA1Impl() throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance("SHA-1");
    }
    public String compute(Path path) throws IOException {
        digest.reset();
        InputStream stream;
        try (DigestInputStream digestInputStream = new DigestInputStream(Files.newInputStream(path), digest))
        {
            digestInputStream.transferTo(OutputStream.nullOutputStream());
            return hexBinary(digest.digest());
        }
    }


    public String hexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
