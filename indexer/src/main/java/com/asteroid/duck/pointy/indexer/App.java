package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import io.github.duckasteroid.progress.ProgressMonitorFactory;
import io.github.duckasteroid.progress.console.DefaultConsoleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A runnable (main) class that will perform any necessary index maintenance tasks.
 */
@CommandLine.Command(name = "indexer", mixinStandardHelpOptions = true, version = "",
        description = "Index the filesystem")
public class App implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    @CommandLine.Parameters(index = "0", description = "Database path")
    Path databaseDir;

    @CommandLine.Parameters(index = "1..*", description = "Files and folders to index (recursively). " +
            "These override (and overwrite) any defined in the database config file.")
    Path[] inputFiles;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    public Integer call() throws IOException {
        ProgressMonitorFactory.addListener(DefaultConsoleProvider.provide());
        Set<Path> paths = Stream.of(inputFiles).collect(Collectors.toSet());
        Files.createDirectories(databaseDir);
        Config config = Config.readFrom(databaseDir, paths);
        try(Indexer indexer = new Indexer(config)) {
            indexer.index(ProgressMonitorFactory.newMonitor("Index", 100));
        } catch (IOException e) {
            LOG.error("Error creating index", e);
            return -1;
        }
        return 0;
    }
}
