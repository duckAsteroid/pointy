package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.indexer.scan.FileScanner;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.duck.asteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.asteroid.duck.pointy.indexer.Fields.*;

public class Indexer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final IndexWriter writer;
    private final IndexReader reader;
    private final Path outputDir;
    private final Function<String, Path> checksum;
    private final FileScanner scanner;

    private static final String FORMAT = "JPG";

    public Indexer(IndexReader reader, IndexWriter writer, Function<String, Path> checksum, Path outputDir) {
        this.reader = reader;
        this.writer = writer;
        this.checksum = checksum;
        this.scanner = new FileScanner();
        this.outputDir = outputDir;
    }

    public void index(Path path, ProgressMonitor monitor) throws IOException {
       // scanner.scan(path, monitor.newSubTask("Scan for files"));
        MultiValuedMap<String, Path> files = null;// scanner.getResult();
        for(final String checksum : files.keySet()) {
            Collection<Path> paths = files.get(checksum);
            Document document = new Document();
            document.add(new StringField(FILE_CHECKSUM.name(), checksum, Field.Store.YES ));
            document.add(IndexDocType.SLIDESHOW.asField());
            // load first instance to create PNG and index text
            Path firstPath = mostRecent(paths);
            try (SlideShow<?, ?> slideShow = SlideShowFactory.create(firstPath.toFile(), null, true)) {
                if (indexContent(checksum, slideShow, document)) {
                    // process path/filename fields into index
                    paths.stream().flatMap(Indexer::pathFields).forEach(document::add);
                    writer.addDocument(document);
                }
            }
        }
    }

    private Path mostRecent(Collection<Path> paths) {
        return paths.stream().min(lastModified()).orElseThrow(() -> new NoSuchElementException("No most recent"));
    }

    private Comparator<? super Path> lastModified() {
        return (o1, o2) -> {
            try {
                FileTime lm1 = Files.getLastModifiedTime(o1);
                FileTime lm2 = Files.getLastModifiedTime(o2);
                return lm1.compareTo(lm2);
            }
            catch(IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Stream<IndexableField> pathFields(Path path) {
        // FIXME Does the analyzer spit the path into searchable path segment names?
        return Stream.of(new TextField(FQ_PATH.name(), path.toAbsolutePath().toString(), Field.Store.YES),
                new StringField(FILENAME.name(), path.getFileName().toString(), Field.Store.YES));
    }



    private boolean indexContent(final String checksum, final SlideShow<?, ?> ss, Document document) throws IOException {
        Dimension size = ss.getPageSize();

        List<? extends Slide<?, ?>> slides = ss.getSlides();
        Path slideOutputDir = outputDir.resolve(checksum);
        if (Files.exists(slideOutputDir)) {
            LOG.warn("Skipping indexing: directory already exists for "+checksum);
            return false;
        }
        Files.createDirectories(slideOutputDir);
        SlideShowExtractor extractor = new SlideShowExtractor(ss);
        document.add(new StoredField(THUMBNAIL.name(), slideOutputDir.toString()));
        for (int i=0; i < slides.size(); i++) {
            Slide<?,?> slide = slides.get(i);
            Document slideDocument = new Document();
            slideDocument.add(new StringField(FILE_CHECKSUM.name(), checksum, Field.Store.YES ));
            slideDocument.add(new StoredField(SLIDE_NO.name(), i));
            slideDocument.add(IndexDocType.SLIDE.asField());
            // extract slide title
            Optional<String> title = Optional.ofNullable(slide.getTitle());
            if (title.isPresent() && !title.get().isEmpty()) {
                document.add(new TextField(Fields.TITLE.name(), title.get(), Field.Store.YES));
                slideDocument.add(new TextField(Fields.TITLE.name(), title.get(), Field.Store.YES));
            }

            // extract slide content text
            String content = extractor.getText(slide);
            document.add(new TextField(CONTENT.name(), content, Field.Store.YES));
            slideDocument.add(new TextField(CONTENT.name(), content, Field.Store.YES));



            File file = slideOutputDir.resolve(i + "."+FORMAT.toLowerCase()).toFile();
           // ImageIO.write(img, FORMAT, file);

            slideDocument.add(new StoredField(THUMBNAIL.name(), file.toString()));
            writer.addDocument(slideDocument);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
       writer.close();
    }
}
