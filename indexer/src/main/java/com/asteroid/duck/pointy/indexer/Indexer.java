package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.duck.asteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Indexer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final IndexWriter writer;
    private final IndexReader reader;
    private final Path outputDir;

    public Indexer(Config cfg) {
        this.reader = null;
        this.writer = null;
        this.outputDir = null;
    }

    public void index(ProgressMonitor monitor) throws IOException {

    }


    @Override
    public void close() throws IOException {
        try {
            writer.close();
        }
        finally {
            reader.close();
        }

    }
}
