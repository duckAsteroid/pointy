package com.asteroid.duck.pointy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * Interface to an object that can compute a checksum for a file.
 * This is used for "uniqueness" (so we only index a file once - even if it occurs in multiple places)
 */
public enum Checksum implements Function<Path, CompletableFuture<String>> {
    CRC32 {
        private final CRC32 crc = new CRC32();
        public CompletableFuture<String> apply(Path path) {
                return CompletableFuture.supplyAsync(() -> {
                    crc.reset();
                    try (CheckedInputStream checkedInputStream = new CheckedInputStream(Files.newInputStream(path), crc)) {
                        checkedInputStream.transferTo(OutputStream.nullOutputStream());
                        final String hex = Long.toHexString(checkedInputStream.getChecksum().getValue());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(path.toString() + ": " + hex);
                        }
                        return hex;
                    } catch(IOException ioe) {
                        throw new CompletionException("Error reading file", ioe);
                    }
                });
        }
    },
    SHA1 {
        @Override
        public CompletableFuture<String> apply(Path path) {
            return CompletableFuture.supplyAsync(() -> {
                digest.reset();
                InputStream stream;
                try (DigestInputStream digestInputStream = new DigestInputStream(Files.newInputStream(path), digest))
                {
                    digestInputStream.transferTo(OutputStream.nullOutputStream());
                    byte[] digestBytes = digest.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte digestByte : digestBytes) {
                        sb.append(Integer.toHexString(digestByte & 0xFF).toUpperCase());
                    }
                    final String hex = sb.toString();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(path.toString() + ": " + hex);
                    }
                    return hex;
                } catch(IOException ioe) {
                    throw new CompletionException("Error reading file", ioe);
                }
            });
        }

        private final MessageDigest digest;
        {
            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("No SHA-1 digest!?", e);
            }
        }

    };

    private static final Logger LOG = LoggerFactory.getLogger(Checksum.class);
}
