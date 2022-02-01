package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.*;
import com.asteroid.duck.pointy.indexer.metadata.CoreFields;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import io.github.duckasteroid.progress.ProgressMonitor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * Action to add a new file to the index
 */
public class NewFileAction extends IndexAction {

    private static final Logger LOG = LoggerFactory.getLogger(NewFileAction.class);

    private final Collection<Path> filenames;

    public NewFileAction(String checksum, Collection<Path> filenames) {
        super(checksum);
        this.filenames = filenames;
    }

    @Override
    public void process(IndexActionContext ctx, ProgressMonitor monitor) throws IOException {
        Config config = ctx.getConfig();
        Iterator<Path> iter = filenames.iterator();
        // we "iterate" over the files - in case we encounter IO errors while processing
        // typically we only need/use the first file
        while (iter.hasNext()) {
            final Path path = iter.next();
            try {
                monitor.setSize(4);
                // create an index document for the show...
                Document slideShowDocument = new Document();
                slideShowDocument.add(IndexDocType.SLIDESHOW.asField());

                String filename = IndexUpdateJob.pathString(path);
                SlideShow<?, ?> slideShow = SlideShowFactory.create(new File(filename));
                if (slideShow != null) {
                    SlideShowIndexer slideShowIdx = new SlideShowIndexer(checksum, slideShow, filenames);
                    slideShowIdx.index(config).forEach(slideShowDocument::add);
                    // add all filenames into index
                    filenames.stream()
                            .map(IndexUpdateJob::pathString)
                            .map(p -> new StoredField(CoreFields.FILENAME_FIELD.getFieldName(), p))
                            .forEach(slideShowDocument::add);

                    // add it to the index
                    ctx.add(slideShowDocument);
                    monitor.worked(1, "Indexed slideshow and file data");
                    // Do we need to process the slides too?
                    if (config.isSlideTextIndexed() || config.isSlideImageIndexed()) {
                        ProgressMonitor indexSlides = monitor.newSubTask("Index slides", 3);
                        indexSlides.setSize(slideShow.getSlides().size());
                        for (Slide<?, ?> slide : slideShow.getSlides()) {
                            ProgressMonitor individualSlideIndex = indexSlides.newSubTask("Individual slide index", 1);
                            individualSlideIndex.setSize(2);
                            Document slideDocument = new Document();
                            slideDocument.add(IndexDocType.SLIDE.asField());
                            // a reference to the parent
                            slideDocument.add(new StringField(CoreFields.PARENT.getFieldName(), checksum, Field.Store.YES));
                            if (config.isSlideTextIndexed()) {
                                SlideTextIndexer textIndexer = new SlideTextIndexer(checksum, slide);
                                List<IndexableField> slideTextFields = textIndexer.index(config);
                                slideTextFields.forEach(slideDocument::add);
                            }
                            individualSlideIndex.worked(1, "Text indexed");
                            if (config.isSlideImageIndexed()) {
                                Path slidesFolder = ctx.getConfig().getSlideFolder(checksum);
                                SlideImageIndexer imageIndexer = new SlideImageIndexer(slidesFolder, slide);
                                List<IndexableField> slideImageFields = imageIndexer.index(config);
                                slideImageFields.forEach(slideDocument::add);
                            }
                            individualSlideIndex.worked(1, "Image indexed");
                            ctx.add(slideDocument);
                            individualSlideIndex.done();
                        }
                        try {
                            slideShow.close();
                        } catch (OpenXML4JRuntimeException ex) {
                            LOG.error("Error closing slide show " +
                                    filenames.stream().map(Path::toString).collect(Collectors.joining(", ")), ex);
                        }
                        indexSlides.done();
                    }
                    break; // no more iterating - this file has served us well
                }
                else {
                    LOG.warn("Unable to open slideshow @ "+filename);
                }
            }
            catch(IOException | EmptyFileException | IndexOutOfBoundsException e) {
                LOG.warn("Unable to index file="+path, e);
                // try next
                if (!iter.hasNext()) {
                    throw new IOException("Tried all filenames", e);
                }
            }
        }
        monitor.done();
    }

    @Override
    public String getTaskName() {
        return "new file";
    }
}
