package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.FileType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class FileScannerTest {

    @TempDir
    File tempFolder;

    @BeforeEach
    void createTestStructure() throws IOException {
        FileUtils.write(new File(tempFolder,"ignore.txt"), "ignore me", StandardCharsets.UTF_8);

        File dir1 = new File(tempFolder, "dir1");
        FileUtils.write(new File(dir1,"ignore.txt"), "ignore me", StandardCharsets.UTF_8);
        FileUtils.write(new File(dir1,"include.ppt"), "Old powerpoint", StandardCharsets.UTF_8);

        File dir2 = new File(tempFolder, "dir1");
        FileUtils.write(new File(dir2,"ignore.pptx.txt"), "ignore me", StandardCharsets.UTF_8);
        FileUtils.write(new File(dir2,"include.pptx"), "New powerpoint", StandardCharsets.UTF_8);

        File dir3 = new File(dir2, "dir1");
        FileUtils.write(new File(dir3,"include..pptx"), "New Powerpoint", StandardCharsets.UTF_8);
    }

    @Test
    void listFiles() {
        FileScanner scanner = new FileScanner(FileType.all());
        List<Path> collect = scanner.listFiles(tempFolder.toPath()).collect(Collectors.toList());
        assertEquals(3, collect.size());

        scanner = new FileScanner(Set.of(FileType.PPT));
        collect = scanner.listFiles(tempFolder.toPath()).collect(Collectors.toList());
        assertEquals(1, collect.size());
    }
}