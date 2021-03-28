package com.asteroid.duck.pointy.indexer.checksum;

import com.asteroid.duck.pointy.Checksum;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class CRC32ImplTest {
    @TempDir
    static Path sharedTempDir;
    private final static List<Timing> timings = new ArrayList<>();
    @BeforeAll
    public static void setup() throws IOException {
        Path copy = sharedTempDir.resolve("temp.pptx");
        IOUtils.copy(CRC32ImplTest.class.getResourceAsStream("/presentations/Test1.pptx"), Files.newOutputStream(copy));
    }
    @RepeatedTest(10)
    public void testCompute() throws IOException, ExecutionException, InterruptedException {
        final Path file = sharedTempDir.resolve("temp.pptx");
        final Timing timing = new Timing();
        timing.start();
//        CRC32Impl sha1 = new CRC32Impl();
        timing.created();
        final String checksum = Checksum.CRC32.apply(file).get();
        timing.checksumComplete();
        timings.add(timing);
        System.out.println("CRC32="+checksum);
    }

    @AfterAll
    public static void dump() {
        System.out.println("Creation:");
        System.out.println("\tavg=" + timings.stream().mapToLong(Timing::getCreation).average() + "ns");
        System.out.println("\tmin=" + timings.stream().mapToLong(Timing::getCreation).min() + "ns");
        System.out.println("\tmax=" + timings.stream().mapToLong(Timing::getCreation).max() + "ns");

        System.out.println("Checksum:");
        System.out.println("\tavg=" + timings.stream().mapToLong(Timing::getChecksum).average() + "ns");
        System.out.println("\tavg=" + (timings.stream().mapToLong(Timing::getChecksum).average().orElseThrow() / 1000) + "ms");
        System.out.println("\tmin=" + timings.stream().mapToLong(Timing::getChecksum).min() + "ns");
        System.out.println("\tmax=" + timings.stream().mapToLong(Timing::getChecksum).max() + "ns");
    }

}