package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.*;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
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

import static org.apache.poi.sl.usermodel.SlideShowFactory.create;

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
    public void process(IndexActionContext ctx) throws IOException {
        Config config = ctx.getConfig();
        Iterator<Path> iter = filenames.iterator();
        while (iter.hasNext()) {
            final Path path = iter.next();
            try {
                Document slideShowDocument = new Document();
                slideShowDocument.add(IndexDocType.SLIDESHOW.asField());

                String filename = IndexUpdateJob.pathString(path);
                SlideShow<?, ?> slideShow = create(new File(filename));
                SlideShowIndexer slideShowIdx = new SlideShowIndexer(checksum, slideShow, filenames);
                slideShowIdx.index(config).forEach(slideShowDocument::add);
                // add all filenames into index
                filenames.stream()
                        .map(IndexUpdateJob::pathString)
                        .map(p -> new StoredField(IndexFieldProvider.FILENAME_FIELD, p))
                        .forEach(slideShowDocument::add);

                // add it to the index
                ctx.getWriter().addDocument(slideShowDocument);

                // Do we need to process the slides too?
                if (config.isSlideTextIndexed() || config.isSlideImageIndexed())
                {
                    for(Slide<?,?> slide : slideShow.getSlides()) {
                        Document slideDocument = new Document();
                        slideDocument.add(IndexDocType.SLIDE.asField());
                        // a reference to the parent
                        slideDocument.add(new StringField("parent", checksum, Field.Store.YES));
                        if (config.isSlideTextIndexed()) {
                            SlideTextIndexer textIndexer = new SlideTextIndexer(checksum, slide);
                            List<IndexableField> slideTextFields = textIndexer.index(config);
                            slideTextFields.forEach(slideDocument::add);
                        }
                        if (config.isSlideImageIndexed()) {
                            Path slidesFolder = ctx.getConfig().getSlideFolder(checksum);
                            SlideImageIndexer imageIndexer = new SlideImageIndexer(slidesFolder, slide);
                            List<IndexableField> slideImageFields = imageIndexer.index(config);
                            slideImageFields.forEach(slideDocument::add);
                        }
                        ctx.getWriter().addDocument(slideDocument);
                    }
                    slideShow.close();
                }
                break; // no more iterating - this file has served us well
            }
            catch(IOException e) {
                LOG.warn("Unable to index file="+path, e);
                // try next
                if (!iter.hasNext()) {
                    throw new IOException("Tried all filenames", e);
                }
            }
        }
    }
}
