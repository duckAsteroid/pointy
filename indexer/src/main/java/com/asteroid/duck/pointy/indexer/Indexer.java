package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.indexer.checksum.Checksum;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static com.asteroid.duck.pointy.indexer.Fields.*;

public class Indexer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private final IndexWriter writer;
    private final Path outputDir;
    private final Checksum checksum;
    private final double scale = 1.0;
    private static final String FORMAT = "JPG";

    public Indexer(IndexWriter writer, Checksum checksum, Path outputDir) {
        this.writer = writer;
        this.checksum = checksum;
        this.outputDir = outputDir;
    }

    public void index(Path path, ProgressMonitor monitor) throws IOException {
        MultiValuedMap<String, Path> files = scan(path, monitor.newSubTask("Scan for files"));
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

    public MultiValuedMap<String, Path> scan(Path path, ProgressMonitor progress) throws IOException {
        MultiValuedMap<String, Path> result = new HashSetValuedHashMap<>();
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Optional<FileType> type = FileType.match(file);
                if (type.isPresent()) {
                    final String csValue = checksum.compute(file);
                    result.put(csValue, file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOG.error("Unable to visit "+file.toString(), exc);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private boolean indexContent(final String checksum, final SlideShow<?, ?> ss, Document document) throws IOException {
        Dimension size = ss.getPageSize();
        int width = (int) (size.width * scale);
        int height = (int) (size.height * scale);
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
            slideDocument.add(new NumericDocValuesField(SLIDE_NO.name(), i));
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


            // Write image
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));
            graphics.scale(scale, scale);
            // draw stuff
            slide.draw(graphics);
            File file = slideOutputDir.resolve(Integer.toString(i) + "."+FORMAT.toLowerCase()).toFile();
            ImageIO.write(img, FORMAT, file);
            graphics.dispose();
            img.flush();
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
