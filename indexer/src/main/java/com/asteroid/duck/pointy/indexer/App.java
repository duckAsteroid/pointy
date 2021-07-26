package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "indexer", mixinStandardHelpOptions = true, version = "",
        description = "Create an index")
public class App implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    @CommandLine.Option(names = "-c", description = "Configuration file (JSON) to use")
    Path configFile;

    @CommandLine.Option(names = "-o", required = true, description = "Output path for generated index")
    Path outputDir;

    @CommandLine.Parameters(arity = "1..*", description = "Files and folders to index (recursively)")
    Path[] inputFiles;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    public Integer call() {
        Config.ConfigBuilder builder;
        if (configFile != null) {
            // TODO Load the config from the file
            Config cfg = null;
            builder = cfg.toBuilder();
        }
        else {
            builder = Config.withDefaults();
        }
        builder.database(outputDir);

        Set<Path> paths = Stream.of(inputFiles).collect(Collectors.toSet());
        builder.scanRoots(paths);

        Config config = builder.build();
        try(Indexer indexer = new Indexer(config)) {
            indexer.index(null);
        } catch (IOException e) {
            LOG.error("Error creating index", e);
            return -1;
        }
        return 0;
    }
}
