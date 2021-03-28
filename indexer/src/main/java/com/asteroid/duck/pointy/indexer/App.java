package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Checksum;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length >= 2) {
            Path outputDir = Paths.get(args[0]);
            //Checksum checksum = new SHA1Impl();
            FSDirectory d = FSDirectory.open(outputDir.resolve("index"));
            IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter writer = new IndexWriter(d, conf); /*
            try(Indexer indexer = new Indexer(reader, writer, checksum, outputDir)) {
                BaseProgressMonitor monitor = new BaseProgressMonitor(args.length - 1);
                Slf4JProgress progress = new Slf4JProgress(LOG, SimpleProgressFormat.DEFAULT, Slf4JProgress.Level.INFO);
                monitor.addProgressMonitorListener(progress);
                for (int i = 1; i < args.length; i++) {
                    Path path = Paths.get(args[i]);
                    indexer.index(path, monitor.newSubTask(path.toString()));
                }
            }
            */
        }
        else {
            System.out.println("Usage: outputFolder, [inputFolder, ...]");
        }
    }
}
